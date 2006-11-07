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
 * @version $Id$
 *
 */
public class UploadFrom {
	public static final int DIRECT = 0;
	public static final int DISK = 1;
	public static final int REDIRECT = 2;
 
	private int type;
	private String source;
	private int count;
	private InputStream is;
	
	/**
	 * this is a file insert
	 */
	public UploadFrom(File f) {
		type = DISK;
		source = f.getAbsolutePath();
	}
	
	/**
	 * this is a direct insert, the stream from actual pos and n bytes
	 */
	public UploadFrom(InputStream s, int count) {
		type = DIRECT;
		this.count = count;
		is = s;
	}
	
	/**
	 * this is a redirect insert
	 */
	public UploadFrom(FreenetKey key) {
		type = REDIRECT;
		source = key.getReadFreenetKey();
	}
	
	
	/*
	 * geek constructor disk/redirect
	 */
	public UploadFrom(int type, String source) {
		this.type = type;
		this.source = source;
	}
	
	/**
	 * this is a cascaded redirect
	 * will be enrolled while insert
	 */
	public UploadFrom(UploadFrom ulf) {
		// TODO
		//
	}

	/**
	 * @return  Returns the count.
	 */
	protected int getCount() {
		return count;
	}

	/**
	 * @return  Returns the is.
	 */
	protected InputStream getIs() {
		return is;
	}

	/**
	 * @return  Returns the source.
	 */
	protected String getSource() {
		return source;
	}

	/**
	 * @return  Returns the type.
	 */
	protected int getType() {
		return type;
	}

}

