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

import java.awt.Color;
import java.io.*;
import java.util.*;

/**
 * Read settings from frost.ini and store them.
 *
 * TODO: why handle all in strings and convert between types? FIX!
 */
public class SettingsClass
{
    private File settingsFile;
    private Hashtable settingsHash;
    private Hashtable defaults = null;
    private final String fs = System.getProperty("file.separator");

    //Constructors
    public SettingsClass ()
    {
        settingsHash = new Hashtable();
        // the FIX config.dir
        settingsHash.put("config.dir", "config"+fs);
        String configFilename = "config" + fs + "frost.ini";
        settingsFile = new File(configFilename);
        loadDefaults();
        if( !readSettingsFile() )
        {
            writeSettingsFile();
        }
        // TODO: remove this, and use default or loaded value
        // i need to add because i used 'unsent' as name in former
        // CVS versions. So if you already have this settings: its wrong!
        // we need the '#' to separate internal boards in keypool from normal board.
        // normal board file names are not allowed to contain '#'.
        // once all users have the '#unsent#' folder configured, this can be removed
        // and you can change the directory location
        settingsHash.put("unsent.dir", "localdata"+fs+"unsent"+fs);
    }

    private String setSystemsFileSeparator(String path)
    {
        if( fs.equals("\\") )
        {
            path = path.replace( '/', File.separatorChar );
        }
        else if( fs.equals("/") )
        {
            path = path.replace( '\\', File.separatorChar );
        }

        // append fileseparator to end if needed
        if( path.endsWith( fs ) == false )
        {
            path = path + fs;
        }
        return path;
    }

    public String getDefaultValue(String key)
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

        if( settingsFile.exists() == false )
        {
            // try to get old frost.ini
            File oldIni = new File("frost.ini");
            if( oldIni.exists() && oldIni.length() > 0 )
            {
                oldIni.renameTo( settingsFile );
            }
        }

        try {
            settingsReader = new LineNumberReader(new FileReader(settingsFile));
        }
        catch( Exception e ) {
            System.out.println(settingsFile.getName() + " does not exist, will create it");
            return false;
        }
        try {
            while( (line = settingsReader.readLine()) != null )
            {
                line = line.trim();
                if( line.length() != 0 && line.startsWith("#") == false )
                {
                    StringTokenizer strtok = new StringTokenizer(line, "=");
                    String key = "";
                    String value = "";
                    Object objValue = value;
                    if( strtok.countTokens() >= 2 )
                    {
                        key = strtok.nextToken().trim();
                        value = strtok.nextToken().trim();
                        // to allow '=' in values
                        while( strtok.hasMoreElements() )
                        {
                            value += "=" + strtok.nextToken();
                        }
                        if( value.startsWith("type.color(") && value.endsWith(")") )
                        {
                            // this is a color
                            String rgbPart = value.substring( 11 , value.length()-1 );
                            StringTokenizer strtok2 = new StringTokenizer(rgbPart, ",");

                            if( strtok2.countTokens() == 3 )
                            {
                                try {
                                    int red, green, blue;
                                    red = Integer.parseInt(strtok2.nextToken().trim());
                                    green = Integer.parseInt(strtok2.nextToken().trim());
                                    blue = Integer.parseInt(strtok2.nextToken().trim());
                                    Color c = new Color(red, green, blue);
                                    objValue = c;
                                }
                                catch(Exception ex) {
                                    objValue = null;
                                }
                            }
                            else
                            {
                                objValue = null; // dont insert in settings, use default instead
                            }
                        }
                        // scan all path config values and set correct system file separator
                        else if( key.equals("unsent.dir") ||
                                 key.equals("temp.dir") ||
                                 key.equals("keypool.dir") )
                        {
                            value = setSystemsFileSeparator( value );
                            objValue = value;
                        }
                        else
                        {
                            // 'old' behaviour
                            objValue = value;
                        }
                        if( objValue != null )
                        {
                            settingsHash.put(key, objValue);
                        }
                    }
                }
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
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
            //System.out.println("!!! set messageBase to default 'news' !!!");
        }

        System.out.println("Read user configuration");
        return true;
    }

