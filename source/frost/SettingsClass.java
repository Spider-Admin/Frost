/*
  SettingsClass.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>
  This file contributed by Stefan Majewski <e9926279@stud3.tuwien.ac.at>

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

package frost;

import java.util.*;
import java.io.*;

/**
 * Read settings from frost.ini and store them.
 */
public class SettingsClass
{
    private File settingsFile;
    private Hashtable settingsHash;
    private Hashtable defaults = null;

    //Constructors
    public SettingsClass ()
    {
        settingsFile = new File ("frost.ini");
        settingsHash = new Hashtable(); //limited to 100 entries!
        loadDefaults();
        if( !readSettingsFile() )
        {
            writeSettingsFile();
        }
        setValue("keypool.dir", frame1.keypool);
    }

    public SettingsClass (File settingsFile)
    {
        this.settingsFile = settingsFile;
        settingsHash = new Hashtable(); //limited to 100 entries!
        loadDefaults();
        if( !readSettingsFile() )
        {
            writeSettingsFile();
        }
    }

    public String getDefValue(String key)
    {
        String val = (String)defaults.get(key);
        if( val == null )
        {
            val = "";
        }
        return val;
    }

    public boolean readSettingsFile()
    {
        LineNumberReader settingsReader = null;
        String line;

        try {
            settingsReader = new LineNumberReader(new FileReader (settingsFile));
        }
        catch( Exception e ) {
            System.out.println(settingsFile.getName() + " does not exist, will create it");
            return false;
        }
        try {
            while( (line = settingsReader.readLine()) != null )
            {
                if( line.length() != 0 && line.indexOf("#") != 0 )
                {
                    StringTokenizer strtok = new StringTokenizer(line, "=");
                    String key = "";
                    String value = "";
                    if( strtok.countTokens() >= 2 )
                    {
                        key = strtok.nextToken();
                        value = strtok.nextToken();
                        // to allow '=' in values
                        while( strtok.hasMoreElements() )
                        {
                            value += "=" + strtok.nextToken();
                        }
                        settingsHash.put(key, value);
                    }
                }
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
            return false;
        }

        try {
            settingsReader.close();
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
        if( this.getValue("messageBase").equals("") )
        {
            this.setValue("messageBase",  "news");
            System.out.println("!!! set messageBase to default 'news' !!!");
        }
        System.out.println("Read user configuration");
        return false;
    }

    public boolean writeSettingsFile ()
    {
        PrintWriter settingsWriter = null;
        Enumeration settingsEnum = settingsHash.keys();

        try {
            settingsWriter = new PrintWriter(new FileWriter(settingsFile));
        }
        catch( Exception e ) {
            e.printStackTrace();
            return false;
        }

        while( settingsEnum.hasMoreElements() )
        {
            String key = (String) settingsEnum.nextElement();
            settingsWriter.println(key + "=" + settingsHash.get(key));
        }

        try {
            settingsWriter.close();
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
        System.out.println("Wrote configuration");
        return false;
    }

    /* Get the values from the Hash
     * Functions will return null if nothing appropriate
     * is found or the settings are wrongly formatted or
     * any other conceivable exception.
     */
    public String getValue (String key)
    {
        return(String) settingsHash.get(key);
    }

    public String[] getArrayValue (String key)
    {
        String str = (String) settingsHash.get(key);
        StringTokenizer strtok = new StringTokenizer(str, ";");
        String [] returnStrArr = new String[strtok.countTokens()];

        for( int i = 0; strtok.hasMoreElements(); i++ )
        {
            returnStrArr[i] = (String)strtok.nextToken();
        }
        return returnStrArr;
    }

    public boolean getBoolValue (String key)
    {
        String str = (String) settingsHash.get(key);
        try {
            if( str.toLowerCase().equals("false") )
            {
                return false;
            }
            if( str.toLowerCase().equals("true") )
            {
                return true;
            }
        }
        catch( NullPointerException e ) {
            return false;
        }
        return getBoolValue(getDefValue(key));
    }

    public int getIntValue (String key)
    {
        String str = (String) settingsHash.get(key);
        int val = 0;
        try {
            val = Integer.parseInt(str);
        }
        catch( NumberFormatException e ) {
            return getIntValue(getDefValue(key));
        }
        catch( Exception e ) {
            return 0;
        }
        return val;
    }

    public float getFloatValue (String key)
    {
        float val = 0.0f;
        String str = (String) settingsHash.get(key);
        try {
            val = Float.parseFloat(str);
        }
        catch( NumberFormatException e ) {
            return getFloatValue(getDefValue(key));
        }
        catch( Exception e ) {
            return 0.0f;
        }
        return val;
    }

    public void setValue (String key, String value)
    {
        settingsHash.put(key, value);
    }
    public void setValue(String key, Integer value)
    {
        this.setValue(key, String.valueOf(value));
    }
    public void setValue(String key, int value)
    {
        this.setValue(key, String.valueOf(value));
    }
    public void setValue(String key, Float value)
    {
        this.setValue(key, String.valueOf(value));
    }
    public void setValue(String key, float value)
    {
        this.setValue(key, String.valueOf(value));
    }
    public void setValue(String key, Boolean value)
    {
        this.setValue(key, String.valueOf(value));
    }
    public void setValue(String key, boolean value)
    {
        this.setValue(key, String.valueOf(value));
    }

    /**
     * Contains all default values that are used if no value is found in .ini file.
     */
    public void loadDefaults()
    {
        defaults = new Hashtable();

        String fs = System.getProperty("file.separator");
        File fn = File.listRoots()[0];

        defaults.put("allowEvilBert", "false");
        defaults.put("altEdit", fn + "path" + fs + "to" + fs + "editor" + " %f");
        defaults.put("automaticUpdate", "true");
        defaults.put("automaticUpdate.concurrentBoardUpdates", "5"); // no. of concurrent updating boards in auto update
        defaults.put("automaticUpdate.boardsMinimumUpdateInterval", "5"); // time in min to wait between start of updates for 1 board
        defaults.put("doBoardBackoff","false");
        defaults.put("spamTreshold","5");
        defaults.put("sampleInterval","5");
        defaults.put("blockMessage", "");
        defaults.put("blockMessageChecked","false");
        defaults.put("blockMessageBody","");
        defaults.put("blockMessageBodyChecked","false");
        defaults.put("signedOnly","false");
        defaults.put("goodOnly","false");
        defaults.put("downloadDirectory", "downloads" + fs);
        defaults.put("downloadThreads", "3");
        defaults.put("downloadingActivated", "true");
        defaults.put("disableRequests", "false");
        defaults.put("htl", "5");
        defaults.put("htlMax", "30");
        defaults.put("htlUpload", "21");
        defaults.put("keyDownloadHtl", "24");
        defaults.put("keyUploadHtl", "21");
        defaults.put("lastUsedDirectory", "." + fs);
        defaults.put("maxAge", "20");
        defaults.put("maxKeys", "200000");
        defaults.put("maxMessageDisplay", "5");
        defaults.put("maxMessageDownload", "3");
        defaults.put("messageBase", "news");
        defaults.put("nodeAddress", "127.0.0.1");
        defaults.put("nodePort", "8481");
        defaults.put("removeFinishedDownloads", "false");
        defaults.put("reducedBlockCheck", "false");
        defaults.put("searchAllBoards", "true");
        defaults.put("splitfileDownloadThreads", "15");
        defaults.put("splitfileUploadThreads", "6");
        defaults.put("tofDownloadHtl", "23");
        defaults.put("tofFontSize", "12.0");
        defaults.put("tofTreeSelectedRow", "0");
        defaults.put("tofUploadHtl", "21");
        defaults.put("uploadThreads", "3");
        defaults.put("uploadingActivated", "true");
        defaults.put("useAltEdit", "false");
        defaults.put("userName", "Anonymous");
        defaults.put("audioExtension", ".mp3;.ogg;.wav;.mid;.mod");
        defaults.put("videoExtension", ".mpeg;.mpg;.avi;.divx;.asf;.wmv;.rm");
        defaults.put("documentExtension", ".doc;.txt;.tex;.pdf;.dvi");
        defaults.put("executableExtension", ".exe;.vbs;.jar;.sh;.bat;.bin");
        defaults.put("archiveExtension", ".zip;.rar;.jar;.gz;.arj;.ace;.bz;.tar");
        defaults.put("imageExtension", ".jpeg;.jpg;.jfif;.gif;.png;.tif;.tiff;.bmp;.xpm");
        defaults.put("doCleanUp","false");

        settingsHash.putAll(defaults);
    }
}

