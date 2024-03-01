package moeba.algorithm;

import java.util.List;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.archive.Archive;

/**
 * Implements an archive that wraps around another archive and selects the best solutions from it
 * using a distance-based subset selection method. This archive is useful for post-processing
 * the results of an optimization algorithm to obtain a subset of solutions that are not only
 * diverse but also representative of the problem space.
 *
 * @param <S> The type of the solutions stored in the archive, which must extend the Solution interface.
 */
public class BestSolutionsArchive<S extends Solution<?>> implements Archive<S> {
  private Archive<S> archive; // The underlying archive from which solutions are selected.
  private int numberOfSolutionsToSelect; // The number of solutions to select from the archive.

  /**
   * Constructs a BestSolutionsArchive with a reference to an existing archive and a specified
   * number of solutions to select. This constructor initializes the archive with the target
   * archive and the number of solutions that should be selected from it.
   *
   * @param archive The existing archive from which solutions will be selected.
   * @param numberOfSolutionsToSelect The number of best solutions to select from the archive.
   */
  public BestSolutionsArchive(Archive<S> archive, int numberOfSolutionsToSelect) {
    this.archive = archive;
    this.numberOfSolutionsToSelect = numberOfSolutionsToSelect;
  }

  /**
   * Adds a solution to the underlying archive. The decision to actually add the solution
   * is delegated to the underlying archive's add method.
   *
   * @param solution The solution to be added to the archive.
   * @return true if the solution was added successfully, false otherwise.
   */
  @Override
  public boolean add(S solution) {
    return archive.add(solution);
  }

  /**
   * Retrieves a solution by its index from the underlying archive.
   *
   * @param index The index of the solution to retrieve.
   * @return The solution at the specified index in the archive.
   */
  @Override
  public S get(int index) {
    return archive.get(index);
  }

  /**
   * Returns a list of solutions selected from the underlying archive. The selection is
   * based on a distance-based subset selection method, which ensures that the returned
   * subset of solutions is diverse and represents different regions of the solution space.
   *
   * @return A list of selected solutions from the archive.
   */
  @Override
  public List<S> getSolutionList() {
    return SolutionListUtils.distanceBasedSubsetSelection(archive.getSolutionList(), numberOfSolutionsToSelect);
  }

  /**
   * Returns the size of the underlying archive, which is the total number of solutions
   * it contains.
   *
   * @return The size of the archive.
   */
  @Override
  public int size() {
    return archive.size();
  }
}
