package freenet.support.sort;

/**
 * A generic window to a zero-based integer-indexible data structure
 * which supports the notion of comparison between two indices and
 * swapping of two indices.
 */
public interface Sortable {

    /**
     * Returns an int greater than, equal to, or less than 0
     * according to whether the object at index1 is greater
     * than, equal to, or less than the object at index2.
     * The sense of the comparison is the same as in Comparator.
     * @param index1 the first object
     * @param index2 the second object
     * @return >0 if index1>index2, ==0 if index1==index2, else <0
     */
    int compare(int index1, int index2);

    /**
     * Exchanges two objects.
     * @param index1 the first object
     * @param index2 the second object
     */
    void swap(int index1, int index2);

    /**
     * Returns the indexible size of the backing structure.
     * @return the size, 0<=index<size
     */
    int size();
}


