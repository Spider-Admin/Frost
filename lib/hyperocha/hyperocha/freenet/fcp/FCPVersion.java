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

import hyperocha.util.Version;

/**
 * @author  saces
 */
public class FCPVersion {
	public static final FCPVersion REQUEIRD_FCP_VERSION = new FCPVersion("Fred", new Version(0,7), new Version(1,0), 964);
	//Version=Fred,0.7,1.0,953
	private String name;
	private Version major;
	private Version minor;
	private int build;


	/**
	 * 
	 */
	public FCPVersion(String ver) {
		
	}


	private FCPVersion(String s, Version maj, Version min, int i) {
		name = s;
		major = maj;
		minor = min;
		build = i;
	}
	
	
	public boolean isTestnet() {
		return false; // TODO
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
//	public boolean equals(Object obj) {
//		if (!(obj instanceof FCPVersion)) return false;
//		// TODO Auto-generated method stub
//		return super.equals(obj);
//	}
//

}
