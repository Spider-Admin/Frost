package freenet.support.sort;

/**
 * A utility class that sorts Sortable objects using the QuickSort algorithm
 */

public class QuickSorter implements SortAlgorithm {

    private static final int r = Integer.MAX_VALUE; // it's a prime

    public void sort(Sortable list) {
        quickSort(list, 0, list.size() - 1);
    }

    public String name() {
        return "Quicksort";
    }

    public static void quickSort(Sortable list) {
        quickSort(list, 0, list.size() - 1);
    }


    private static void quickSort(Sortable list, int off, int last) {
        if (last - off < 1)
            return;
        int f = off;
        int l = last;
        list.swap(f, off + (r % (last + 1 - off))); //avoid sorted degeneration
        while (f < l) {
            int i = list.compare(f, l);
            if (i == 0) // avoid uniform degeneration
                i = f + l & 1;
            if (i > 0) {
                list.swap(f, l); 
                if (f + 1 != l)
                    list.swap(f + 1, l);
                f++;
            } else {
                l--;
            }
        }
        quickSort(list, off, f-1);
        quickSort(list, f+1, last);
    }

}
