/*
  FcpInsert.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

/*
 * PORTED TO 0.7 by Roman 22.01.06 00:10
 */
package frost.fcp.fcp07;

import java.io.*;
import java.net.*;
import java.util.logging.*;

import javax.swing.*;

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.upload.*;

/**
 * This class provides methods to insert data into freenet.
 */
public class FcpInsert {

	private static final Logger logger = Logger.getLogger(FcpInsert.class.getName());

    /**
     * Inserts a file into freenet.
     * The maximum file size for a KSK/SSK direct insert is 32kb! (metadata + data!!!)
     * The uploadItem is needed for FEC splitfile puts,
     * for inserting e.g. the pubkey.txt file set it to null.
     * Same for uploadItem: if a non-uploadtable file is uploaded, this is null.
     */
    public static FcpResultPut putFile(
            final int type,
            final String uri,
            final File file,
            final boolean doMime,
            final FrostUploadItem ulItem)
    {
        if (file.length() == 0) {
            logger.log(Level.SEVERE, "Error: Can't upload empty file: "+file.getPath());
			JOptionPane.showMessageDialog(MainFrame.getInstance(),
							 "FcpInsert: File "+file.getPath()+" is empty!", // message
							 "Warning",
							 JOptionPane.WARNING_MESSAGE);
            return FcpResultPut.ERROR_RESULT;
        }

        try {
            FcpConnection connection;
            try {
                connection = FcpFactory.getFcpConnectionInstance();
            } catch (final ConnectException e1) {
                connection = null;
            }
            if( connection == null ) {
                return FcpResultPut.NO_CONNECTION_RESULT;
            }

            final FcpResultPut result = connection.putKeyFromFile(type, uri, file, false, doMime, ulItem);
            return result;

        } catch( final UnknownHostException e ) {
			logger.log(Level.SEVERE, "UnknownHostException", e);
        } catch( final Throwable e ) {
        	logger.log(Level.SEVERE, "Throwable", e);
        }
        return FcpResultPut.ERROR_RESULT;
    }

    public static String generateCHK(final File file) {

    	if (file.length() == 0) {
            logger.log(Level.SEVERE, "Error: Can't generate CHK for empty file: "+file.getPath());
			JOptionPane.showMessageDialog(MainFrame.getInstance(),
							 "FcpInsert: File "+file.getPath()+" is empty!", // message
							 "Warning",
							 JOptionPane.WARNING_MESSAGE);
            return null;
        }

        try {
            FcpConnection connection;
            try {
                connection = FcpFactory.getFcpConnectionInstance();
            } catch (final ConnectException e1) {
                connection = null;
            }
            if( connection == null ) {
                return null;
            }
            final String generatedCHK = connection.generateCHK(file);
            return generatedCHK;

        } catch( final UnknownHostException e ) {
			logger.log(Level.SEVERE, "UnknownHostException", e);
        } catch( final Throwable e ) {
        	logger.log(Level.SEVERE, "Throwable", e);
        }
        return null;
    }
}
