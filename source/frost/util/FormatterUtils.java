/*
  FormatterUtils.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.util;

import java.text.*;

/**
 * Partially derived from freenetproject.
 */
public class FormatterUtils {

    private static final String[] sizeSuffixes = { " B", " KiB", " MiB", " GiB", " TiB", " PiB", " EiB", " ZiB", " YiB" };

    private final static NumberFormat numberFormat;
    static {
        numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);
    }

    public static String formatSize(final long sz) {
        // First determine suffix
        long s = 1;
        int i;
        for( i = 0; i < sizeSuffixes.length; i++ ) {
            s *= 1024;
            if( s > sz ) {
                break; // Smaller than multiplier [i] - use the previous one
            }
        }

        s /= 1024; // we use the previous unit

        if( s == 1 ) {
            // Bytes? Then we don't need real numbers with a comma
            return sz + sizeSuffixes[0];
        } else {
            final double mantissa = (double) sz / (double) s;
            String o = Double.toString(mantissa);
            if( o.indexOf('.') == 3 ) {
                o = o.substring(0, 3);
            } else if( (o.indexOf('.') > -1) && (o.indexOf('E') == -1) && (o.length() > 4) ) {
                o = o.substring(0, 4);
            }
            o += sizeSuffixes[i];
            return o;
        }
    }

    public static String formatPercent(final int value, int maxValue) {
        if( maxValue == 0 ) {
            maxValue = 1;
        }
        final double d = ((double) value * (double) 100) / (maxValue);
        return numberFormat.format(d);
    }

    public static String formatFraction(final long value, long maxValue) {
        if( maxValue == 0 ) {
            maxValue = 1;
        }
        final double d = (double) value / (double) maxValue;
        return numberFormat.format(d);
    }
}
