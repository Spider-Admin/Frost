package org.shiftone.cache.util;



import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;


/**
 * @version $Revision$
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 */
public class MapFactory
{

    private static final Log  LOG                 = new Log(MapFactory.class);
    public static final float DEFAULT_LOAD_FACTOR = 0.5f;
    public static final int[] PRIMES              =
    {

        // 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 37, 43, 53, 59, 67, 79, 89, 101, 113, 127, 149,        //
        167, 191, 223, 251, 281, 313, 349, 389, 433, 487, 541, 601, 673, 751, 839, 937, 1049,           //
             1171, 1301, 1447, 1607, 1787, 1987, 2207, 2459, 2731, 3037, 3373, 3761, 4177, 4637,        //
             5153, 5737, 6373, 7079, 7867, 8737, 9719, 10789, 11981, 13309, 14779, 16411, 18217,        //
             20231, 22469, 24943, 27689, 30757, 34141, 37897, 42071, 46703, 51853, 57557, 63901,        //
             70937, 78779, 87473, 97103, 107791, 119653, 132817, 147449, 163673, 181693, 201683,        //
             223903, 248533, 275881, 306239, 339943, 377339, 418849, 464923, 516077, 572867, 635891,    //
             705841, 783487, 869683, 965357, 1071563, 1189453, 1320301, 1465547, 1626763, 1805729,      //
             2004377, 2224861, 2469629, 2741303, 3042857, 3377579, 3749117, 4161527, 4619309,           //
             5127433, 5691457, 6317527, 7012469, 7783843, 8640109, 9590531, 10645507, 11816521,         //
             13116343, 14559151, 16160663, 17938357, 19911581, 22101889, 24533099, 27231751,            //
             30227287, 33552293, 37243051, 41339843, Integer.MAX_VALUE
    };

    public static int getNearPrime(int number)
    {

        int value = Arrays.binarySearch(PRIMES, number);

        if (value < 0)
        {
            value = -value - 1;
        }

        return PRIMES[value];
    }


    public static Map createMap(int initialCapacity)
    {
        return new Hashtable(getNearPrime(initialCapacity), DEFAULT_LOAD_FACTOR);
    }


    private static void test(int a)
    {

        int b = getNearPrime(a);

        LOG.info(a + " -> " + b);
    }


    public static void main(String args[])
    {

        test(0);
        test(965356);
        test(965357);
        test(965358);
        test(Integer.MAX_VALUE);
    }
}
