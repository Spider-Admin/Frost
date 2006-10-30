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
package hyperocha.freenet.fcp.dispatcher.job;

import hyperocha.freenet.fcp.FreenetKey;

import java.io.File;


/**
 * @author saces
 *
 */
public class GenerateCHKJob extends Job {

	/**
	 * 
	 */
	private GenerateCHKJob(int requirednetworktype, String id) {
		super(requirednetworktype, id);
	}
	
	public GenerateCHKJob(int requirednetworktype, String id, File file) {
		this(requirednetworktype, id);
	}
	
	public GenerateCHKJob(int requirednetworktype, CHKFileInsertJob fij, String id) {
		this(requirednetworktype, id);
	}

	
	public FreenetKey getKey() {
		return null;
	}

	public boolean doPrepare() {
		return false;
	}

}
