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
public class Verbosity {
	public static final int ALL = Integer.MAX_VALUE;
	public static final int NONE = 0;
	public static final int SPLITFILE_PROGRESS = 1;
	public static final int PUT_FETCHABLE = 256;
	public static final int COMPRESSION_START_END = 512;

	private int verbosity;

	/**
	 * 
	 */
	public Verbosity() {
		verbosity = Verbosity.NONE;
	}

	/**
	 * @param verbosity
	 */
	public Verbosity(int verbosity) {
		this.verbosity = verbosity;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof Verbosity)) return false; 
		return ((Verbosity)obj).verbosity == verbosity;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "" + verbosity;
	}
	
	public int getVerbosity() {
		return verbosity;
	}

}
