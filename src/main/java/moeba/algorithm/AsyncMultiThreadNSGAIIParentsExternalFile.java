package moeba.algorithm;

import java.util.List;
import org.uma.jmetal.util.termination.Termination;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.parallel.asynchronous.task.ParallelTask;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.archive.Archive;
import org.uma.jmetal.util.archive.impl.NonDominatedSolutionListArchive;

/**
 * Extends AsyncMultiThreadNSGAIIParents to support the maintenance of an external archive.
 * This archive stores the best solutions found during the algorithm execution, evaluated according
 * to their dominance relation. The class is designed to work in an asynchronous, multi-threaded
 * environment, allowing for efficient solution evaluation and archiving in parallel.
 *
 * @param <S> Solution type extending the Solution interface, which represents the type of solutions
 *            the algorithm will work with.
 */
public class AsyncMultiThreadNSGAIIParentsExternalFile<S extends Solution<?>>
    extends AsyncMultiThreadNSGAIIParents<S> {

  protected Archive<S> externalArchive; // Archive to store non-dominated solutions.

  /**
   * Constructor to initialize the algorithm with necessary operators, problem definition,
   * and termination condition. It also initializes an external archive to store the best solutions
   * found during the execution.
   *
   * @param numberOfCores   Number of cores to use for parallel execution.
   * @param problem         Problem to be solved by the algorithm.
   * @param populationSize  Size of the population.
   * @param crossover       Crossover operator to be used in the algorithm.
   * @param mutation        Mutation operator to be used in the algorithm.
   * @param termination     Condition that determines when the algorithm should terminate.
   */
  public AsyncMultiThreadNSGAIIParentsExternalFile(
      int numberOfCores,
      Problem<S> problem,
      int populationSize,
      CrossoverOperator<S> crossover,
      MutationOperator<S> mutation,
      Termination termination) {
    super(numberOfCores, problem, populationSize, crossover, mutation, termination);

    // Initializes the external archive with a NonDominatedSolutionListArchive, allowing
    // for the storage of non-dominated solutions up to a specified limit (populationSize).
    externalArchive = new BestSolutionsArchive<>(new NonDominatedSolutionListArchive<>(), populationSize);
  }

  /**
   * Processes each computed task, adding the solution to the external archive before continuing
   * with the normal task processing flow. This allows the algorithm to maintain a separate list
   * of non-dominated solutions outside of the main population.
   *
   * @param task The computed task containing a solution to be added to the external archive.
   */
  @Override
  public void processComputedTask(ParallelTask<S> task) {
    externalArchive.add(task.getContents()); // Adds the task's solution to the external archive.
    super.processComputedTask(task); // Continues with the standard task processing.
  }

  /**
   * Returns the list of solutions stored in the external archive. This list represents the best
   * solutions found by the algorithm according to their dominance relations.
   *
   * @return List of non-dominated solutions stored in the external archive.
   */
  @Override
  public List<S> getResult() {
    return externalArchive.getSolutionList();
  }
}
