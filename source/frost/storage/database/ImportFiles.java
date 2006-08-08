/*
 ImportFiles.java / Frost
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
package frost.storage.database;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.gui.objects.*;
import frost.messages.*;
import frost.storage.database.applayer.*;

public class ImportFiles {

    private static Logger logger = Logger.getLogger(ImportFiles.class.getName());

    public void importFiles() {

        List allBoards = MainFrame.getInstance().getTofTreeModel().getAllBoards();
        FileListDatabaseTable dbt = AppLayerDatabase.getFileListDatabaseTable();

        for( Iterator i = allBoards.iterator(); i.hasNext(); ) {
            Board board = (Board) i.next();

            File keypoolDir = new File(MainFrame.keypool + board.getBoardFilename());
            if( keypoolDir.isDirectory() ) {
                File shaIndex = new File(keypoolDir + File.separator + "files.xml");
                if( shaIndex.exists() ) {
                    FrostIndex frostIndex = FrostIndex.readKeyFile(shaIndex, board);
                    if( frostIndex == null ) {
                        logger.log(Level.SEVERE, "Error reading keyfile");
                        continue;
                    }
                    Iterator j = frostIndex.getFilesMap().values().iterator();
                    while( j.hasNext() ) {
                        SharedFileXmlFile key = (SharedFileXmlFile) j.next();

                        FrostSharedFileObject fo = new FrostSharedFileObject(key);
                        fo.setLastReceived(FrostSharedFileObject.defaultDate);

                        try {
                            dbt.insertOrUpdateFrostSharedFileObject(fo);
                        } catch (SQLException e) {
                            logger.log(Level.SEVERE, "Error inserting file", e);
                        }
                    }
                }
            }
        }
    }
}
