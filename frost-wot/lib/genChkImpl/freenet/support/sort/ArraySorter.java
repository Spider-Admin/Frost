package freenet.support.sort;

import freenet.support.*;
import freenet.support.Comparable;

/** An object that can be passed to the HeapSorter, or other sorters
  * that take a Sortable object.  It sorts the underlying array passed
  * in to the constructor.  Unfortunately, it only works on Object[]'s.
  * @author tavin
  */
public final class ArraySorter implements Sortable {

    private final Object[] target;
    private final Comparator comp;

    private final int offset, size;

    public ArraySorter(Object[] target) {
        this(target, null, 0, target.length);
    }

    public ArraySorter(Object[] target, int offset, int size) {
        this(target, null, offset, size);
    }

    public ArraySorter(Object[] target, Comparator comp) {
        this(target, comp, 0, target.length);
    }

    public ArraySorter(Object[] target, Comparator comp, int offset, int size) {
        this.target = target;
        this.comp   = comp;
        this.offset = offset;
        this.size   = size;
    }

    public final int compare(int index1, int index2) {
        return comp == null
            ? ((Comparable) target[offset+index1]).compareTo(target[offset+index2])
            : comp.compare(target[offset+index1], target[offset+index2])
            ;
    }

    public final void swap(int index1, int index2) {
        Object t = target[offset+index1];
        target[offset+index1] = target[offset+index2];
        target[offset+index2] = t;
    }

    public final int size() {
        return size;
    }
}


