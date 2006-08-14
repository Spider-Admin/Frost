/**
 *   This file is part of JHyperochaFCPLib.
 *   
 *   Copyright (C) 2006  Hyperocha Project <saces@users.sourceforge.net>
 * 
 * JHyperochaFCPLib is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * JHyperochaFCPLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JHyperochaFCPLib; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 */
package hyperocha.freenet.fcp;

public class PriorityClass {

    public static final PriorityClass MAXIMUM = new PriorityClass("maximum", 0);
    public static final PriorityClass INTERACTIVE = new PriorityClass("interactive", 1);
    public static final PriorityClass SEMI_INTERACTIVE = new PriorityClass("semiinteractive", 2);
    public static final PriorityClass UPDATABLE = new PriorityClass("updatable", 3);
    public static final PriorityClass BULK = new PriorityClass("bulk", 4);
    public static final PriorityClass PREFETCH = new PriorityClass("prefetch", 5);
    public static final PriorityClass MINIMUM = new PriorityClass("minimum", 6);

    private String name;
    private int value;
    
    private static PriorityClass[] a = {
    		MAXIMUM, 			//0
    		INTERACTIVE,		//1
    		SEMI_INTERACTIVE,	//2
    		UPDATABLE,			//3
    		BULK, 				//4
    		PREFETCH,			//5
    		MINIMUM 			//6
    		};

    private PriorityClass(String name, int value) {
            this.name = name;
            this.value = value;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
            return name;
    }
       
    static public String getPriorityName(int p) {
    	return a[p].getName();
    }

    /**
     * @return Returns the value.
     */
    public int getValue() {
            return value;
    }
    
    public String toString() {
    	return name;
    } 
    
    public static int getIDfromName(String name) {
    	if (name.compareToIgnoreCase("maximum") == 0) { return 0; }
    	if (name.compareToIgnoreCase("interactive") == 0) { return 1; }
    	if (name.compareToIgnoreCase("semiinteractive") == 0) { return 2; }
    	if (name.compareToIgnoreCase("updatable") == 0) { return 3; }
    	if (name.compareToIgnoreCase("bulk") == 0) { return 4; }
    	if (name.compareToIgnoreCase("prefetch") == 0) { return 5; }
    	if (name.compareToIgnoreCase("minimum") == 0) { return 6; }
    	return -1;
    }
}
