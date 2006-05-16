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
import java.util.*;
import java.util.logging.*;

import javax.swing.*;

import frost.*;
import frost.fcp.*;
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

    private static Map putKeywords = null;
    
    private static Map getKeywords() {
        if( putKeywords == null ) {
            // fill a map with possible keyword to result assignments
            putKeywords = new HashMap();
            putKeywords.put("Success", new Integer(FcpResultPut.Success));
            putKeywords.put("RouteNotFound", new Integer(FcpResultPut.Retry));
            putKeywords.put("KeyCollision", new Integer(FcpResultPut.KeyCollision));
            putKeywords.put("SizeError", new Integer(FcpResultPut.Error));
            putKeywords.put("DataNotFound", new Integer(FcpResultPut.Error));
            putKeywords.put("PutSuccessful", new Integer(FcpResultPut.Success));
            putKeywords.put("PutFailed", new Integer(FcpResultPut.Error));
        }
        return putKeywords;
    }

    private static FcpResultPut result(String text) {

        logger.info("*** FcpInsert.result: text='"+text+"'");
//        System.out.println("*** FcpInsert.result: text='"+text+"'");

        if( text == null || text.length() == 0 ) {
            return FcpResultPut.ERROR_RESULT;
        }

        int result = FcpResultPut.Error;
        
        // check if the keyword returned by freenet is a known keyword
        for(Iterator i=getKeywords().keySet().iterator(); i.hasNext(); ) {
            String keyword = (String)i.next();
            if( text.indexOf(keyword) >= 0 ) {
                if( keyword.equals("PutFailed") && text.indexOf("Code=9") > -1 ) {
                    result = FcpResultPut.KeyCollision;
                    break;
                } else if( keyword.equals("PutFailed") && text.indexOf("Code=5") > -1 ) {
                    // "route not found". retry, finally we maybe get Code=8 ("route really not found")
                    result = FcpResultPut.Retry;
                    break;
                } else {
                    result = ((Integer)getKeywords().get(keyword)).intValue();
                    break;
                }
            }
        }
        
        String chkKey = null;
        
        // check if the returned text contains the computed CHK key (key generation)
        int pos = text.indexOf("CHK@"); 
        if( pos > -1 ) {
            chkKey = text.substring(pos);
            chkKey = chkKey.substring(0, chkKey.indexOf('\n'));
        }
//        if( text.indexOf("CHK@") > -1 && text.indexOf("EndMessage") > -1 ) {
//            chkKey = text.substring(text.lastIndexOf("CHK@"), text.lastIndexOf("EndMessage")).trim();
//        }
        
        return new FcpResultPut(result, chkKey);
    }

    /**
     * Inserts a file into freenet.
     * The maximum file size for a KSK/SSK direct insert is 32kb! (metadata + data!!!)
     * The uploadItem is needed for FEC splitfile puts,
     * for inserting e.g. the pubkey.txt file set it to null.
     * Same for uploadItem: if a non-uploadtable file is uploaded, this is null.
     */
    public static FcpResultPut putFile(String uri,
                                   File file,
                                   FrostUploadItem ulItem)
    {
        if (file.length() == 0) {
            logger.log(Level.SEVERE, "Error: Can't upload empty file: "+file.getPath());
//            System.out.println("Error: Can't upload empty file: "+file.getPath());
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
            } catch (ConnectException e1) {
                connection = null;
            }
            if( connection == null ) {
                return FcpResultPut.ERROR_RESULT;
            }

            String output = connection.putKeyFromFile(uri, file, false);

            return result(output);

        } catch( UnknownHostException e ) {
			logger.log(Level.SEVERE, "UnknownHostException", e);
        } catch( Throwable e ) {
        	logger.log(Level.SEVERE, "Throwable", e);
        }
        return FcpResultPut.ERROR_RESULT;
    }

    public static String generateCHK(File file) {

    	if (file.length() == 0) {
            logger.log(Level.SEVERE, "Error: Can't generate CHK for empty file: "+file.getPath());
//            System.out.println("Error: Can't generate CHK for empty file: "+file.getPath());
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
