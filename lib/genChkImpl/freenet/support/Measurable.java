package freenet.support;

/**
 * Extends the two-object comparison of Comparable
 * with a three-object comparison of the relative
 * distances between objects.
 */
public interface Measurable extends Comparable {

    /**
     * @return  0 if this object is equidistant b/t A and B
     *         >0 if the distance to A is greater
     *         <0 if the distance to B is greater
     */
    int compareTo(Object A, Object B);

    /**
     * The same, but this object is known to fall between A and B
     * in the sort order.
     */
    int compareToSorted(Object A, Object B);
}

