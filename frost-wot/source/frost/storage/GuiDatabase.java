/*
  GuiDatabase.java / Frost
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
package frost.storage;

import java.sql.*;
import java.util.*;

public class GuiDatabase extends Database {
    
    private static GuiDatabase instance = null;
    private static MessageDatabaseTable messageTable = null;
    private static SentMessageDatabaseTable sentMessageTable = null;
    
    protected GuiDatabase() throws SQLException {
        super("guidb");
    }
    
    public static void initialize() throws SQLException {
        if( instance == null ) {
            instance = new GuiDatabase();
            messageTable = new MessageDatabaseTable();
            sentMessageTable = new SentMessageDatabaseTable();
            
            instance.ensureTables();
        } else {
            System.out.println("ERROR: GuiDatabase already initialized!");
        }
    }

    public static GuiDatabase getInstance() {
        return instance;
    }
    public static MessageDatabaseTable getMessageTable() {
        return messageTable;
    }
    public static SentMessageDatabaseTable getSentMessageTable() {
        return sentMessageTable;
    }

    private void ensureTables() {
        // messages tables
        for(Iterator i=getMessageTable().getTableDDL().iterator(); i.hasNext(); ) {
            String tableDDL = (String)i.next();
            try {
                update(tableDDL);
            } catch(SQLException ex) {
                // table already exists
                ex.printStackTrace();
            }
        }
        // sent messages tables
        for(Iterator i=getSentMessageTable().getTableDDL().iterator(); i.hasNext(); ) {
            String tableDDL = (String)i.next();
            try {
                update(tableDDL);
            } catch(SQLException ex) {
                // table already exists
                ex.printStackTrace();
            }
        }
    }
}
