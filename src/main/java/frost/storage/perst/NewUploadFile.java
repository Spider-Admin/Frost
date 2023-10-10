/*
 NewUploadFile.java / Frost
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
package frost.storage.perst;

import java.io.File;

import org.garret.perst.Persistent;

/**
 * Holds the data for a new upload file until SHA is calculated.
 */
public class NewUploadFile extends Persistent {

    protected String filePath;
    protected String from;
    protected boolean replacePathIfFileExists;

    public NewUploadFile() {
    }

    public NewUploadFile(final File f, final String fromName, final boolean replacePath) {
        filePath = f.getPath();
        from = fromName;
        replacePathIfFileExists = replacePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFrom() {
        return from;
    }

    public boolean isReplacePathIfFileExists() {
        return replacePathIfFileExists;
    }
}
