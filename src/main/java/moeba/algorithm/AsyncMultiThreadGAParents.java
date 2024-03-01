package moeba.algorithm;

import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.replacement.Replacement;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.parallel.asynchronous.multithreaded.Master;
import org.uma.jmetal.parallel.asynchronous.multithreaded.Worker;
import org.uma.jmetal.parallel.asynchronous.task.ParallelTask;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.observable.Observable;
import org.uma.jmetal.util.observable.impl.DefaultObservable;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.termination.Termination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Implements an asynchronous multi-threaded genetic algorithm (GA) with parallel evaluation of parents.
 * This class extends the Master class provided by the jMetal framework to support parallel execution of tasks
 * in a genetic algorithm context. It handles the creation, execution, and updating of tasks representing
 * genetic operations such as mutation and crossover in an asynchronous manner.
 *
 * @param <S> Solution type that extends the Solution interface, representing the type of solutions the GA will work with.
 */
public class AsyncMultiThreadGAParents<S extends Solution<?>>
    extends Master<ParallelTask<S>, List<S>> {
  private Problem<S> problem;
  private CrossoverOperator<S> crossover;
  private MutationOperator<S> mutation;
  private SelectionOperator<List<S>, S> selection;
  private Replacement<S> replacement;
  private Termination termination;

  private List<S> population = new ArrayList<>();
  private int populationSize;
  private int evaluations = 0;
  private long initTime;

  private Map<String, Object> attributes;
  private Observable<Map<String, Object>> observable;

  private int numberOfCores;

  /**
   * Constructs an AsyncMultiThreadGAParents object with the specified parameters.
   *
   * @param numberOfCores   The number of cores to use for parallel task execution.
   * @param problem         The problem to be solved by the GA.
   * @param populationSize  The size of the population.
   * @param crossover       The crossover operator to be used.
   * @param mutation        The mutation operator to be used.
   * @param selection       The selection operator to be used for selecting parents.
   * @param replacement     The replacement strategy to be used for creating the new population.
   * @param termination     The termination condition to be checked to stop the algorithm.
   */
  public AsyncMultiThreadGAParents(
      int numberOfCores,
      Problem<S> problem,
      int populationSize,
      CrossoverOperator<S> crossover,
      MutationOperator<S> mutation,
      SelectionOperator<List<S>, S> selection,
      Replacement<S> replacement,
      Termination termination) {
    super(numberOfCores);
    this.problem = problem;
    this.crossover = crossover;
    this.mutation = mutation;
    this.populationSize = populationSize;
    this.termination = termination;
    this.selection = selection;
    this.replacement = replacement;

    attributes = new HashMap<>();
    observable = new DefaultObservable<>("Observable");

    this.numberOfCores = numberOfCores;

    createWorkers(numberOfCores, problem);
  }

  /**
   * Creates worker threads for parallel task execution.
   *
   * @param numberOfCores The number of worker threads to create.
   * @param problem       The problem to be solved, used in the evaluation of solutions.
   */
  private void createWorkers(int numberOfCores, Problem<S> problem) {
    IntStream.range(0, numberOfCores).forEach(i -> new Worker<>(
        (task) -> {
          problem.evaluate(task.getContents());
          return ParallelTask.create(createTaskIdentifier(), task.getContents());
        },
        pendingTaskQueue,
        completedTaskQueue).start());
  }

  /**
   * Generates a unique identifier for a task.
   *
   * @return An integer representing the task identifier.
   */
  private int createTaskIdentifier() {
    return JMetalRandom.getInstance().nextInt(0, 1000000000);
  }

  /**
   * Initializes the progress attributes at the beginning of the algorithm's execution.
   */
  @Override
  public void initProgress() {
    attributes.put("EVALUATIONS", evaluations);
    attributes.put("POPULATION", population);
    attributes.put("COMPUTING_TIME", System.currentTimeMillis() - initTime);

    observable.setChanged();
    observable.notifyObservers(attributes);
  }

  /**
   * Updates progress attributes during the algorithm's execution.
   */
  @Override
  public void updateProgress() {
    attributes.put("EVALUATIONS", evaluations);
    attributes.put("POPULATION", population);
    attributes.put("COMPUTING_TIME", System.currentTimeMillis() - initTime);
    attributes.put("BEST_SOLUTION", population.get(0));

    observable.setChanged();
    observable.notifyObservers(attributes);
  }

  /**
   * Creates the initial set of tasks representing the initial population.
   *
   * @return A list of ParallelTask objects representing the initial solutions to be evaluated.
   */
  @Override
  public List<ParallelTask<S>> createInitialTasks() {
    List<S> initialPopulation = new ArrayList<>();
    List<ParallelTask<S>> initialTaskList = new ArrayList<>();
    IntStream.range(0, populationSize)
        .forEach(i -> initialPopulation.add(problem.createSolution()));
    initialPopulation.forEach(
        solution -> {
          int taskId = JMetalRandom.getInstance().nextInt(0, 1000);
          initialTaskList.add(ParallelTask.create(taskId, solution));
        });

    return initialTaskList;
  }

  /**
   * Submits the initial tasks for execution.
   *
   * @param initialTaskList The list of initial tasks to be submitted.
   */
  @Override
  public void submitInitialTasks(List<ParallelTask<S>> initialTaskList) {
    if (initialTaskList.size() >= numberOfCores) {
      initialTaskList.forEach(this::submitTask);
    } else {
      int idleWorkers = numberOfCores - initialTaskList.size();
      initialTaskList.forEach(this::submitTask);
      while (idleWorkers > 0) {
        submitTask(createNewTask());
        idleWorkers--;
      }
    }
  }

  /**
   * Processes a computed task, updating the population with the new solution.
   *
   * @param task The computed ParallelTask containing a solution.
   */
  @Override
  public void processComputedTask(ParallelTask<S> task) {
    evaluations++;
    if (population.size() < populationSize) {
      population.add(task.getContents());
    } else {
      List<S> offspringPopulation = new ArrayList<>(1);
      offspringPopulation.add(task.getContents());

      population = replacement.replace(population, offspringPopulation);
      Check.that(population.size() == populationSize, "The population size is incorrect");
    }
  }

  /**
   * Submits a task for execution.
   *
   * @param task The ParallelTask to be submitted.
   */
  @Override
  public void submitTask(ParallelTask<S> task) {
    pendingTaskQueue.add(task);
  }

  /**
   * Creates a new task for execution. This method is used when creating tasks dynamically during the algorithm's execution.
   *
   * @return A new ParallelTask object.
   */
  @Override
  public ParallelTask<S> createNewTask() {
    int numberOfParents = crossover.getNumberOfRequiredParents();
    if (population.size() > numberOfParents) {
      List<S> parents = new ArrayList<>(numberOfParents);
      for (int i = 0; i < numberOfParents; i++) {
        parents.add(selection.execute(population));
      }

      List<S> offspring = crossover.execute(parents);

      mutation.execute(offspring.get(0));

      return ParallelTask.create(createTaskIdentifier(), offspring.get(0));
    } else {
      return ParallelTask.create(createTaskIdentifier(), problem.createSolution());
    }
  }

  /**
   * Checks whether the termination condition of the algorithm has not been met.
   *
   * @return True if the termination condition is not met, false otherwise.
   */
  @Override
  public boolean stoppingConditionIsNotMet() {
    return !termination.isMet(attributes);
  }

  /**
   * Starts the execution of the asynchronous multi-threaded genetic algorithm.
   */
  @Override
  public void run() {
    initTime = System.currentTimeMillis();
    super.run();
  }

  /**
   * Returns the result of the genetic algorithm, which is the current population.
   *
   * @return A list of solutions representing the current population.
   */
  @Override
  public List<S> getResult() {
    return population;
  }

  /**
   * Gets the observable object for this algorithm.
   *
   * @return The Observable object that allows observers to track changes.
   */
  public Observable<Map<String, Object>> getObservable() {
    return observable;
  }
}
