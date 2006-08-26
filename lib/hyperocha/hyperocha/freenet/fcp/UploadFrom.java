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

import java.io.File;
import java.io.InputStream;

/**
 * @author saces
 *
 */
public class UploadFrom {
	public static final int DIRECT = 0;
	public static final int DISK = 1;
	public static final int REDIRECT = 2;
 
	private int type;
	private String source;
	private long count;
	private InputStream is;
	
	/**
	 * this is a file insert
	 */
	public UploadFrom(File f) {
		type = DISK;
		source = f.getAbsolutePath();
	}
	
	/**
	 * this is a direct insert, the entire stream
	 */
	public UploadFrom(InputStream s) {
		type = DIRECT;
		count = -1;
		is = s;
	}
	
	/**
	 * this is a direct insert, the stream from actual pos and n bytes
	 */
	public UploadFrom(InputStream s, long count) {
		type = DIRECT;
		this.count = count;
		is = s;
	}
	
	/**
	 * this is a redirect insert
	 */
	public UploadFrom(FreenetURL key) {
		type = REDIRECT;
		source = key.getKey();
	}

}
