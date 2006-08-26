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

/**
 * @author saces
 *
 */
public class FreenetKeyType {
	public static final FreenetKeyType KSK = new FreenetKeyType(0);
	public static final FreenetKeyType CHK = new FreenetKeyType(1);
	public static final FreenetKeyType SSK = new FreenetKeyType(2);
	public static final FreenetKeyType USK = new FreenetKeyType(3);
	public static final FreenetKeyType TUK = new FreenetKeyType(4);
     
    private int keyType;
    
    private final String[] names = { "KSK@",
    		                     "CHK@",
    		                     "SSK@",
    							 "USK@",
    							 "TUK@" };

    /**
	 * 
	 */
	private FreenetKeyType(int kt) {
		keyType = kt;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof FreenetKeyType)) return false; 
		return ((FreenetKeyType)obj).keyType == keyType;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return names[keyType];
	}

}
