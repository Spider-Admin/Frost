/**
 *   This file is part of JHyperochaUtilLib.
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
package hyperocha.util;

/**
 * @author saces
 *
 */
public class Version implements Comparable {
	private int minor;
	private int major;

	/**
	 * 
	 */
	public Version(int major, int minor) {
		this.major = major;
		this.minor = minor;
	}
	
	public Version(String v) {
		String[] s = v.split(".");
		this.major = Integer.parseInt(s[0]);
		this.minor = Integer.parseInt(s[1]);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(T)
	 * Parameters: o - the Object to be compared.
	 * Returns: a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	 */
	public int compareTo(Object arg0) {
		Version v = (Version)arg0;
		if (this.major < v.major) { return -1; }
		if (this.major > v.major) { return 1; }
		// this.major = v.major
		if (this.minor < v.minor) { return -1; }
		if (this.minor > v.minor) { return 1; }
		//	this.minor = v.minor
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */

	public boolean equals(Object arg0) {
		if (!(arg0 instanceof Version)) return false;
		return (compareTo(arg0) == 0);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */

	public String toString() {
		return major + "." + minor;
	}

}
