package freenet.support.sort;

/**
 * An interface for classes that can sort Sortable lists.
 *
 * @author oskar
 */

public interface SortAlgorithm {
    void sort(Sortable list);

    String name();
}