    public boolean writeSettingsFile()
    {
        PrintWriter settingsWriter = null;
        try {
            settingsWriter = new PrintWriter(new FileWriter(settingsFile));
        }
        catch( Exception e ) {
            e.printStackTrace();
            return false;
        }

        TreeMap sortedSettings = new TreeMap( settingsHash ); // sort the lines
        Iterator i = sortedSettings.keySet().iterator();
        while( i.hasNext() )
        {
            String key = (String)i.next();
            if( key.equals("config.dir") )
                continue; // do not save the config dir, its unchangeable

            String val = null;
            if( sortedSettings.get(key) instanceof Color )
            {
                Color c = (Color)sortedSettings.get(key);

                val = new StringBuffer().append("type.color(")
                .append( c.getRed() )
                .append( "," )
                .append( c.getGreen() )
                .append( "," )
                .append( c.getBlue() )
                .append( ")" ).toString();
            }
            else
            {
                val = sortedSettings.get(key).toString();
            }

            settingsWriter.println(key + "=" + val );
        }

        try {
            settingsWriter.close();
            System.out.println("Wrote configuration");
            return true;
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
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

    public Object getObjectValue(String key)
    {
        return settingsHash.get(key);
    }

    public String[] getArrayValue(String key)
    {
        String str = (String) settingsHash.get(key);
        if(str==null) return new String[0];
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
        if(str==null) return false;
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
        return getBoolValue(getDefaultValue(key));
    }

    public int getIntValue (String key)
    {
        String str = (String) settingsHash.get(key);
        if(str==null) return 0;
        int val = 0;
        try {
            val = Integer.parseInt(str);
        }
        catch( NumberFormatException e ) {
            return getIntValue(getDefaultValue(key));
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
        if(str==null) return val;
        try {
            val = Float.parseFloat(str);
        }
        catch( NumberFormatException e ) {
            return getFloatValue(getDefaultValue(key));
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

    public void setObjectValue(String key, Object value)
    {
        settingsHash.put(key, value);
    }

    /**
     * Contains all default values that are used if no value is found in .ini file.
     */
    public void loadDefaults()
    {
        defaults = new Hashtable();
        File fn = File.listRoots()[0];

        // DIRECTORIES
        defaults.put("keypool.dir", "keypool"+fs);
        defaults.put("unsent.dir", "localdata"+fs+"unsent"+fs);
        defaults.put("temp.dir", "localdata"+fs+"temp"+fs);

        defaults.put("allowEvilBert", "false");
        defaults.put("altEdit", fn + "path" + fs + "to" + fs + "editor" + " %f");
        defaults.put("automaticUpdate", "true");
        defaults.put("automaticUpdate.concurrentBoardUpdates", "5"); // no. of concurrent updating boards in auto update
        defaults.put("automaticUpdate.boardsMinimumUpdateInterval", "5"); // time in min to wait between start of updates for 1 board
        defaults.put("boardUpdateVisualization", "true");
        defaults.put("doBoardBackoff","false");
        defaults.put("spamTreshold","5");
        defaults.put("sampleInterval","5");
        defaults.put("blockMessage", "");
        defaults.put("blockMessageChecked","false");
        defaults.put("blockMessageBody","");
        defaults.put("blockMessageBodyChecked","false");
        defaults.put("signedOnly","false");
        defaults.put("hideBadMessages","false");
        defaults.put("hideCheckMessages","false");
        defaults.put("hideNAMessages","false");
        defaults.put("downloadDirectory", "downloads" + fs);
        defaults.put("downloadThreads", "3");
        defaults.put("downloadingActivated", "true");
//        defaults.put("downloadMethodLeastHtl", "false");
//        defaults.put("downloadMethodOneByOne", "true");
        defaults.put("downloadRestartFailedDownloads", "true");
        defaults.put("downloadEnableRequesting", "true");

        defaults.put("downloadRequestAfterTries", "5");
        defaults.put("downloadMaxRetries", "5");
        defaults.put("downloadWaittime", "5");
        
        defaults.put("downloadDecodeAfterEachSegment", "true");
        defaults.put("downloadTryAllSegments", "true");

        defaults.put("disableRequests", "false");
        defaults.put("disableDownloads","false");
//        defaults.put("htl", "5");
//        defaults.put("htlMax", "30");
        defaults.put("htlUpload", "21");
        defaults.put("keyDownloadHtl", "24");
        defaults.put("keyUploadHtl", "21");
        defaults.put("lastUsedDirectory", "." + fs);
        defaults.put("maxAge", "5");
        defaults.put("maxKeys", "500000");
        defaults.put("maxMessageDisplay", "10");
        defaults.put("maxMessageDownload", "3");
        defaults.put("messageBase", "news");
        defaults.put("nodeAddress", "127.0.0.1");
        defaults.put("nodePort", "8481");
        defaults.put("removeFinishedDownloads", "false");
        defaults.put("reducedBlockCheck", "false");
        defaults.put("searchAllBoards", "true");
        defaults.put("maxSearchResults", "10000");
        defaults.put("splitfileDownloadThreads", "30");
        defaults.put("splitfileUploadThreads", "15");
//        defaults.put("startRequestingAfterHtl", "10");
        defaults.put("tofDownloadHtl", "23");
        defaults.put("tofFontSize", "12.0");
        defaults.put("tofTreeSelectedRow", "0");
        defaults.put("tofUploadHtl", "21");
        defaults.put("uploadThreads", "3");
	defaults.put("uploadBatchSize","100");
	defaults.put("indexFileRedundancy","1");
        defaults.put("uploadingActivated", "true");
	defaults.put("signUploads","true");
	defaults.put("helpFriends","true");
	defaults.put("hideBadFiles","true");
	defaults.put("hideAnonFiles","true");
        defaults.put("useAltEdit", "false");
        defaults.put("userName", "Anonymous");
        defaults.put("audioExtension", ".mp3;.ogg;.wav;.mid;.mod");
        defaults.put("videoExtension", ".mpeg;.mpg;.avi;.divx;.asf;.wmv;.rm;.ogm");
        defaults.put("documentExtension", ".doc;.txt;.tex;.pdf;.dvi");
        defaults.put("executableExtension", ".exe;.vbs;.jar;.sh;.bat;.bin");
        defaults.put("archiveExtension", ".zip;.rar;.jar;.gz;.arj;.ace;.bz;.tar");
        defaults.put("imageExtension", ".jpeg;.jpg;.jfif;.gif;.png;.tif;.tiff;.bmp;.xpm");
        defaults.put("doCleanUp","false");
        defaults.put("autoSaveInterval","15");

        defaults.put("boardUpdatingNonSelectedBackgroundColor", new Color(233,233,233) );//"type.color(233,233,233)"
        defaults.put("boardUpdatingSelectedBackgroundColor", new Color(137,137,191) );//"type.color(137,137,191)

        settingsHash.putAll(defaults);
    }
}

