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
 * Read settings from frost.ini and store them
 */
public class SettingsClass
{
    private File settingsFile;
    private Hashtable settingsHash;
    private Defaults defaults = new Defaults();


    public void loadDefaults()
    {
        System.out.println("Loading default configuration");

        for( int i = 0; i < defaults.val.length; i++ )
        {
            String key = "";
            String value = "";

            for( int j =  0; j < defaults.val[i].length; j++ )
            {
                if( j == 0 )
                {
                    key = defaults.val[i][j];
                }
                else
                {
                    value = defaults.val[i][j];
                }
            }
            settingsHash.put(key, value);
        }
    }

    public String getDefValue(String key)
    {
        for( int i = 0; i < defaults.val.length; i++ )
        {
            if( defaults.val[i][0].equals(key) )
            {
                return defaults.val[i][1];
            }
        }
        return "";
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
            System.out.println("!!!substituted news for empty messageBase setting!!!");
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
            returnStrArr[i] = (String) strtok.nextToken();
        }
        return returnStrArr;
    }

    public boolean getBoolValue (String key)
    {
        String str = (String) settingsHash.get(key);
        try {
            if( str.equals("false") )
            {
                return false;
            }
            if( str.equals("true") )
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
        //if (value!="")
        settingsHash.put(key, value);
        //else {
        //settingsHash.put(key, "");
        //System.out.println("^^^^^^^^^^got an empty value");

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

    class Defaults
    {
        private String fs = System.getProperty("file.separator");
        private File fn = File.listRoots()[0];
        public String[][] val = {
            {"allowEvilBert", "false"},
            {"altEdit", fn + "path" + fs + "to" + fs + "editor" + " %f"},
            {"automaticUpdate", "true"},
            {"doBoardBackoff","false"},
            {"spamTreshold","5"},
            {"sampleInterval","5"},
            {"blockMessage", ""},
            {"blockMessageChecked","false"},
            {"blockMessageBody",""},
            {"blockMessageBodyChecked","false"},
            {"signedOnly","false"},
            {"goodOnly","false"},
            {"downloadDirectory", "downloads" + fs},
            {"downloadThreads", "3"},
            {"downloadingActivated", "true"},
            {"disableRequests", "false"},
            {"htl", "5"},
            {"htlMax", "30"},
            {"htlUpload", "12"},
            {"keyDownloadHtl", "25"},
            {"keyUploadHtl", "12"},
            {"lastUsedDirectory", "." + fs},
            {"maxAge", "20"},
            {"maxKeys", "200000"},
            {"maxMessageDisplay", "5"},
            {"maxMessageDownload", "3"},
            {"messageBase", "news"},
            {"nodeAddress", "127.0.0.1"},
            {"nodePort", "8481"},
            {"removeFinishedDownloads", "false"},
            {"reducedBlockCheck", "false"},
            {"searchAllBoards", "true"},
            {"splitfileDownloadThreads", "15"},
            {"splitfileUploadThreads", "6"},
            {"tofDownloadHtl", "23"},
            {"tofFontSize", "12.0"},
            {"tofTreeSelectedRow", "0"},
            {"tofUploadHtl", "21"},
            {"uploadThreads", "3"},
            {"uploadingActivated", "true"},
            {"useAltEdit", "false"},
            {"userName", "Anonymous"},
            {"audioExtension", ".mp3;.ogg;.wav;.mid;.mod"},
            {"videoExtension", ".mpeg;.mpg;.avi;.divx;.asf;.wmv;.rm"},
            {"documentExtension", ".doc;.txt;.tex;.pdf;.dvi"},
            {"executableExtension", ".exe;.vbs;.jar;.sh;.bat;.bin"},
            {"archiveExtension", ".zip;.rar;.jar;.gz;.arj;.ace;.bz;.tar"},
            {"imageExtension", ".jpeg;.jpg;.jfif;.gif;.png;.tif;.tiff;.bmp;.xpm"},
            {"doCleanUp","false"}
        };
    }
}

