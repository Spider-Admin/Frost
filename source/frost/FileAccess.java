/*
  FileAccess.java / File Access
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;

public class FileAccess
{
    /**
     * Writes a file to disk after opening a saveDialog window
     * @param parent The parent component, often 'this' can be used
     * @param conten The data to write to disk.
     * @param lastUsedDirectory The saveDialog starts at this directory
     * @param title The saveDialog gets this title
     */
    public static void saveDialog(Component parent, String content, String lastUsedDirectory, String title)
    {
        final JFileChooser fc = new JFileChooser(lastUsedDirectory);
        fc.setDialogTitle(title);
        fc.setFileHidingEnabled(true);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(false);

        int returnVal = fc.showSaveDialog(parent);
        if( returnVal == JFileChooser.APPROVE_OPTION )
        {
            File file = fc.getSelectedFile();
            if( file != null )
            {
                frame1.frostSettings.setValue("lastUsedDirectory", file.getParent());
                if( !file.isDirectory() )
                {
                    writeFile(content, file);
                }
            }
        }
    }

    /**
     * Reads a keyfile from disk and adds the keys to a map
     * @param source keyfile as String or as File
     * @param chk Map that will be used to add the keys
     * @param exchange the exchange flag of KeyClass will be set to this value
     */
    public static void readKeyFile(String source, Map chk, boolean exchange)
    {
        readKeyFile(new File(source), chk, exchange);
    }
    public static void readKeyFile(File source, Map chk, boolean exchange)
    {
        if( source.isFile() && source.length() > 0 )
        {
            BufferedReader f;
            String line = new String();
            String filename = new String();
            String size = new String();
            String date = new String();
            String key = new String();
            int counter = 0;

            try
            {
                f = new BufferedReader(new FileReader(source));
                while( (line = f.readLine()) != null )
                {
                    switch( counter ) {
                    case 0 :
                        filename = line;
                        break;
                    case 1 :
                        size = line;
                        break;
                    case 2 :
                        date = line;
                        break;
                    case 3 :
                        key = line;
                        KeyClass newKey = new KeyClass(key);
                        newKey.setFilename(filename);
                        newKey.setSize(size);
                        newKey.setDate(date);
                        newKey.setExchange(exchange);

                        if( newKey.isValid() )
                        {

                            KeyClass keyObject = (KeyClass)chk.get(key);
                            if( keyObject != null )
                            {
                                GregorianCalendar cal = newKey.getCal();
                                GregorianCalendar keyCal = keyObject.getCal();
                                keyObject.setExchange(exchange);

                                if( keyCal.before(cal) )
                                {
                                    // System.out.println("Updating date for: " + filename);
                                    keyObject.setDate(newKey.getDate());
                                }
                            }
                            else
                            {
                                if( chk.size() < frame1.frostSettings.getIntValue("maxKeys") )
                                {
                                    chk.put(key, newKey);
                                }
                            }
                        }
                        counter = -1;
                        break;
                    }
                    counter++;
                }
                f.close();
            }
            catch( IOException e )
            {
                System.out.println("Read Error: " + source);
            }
        }
    }

    public static void writeKeyFile(Map chk, String destination)
    {
        writeKeyFile(chk, new File(destination));
    }
    public static void writeKeyFile(Map chk, File destination)
    {
        File tmpFile = new File(destination.getPath() + ".tmp");
        FileWriter f1;

        try {
            f1 = new FileWriter(tmpFile);
            synchronized(chk)
            {
                Iterator i = chk.keySet().iterator();
                while( i.hasNext() )
                {
                    KeyClass key = (KeyClass)chk.get((String)i.next());
                    f1.write(key.getFilename() + "\r\n" + key.getSize() + "\r\n" + key.getDate() + "\r\n" + key.getKey() + "\r\n");
                }
            }
            f1.close();
        }
        catch( IOException e ) {
            System.out.println("Write Error: " + destination);
        }
        File oldFile = new File(destination.getPath() + ".old");
        oldFile.delete();
        destination.renameTo(oldFile);
        tmpFile.renameTo(destination);
    }

    /**
     * removes unwanted files from the keypool
     * @param keyPath the directory to clean
     */
    public static void cleanKeypool(String keyPath)
    {
        File[] chunks = (new File(keyPath)).listFiles();
        String date = DateFun.getExtendedDate();
        String fileSeparator = System.getProperty("file.separator");

        for( int i = 0; i < chunks.length; i++ )
        {
            if( chunks[i].isFile() )
            {
                // Remove 0 byte and tmp files
                if( chunks[i].length() == 0 ||
                    chunks[i].getName().endsWith(".tmp") //||
                    //chunks[i].getName().endsWith(".txt")
                    )
                    chunks[i].delete();

                // Remove keyfiles and their locks
                if( !chunks[i].getName().startsWith(date) && chunks[i].getName().endsWith(".idx") )
                    chunks[i].delete();
                if( !chunks[i].getName().startsWith(date) && chunks[i].getName().endsWith(".loc") )
                    chunks[i].delete();
            }
        }
    }

    /**
     * Writes a byte[] to disk
     * @param data the byte[] with the data to write
     * @param file the destination file
     */
    public static void writeByteArray(byte[] data, File file)
    {
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            fileOut.write(data);
            fileOut.close();
        }
        catch( IOException e ) {
            System.out.println("writeByteArray: " + e);
        }
    }

    /**
     * Reads a file and returns it's content in a byte[]
     * @param file the file to read
     * @return byte[] with the files content
     */
    public static byte[] readByteArray(String filename)
    {
        return readByteArray(new File(filename));
    }
    public static byte[] readByteArray(File file)
    {
        try {
            byte[] data = new byte[(int)file.length()];
            FileInputStream fileIn = new FileInputStream(file);
            int count = 0;
            int bytesRead = 0;
            int dataChunkLength = data.length;

            while( bytesRead < dataChunkLength )
            {
                count = fileIn.read(data, bytesRead, dataChunkLength - bytesRead);
                if( count < 0 )
                {
                    break;
                }
                else
                {
                    bytesRead++;
                }
            }
            fileIn.close();
            return data;
        }
        catch( IOException e ) {
            System.err.println(e);
        }
        return null;
    }

    /**
     * Reads a file and returns it contents in a String
     */
    public static String read(String path)
    {
        FileReader fr;
        StringBuffer content = new StringBuffer();
        int c;
        try
        {
            fr = new FileReader(path);
            while( (c = fr.read()) != -1 )
            {
                content.append((char)c);
            }
            fr.close();
        }
        catch( IOException e )
        {
            return("Read Error");
        }
        return content.toString();
    }

    /**Returns an ArrayList of File objects with all Directories and files*/
    public static ArrayList getAllEntries(File file, final String extension)
    {
        ArrayList files = new ArrayList();
        getAllFiles(file, extension, files);
        return files;
    }

    /**
     * Returns all files starting from given directory/file that have a given extension.
     */
    private static void getAllFiles(File file, String extension, ArrayList filesLst)
    {
        if( file != null )
        {
            if( file.isDirectory() )
            {
                File[] dirfiles = file.listFiles();
                if( dirfiles != null )
                {
                    for( int i = 0; i < dirfiles.length; i++ )
                    {
                        getAllFiles(dirfiles[i], extension, filesLst); // process recursive
                    }
                }
            }
            if( file.getName().endsWith(extension) )
            {
                filesLst.add( file );
            }
        }
    }

    /**
     * Writes zip file
     */
    public static void writeZipFile(String content, String entry, File file)
    {
        writeZipFile(content.getBytes(), entry, file);
    }
    public static void writeZipFile(byte[] content, String entry, File file)
    {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ZipOutputStream zos = new ZipOutputStream(fos);
            ZipEntry ze = new ZipEntry(entry);
            zos.putNextEntry(ze);
            zos.write(content);
            zos.closeEntry();
            zos.close();
        }
        catch( IOException e ) {
            System.out.println("files.writeZipFile: " + e);
        }
    }

    /**
     * Reads first zip file entry and returns content in a String
     */
    public static String readZipFile(String path)
    {
        return readZipFile(new File(path));
    }
    public static String readZipFile(File file)
    {
        if( !file.isFile() || file.length() == 0 )
            return null;

        StringBuffer sb = new StringBuffer();
        int bufferSize = 4096;
        try
        {
            FileInputStream fis = new FileInputStream(file);
            ZipInputStream zis = new ZipInputStream(fis);

            try {
                zis.getNextEntry();
                byte[] zipData = new byte[bufferSize];
                int len;
                boolean bur = true; // Buffer underrun
                int off = 0;
                int num = bufferSize;

                while( zis.available() == 1 )
                {
                    bur = true;
                    off = 0;
                    num = bufferSize;
                    while( bur && zis.available() == 1 )
                    {
                        len = zis.read(zipData, off, num);
                        off += len;
                        if( off >= bufferSize )
                            bur = false;
                        else
                            num = num - len;
                    }
                    sb.append(new String(zipData));
                }
                fis.close();
                zis.close();
            }
            catch( IOException e ) {
                System.out.println("files.readZipFile: " + e);
            }
        }
        catch( FileNotFoundException e ) {
            System.out.println("files.readZipFile: " + e);
        }
        return sb.toString();
    }

    /**
     * Reads file and returns a Vector of lines
     */
    public static Vector readLines(File file)
    {
        return readLines(file.getPath());
    }
    public static Vector readLines(String path)
    {
        BufferedReader f;
        String line;
        line = "";
        Vector data = new Vector();

        try {
            f = new BufferedReader(new FileReader(path));
            while( (line = f.readLine()) != null )
            {
                data.add(line.trim());
            }
            f.close();
        }
        catch( IOException e ) {
            System.out.println("Read Error: " + path);
        }
        return data;
    }

    /**
     * Reads a file and returns its contents in a String
     */
    public static String readFile(File file)
    {
        return readFile(file.getPath());
    }

    public static String readFile(String path)
    {
        BufferedReader f;
        String line = new String();
        StringBuffer stringBuffer = new StringBuffer();

        try
        {
            f = new BufferedReader(new FileReader(path));
            while( (line = f.readLine()) != null )
            {
                stringBuffer.append(line);
                stringBuffer.append("\r\n");
            }
            f.close();
        }
        catch( IOException e )
        {
            System.out.println("Read Error: " + path);
        }
        return stringBuffer.toString();
    }

    /**
     * Writes a file "file" to "path"
     */
    public static void writeFile(String content, File file)
    {
        writeFile(content, file.getPath());
    }
    public static void writeFile(String content, String filename)
    {
        FileWriter f1;
        try {
            f1 = new FileWriter(filename);
            f1.write(content);
            f1.close();
        }
        catch( IOException e ) {
            System.out.println("Write Error: " + filename);
        }
    }

    /**
     * Returns filenames in a directory
     */
    public static String[] getFilenames(String Path)
    {
        File FileObject = new File(Path);
        String[] filenames;

        if( FileObject.isDirectory() )
            filenames = FileObject.list();
        else
            filenames = new String[0];

        return filenames;
    }
}
