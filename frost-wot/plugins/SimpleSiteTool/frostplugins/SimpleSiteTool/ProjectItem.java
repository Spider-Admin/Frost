/*
 SimpleSiteTool.java / Frost
 Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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

package frostplugins.SimpleSiteTool;

import org.garret.perst.Persistent;

public class ProjectItem extends Persistent {
	protected String projectName = "<untitled project>";
	protected String sourceDir = null;
		
	protected boolean index = true;
	protected boolean indexGenerated = false;
	protected String indexFilename = "index.html";
	
	protected boolean announce = true;
	protected String announceTo = "test"; // FIXME change default to "sites" befor release
	protected String announceFrom = "<anonymouse>";
	
	protected String keyType = "USK";
	protected String insertUri = null;
	protected String sitePath = null;
	protected int edition = 1;
	
	protected boolean makePersistent = true;
	
	protected int insertCmd = 2;
	
	protected String jobIdentifier = null;

	public ProjectItem() {
	}
}
