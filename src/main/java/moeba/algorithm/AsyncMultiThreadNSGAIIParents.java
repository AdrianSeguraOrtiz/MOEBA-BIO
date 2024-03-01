package moeba.algorithm;

import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.replacement.Replacement;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.replacement.impl.RankingAndDensityEstimatorReplacement;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.densityestimator.impl.CrowdingDistanceDensityEstimator;
import org.uma.jmetal.util.ranking.impl.MergeNonDominatedSortRanking;
import org.uma.jmetal.util.termination.Termination;

/**
 * Extends the asynchronous multi-threaded genetic algorithm (GA) to implement the NSGA-II algorithm,
 * utilizing multi-threading for parallel solution evaluation. NSGA-II is a popular multi-objective genetic
 * algorithm known for its effectiveness in handling multi-objective optimization problems.
 *
 * @param <S> Solution type that extends the Solution interface, representing the type of solutions the algorithm will work with.
 */
public class AsyncMultiThreadNSGAIIParents<S extends Solution<?>>
    extends AsyncMultiThreadGAParents<S> {

  /**
   * Constructs an AsyncMultiThreadNSGAIIParents object with the specified parameters. This constructor
   * sets up the NSGA-II algorithm with binary tournament selection, ranking and density estimator for replacement,
   * and other necessary genetic operators.
   *
   * @param numberOfCores   The number of cores to use for parallel task execution.
   * @param problem         The problem to be solved, which is multi-objective in nature.
   * @param populationSize  The size of the population.
   * @param crossover       The crossover operator to be used for generating new offspring.
   * @param mutation        The mutation operator to be applied to the offspring.
   * @param termination     The termination condition to determine when the algorithm should stop.
   */
  public AsyncMultiThreadNSGAIIParents(
      int numberOfCores,
      Problem<S> problem,
      int populationSize,
      CrossoverOperator<S> crossover,
      MutationOperator<S> mutation,
      Termination termination) {
    super(numberOfCores,problem, populationSize, crossover,mutation,
          // BinaryTournamentSelection is used for selecting parents for crossover. 
          // It uses a RankingAndCrowdingDistanceComparator to maintain diversity.
          new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>()),
          // RankingAndDensityEstimatorReplacement combines ranking and crowding distance to replace individuals.
          // It ensures a diverse front of non-dominated solutions.
          new RankingAndDensityEstimatorReplacement<>(
                  new MergeNonDominatedSortRanking<>(), // Sorts individuals based on dominance ranking.
                  new CrowdingDistanceDensityEstimator<>(), // Assigns crowding distance to maintain diversity.
                  Replacement.RemovalPolicy.oneShot), // Specifies the removal policy for the replacement strategy.
          termination);
  }
}
