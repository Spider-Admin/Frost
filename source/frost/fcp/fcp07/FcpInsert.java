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
import frost.fileTransfer.upload.*;

/**
 * This class provides methods to insert data into freenet.
 */
public class FcpInsert
{
	private static Logger logger = Logger.getLogger(FcpInsert.class.getName());

	/*
	 * ClientPut
URI=KSK@fuckkkk
DataLength=10
Identifier=I
Verbosity=0
MaxRetries=1
Daat
djaskdjalsdj
oio
PutSuccessful
Identifier=I
URI=freenet:KSK@fuckkkk
EndMessage

ClientPut
URI=KSK@fuckkkk
DataLength=10
Identifier=I
Verbosity=0
MaxRetries=1
Data
djalskjdlakjsd
daksjldaksjd
PutFailed
Code=9
Identifier=I
ExpectedURI=freenet:KSK@fuckkkk
CodeDescription=Insert collided with different, pre-existing data at the same key
EndMessage

	 */

    private static String[] keywords = {"Success",
                                        "RouteNotFound",
                                        "KeyCollision",
                                        "SizeError",
                                        "DataNotFound",
                                        "PutSuccessful",
                                        "PutFailed"};

    private static final String[] ERROR = new String[] {"Error","Error"};

    private static String[] result(String text) {

        logger.info("*** FcpInsert.result: text='"+text+"'");
        System.out.println("*** FcpInsert.result: text='"+text+"'");

        if( text == null || text.length() == 0 ) {
            return ERROR;
        }

        String[] result = {"Error", "Error"};
        // check if the keyword returned by freenet is a known keyword
        for( int i = 0; i < keywords.length; i++ ) {
            if( text.indexOf(keywords[i]) != -1 ) {
                result[0] = keywords[i];
            }
        }
        // check if the returned text contains the computed CHK key (key generation)
        if( text.indexOf("CHK@") > -1 && text.indexOf("EndMessage") > -1 ) {
            result[1] = text.substring(text.lastIndexOf("CHK@"), text.lastIndexOf("EndMessage")).trim();
        }
        if ( result[0] == "PutFailed" && text.indexOf("Code=9") > -1 ) {
        	result[0] = "KeyCollision";
        }

        return result;
    }

    /**
     * Inserts a file into freenet.
     * The maximum file size for a KSK/SSK direct insert is 32kb! (metadata + data!!!)
     * The uploadItem is needed for FEC splitfile puts,
     * for inserting e.g. the pubkey.txt file set it to null.
     * Same for uploadItem: if a non-uploadtable file is uploaded, this is null.
     */
    public static String[] putFile(String uri,
                                   File file,
                                   FrostUploadItem ulItem)
    {
        if (file.length() == 0) {
            logger.log(Level.SEVERE, "Error: Can't upload empty file: "+file.getPath());
            System.out.println("Error: Can't upload empty file: "+file.getPath());
			JOptionPane.showMessageDialog(MainFrame.getInstance(),
							 "FcpInsert: File "+file.getPath()+" is empty!", // message
							 "Warning",
							 JOptionPane.WARNING_MESSAGE);
            return ERROR;
        }

        try {
            FcpConnection connection;
            try {
                connection = FcpFactory.getFcpConnectionInstance();
            } catch (ConnectException e1) {
                connection = null;
            }
            if( connection == null ) {
                return ERROR;
            }

            String output = connection.putKeyFromFile(uri, file, false);

            return result(output);

        } catch( UnknownHostException e ) {
			logger.log(Level.SEVERE, "UnknownHostException", e);
        } catch( Throwable e ) {
        	logger.log(Level.SEVERE, "Throwable", e);
        }
        return ERROR;
    }

    public static String generateCHK(File file) {

    	if (file.length() == 0) {
            logger.log(Level.SEVERE, "Error: Can't generate CHK for empty file: "+file.getPath());
            System.out.println("Error: Can't generate CHK for empty file: "+file.getPath());
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
            } catch (ConnectException e1) {
                connection = null;
            }
            if( connection == null ) {
                return null;
            }
            String generatedCHK = connection.generateCHK(file);
            return generatedCHK;

        } catch( UnknownHostException e ) {
			logger.log(Level.SEVERE, "UnknownHostException", e);
        } catch( Throwable e ) {
        	logger.log(Level.SEVERE, "Throwable", e);
        }
        return null;
    }
}
