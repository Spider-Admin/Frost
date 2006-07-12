/*
  FileAccess.java / File Access
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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

import java.awt.*;
import java.io.*;
import java.nio.channels.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;
import java.util.zip.*;

import javax.swing.*;

public class FileAccess {

    private static Logger logger = Logger.getLogger(FileAccess.class.getName());
    
    public static File createTempFile(String prefix, String suffix) {
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(prefix, suffix, new File(Core.frostSettings.getValue("temp.dir")));
        } catch( Throwable ex ) {
        }
        if( tmpFile == null ) {
            do {
                tmpFile = new File(
                        Core.frostSettings.getValue("temp.dir")+
                        prefix+
                        System.currentTimeMillis()+
                        suffix);
            } while(tmpFile.exists());
        }
        return tmpFile;
    }

    /**
     * Writes a file to disk after opening a saveDialog window
     * @param parent The parent component, often 'this' can be used
     * @param conten The data to write to disk.
     * @param lastUsedDirectory The saveDialog starts at this directory
     * @param title The saveDialog gets this title
     */
    public static void saveDialog(Component parent, String content, String lastUsedDirectory,   String title) {

        final JFileChooser fc = new JFileChooser(lastUsedDirectory);
        fc.setDialogTitle(title);
        fc.setFileHidingEnabled(true);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(false);

        int returnVal = fc.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file != null) {
                Core.frostSettings.setValue("lastUsedDirectory", file.getParent());
                if (!file.isDirectory()) {
                    writeFile(content, file, "UTF-8");
                }
            }
        }
    }

    /**
     * Reads a file and returns it's content in a byte[]
     * @param file the file to read
     * @return byte[] with the files content
     */
    public static byte[] readByteArray(String filename) {
        return readByteArray(new File(filename));
    }

    public static byte[] readByteArray(File file) {
        try {
            byte[] data = new byte[(int)file.length()];
            FileInputStream fileIn = new FileInputStream(file);
            DataInputStream din = new DataInputStream(fileIn);
            din.readFully(data);
            fileIn.close();
            return data;
        } catch( IOException e ) {
            logger.log(Level.SEVERE, "Exception thrown in readByteArray(File file)", e);
        }
        return null;
    }

    /**
     * Returns all files starting from given directory/file that have a given extension.
     */
    public static ArrayList getAllEntries(File file, final String extension) {
        ArrayList files = new ArrayList();
        getAllFiles(file, extension, files);
        return files;
    }

    /**
     * Returns all files starting from given directory/file that have a given extension.
     */
    private static void getAllFiles(File file, String extension, ArrayList filesLst) {
        if( file != null ) {
            if( file.isDirectory() ) {
                File[] dirfiles = file.listFiles();
                if( dirfiles != null ) {
                    for( int i = 0; i < dirfiles.length; i++ ) {
                        getAllFiles(dirfiles[i], extension, filesLst); // process recursive
                    }
                }
            }
            if( extension.length() == 0 || file.getName().endsWith(extension) ) {
                filesLst.add(file);
            }
        }
    }

    /**
     * Writes zip file
     */
    public static boolean writeZipFile(byte[] content, String entry, File file) {
        if (content == null || content.length == 0) {
            Exception e = new Exception();
            e.fillInStackTrace();
            logger.log(Level.SEVERE, "Tried to zip an empty file!  Send this output to a dev"+
                                        " and describe what you were doing.", e);
            return false;
        }
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
            zos.setLevel(9); // maximum compression
            ZipEntry ze = new ZipEntry(entry);
            ze.setSize(content.length);
            zos.putNextEntry(ze);
            zos.write(content);
            zos.flush(); //do this before closeEntry()
            zos.closeEntry();
            zos.close();
            return true;
        } catch( Throwable e ) {
            logger.log(Level.SEVERE, "Exception thrown in writeZipFile(byte[] content, String entry, File file)", e);
            return false;
        }
    }

    /**
     * Reads first zip file entry and returns content in a byte[].
     */
    public static byte[] readZipFileBinary(File file) {
        if( !file.isFile() || file.length() == 0 ) {
            return null;
        }

        final int bufferSize = 4096;
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(file));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            zis.getNextEntry();

            byte[] zipData = new byte[bufferSize];
            while( true ) {
                int len = zis.read(zipData);
                if( len < 0 ) {
                    break;
                }
                out.write(zipData, 0, len);
            }
            zis.close();
            return out.toByteArray();

        } catch( FileNotFoundException e ) {
            logger.log(Level.SEVERE, "Exception thrown in readZipFile(String path)", e);
        }
        catch( IOException e ) {
            try { if( zis != null) zis.close(); } catch(Throwable t) { }
            logger.log(Level.SEVERE, "Exception thrown in readZipFile(String path) \n" +
                                     "Offending file saved as badfile.zip, send to a dev for analysis", e);
            copyFile(file.getPath(), "badfile.zip");
        }
        return null;
    }

    /**
     * Reads file and returns a List of lines.
     * Encoding "ISO-8859-1" is used.
     */
    public static List readLines(File file) {
        return readLines(file, "ISO-8859-1");
    }
    /**
     * Reads a File and returns a List of lines
     */
    public static List readLines(File file, String encoding) {
        List result = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            result = readLines(fis, encoding);
            fis.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception thrown in readLines(File file, String encoding)", e);
        }
        return result;
    }
    
    /**
     * Reads an InputStream and returns a List of lines.
     */
    public static ArrayList readLines(InputStream is, String encoding) {
        String line;
        ArrayList data = new ArrayList();
        try {
            InputStreamReader iSReader = new InputStreamReader(is, encoding);
            BufferedReader reader = new BufferedReader(iSReader);
            while( (line = reader.readLine()) != null ) {
                data.add(line.trim());
            }
            reader.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception thrown in readLines(InputStream is, String encoding)", e);
        }
        return data;
    }


    /**
     * Reads a file and returns its contents in a String
     */
    public static String readFile(File file) {
        String line;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            BufferedReader f = new BufferedReader(new FileReader(file));
            while( (line = f.readLine()) != null ) {
                stringBuffer.append(line).append("\n");
            }
            f.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception thrown in readFile(String path)", e);
        }
        return stringBuffer.toString();
    }

    /**
     * Reads a file, line by line, and adds a \n after each one. You can specify the encoding to use when reading.
     *
     * @param path
     * @param encoding
     * @return the contents of the file
     */
    public static String readFile(File file, String encoding) {
        String line;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            InputStreamReader iSReader = new InputStreamReader(new FileInputStream(file), encoding);
            BufferedReader reader = new BufferedReader(iSReader);
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception thrown in readFile(String path, String encoding)", e);
        }
        return stringBuffer.toString();
    }

    /**
     * Writes a file "file" to "path"
     */
    public static boolean writeFile(String content, String filename) {
        return writeFile(content, new File(filename));
    }

    /**
     * Writes a file "file" to "path", being able to specify the encoding
     */
    public static boolean writeFile(String content, String filename, String encoding) {
        return writeFile(content, new File(filename), encoding);
    }

    /**
     * Writes a text file in ISO-8859-1 encoding.
     */
    public static boolean writeFile(String content, File file) {
        OutputStreamWriter f1 = null;
        try {
            // write the file in single byte codepage (default could be a DBCS codepage)
            try {
                f1 = new OutputStreamWriter(new FileOutputStream(file), "ISO-8859-1");
            } catch(UnsupportedEncodingException e) {
                f1 = new FileWriter(file);
            }
            f1.write(content);
            f1.close();
            return true;
        } catch( Throwable e ) {
            logger.log(Level.SEVERE, "Exception thrown in writeFile(String content, File file)", e);
            if( f1 != null ) {
                try { f1.close(); } catch(Throwable t) {}
            }
        }
        return false;
    }

    public static boolean writeFile(byte[] content, File file) {
        try {
            FileOutputStream s = new FileOutputStream(file);
            s.write(content);
            s.close();
            return true;
        } catch( Throwable e ) {
            logger.log(Level.SEVERE, "Exception thrown in writeFile(byte[] content, File file)", e);
        }
        return false;
    }

    /**
     * Writes a text file in specified encoding. Converts line separators to target platform.
     */
    public static boolean writeFile(String content, File file, String encoding) {
        OutputStreamWriter outputWriter = null;
        try {
            outputWriter = new OutputStreamWriter(new FileOutputStream(file), encoding);

            BufferedReader inputReader = new BufferedReader(new StringReader(content));
            String lineSeparator = System.getProperty("line.separator");
            String line = inputReader.readLine();

            while (line != null) {
                outputWriter.write(line + lineSeparator);
                line = inputReader.readLine();
            }

            outputWriter.close();
            inputReader.close();
            return true;
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Exception thrown in writeFile(String content, File file, String encoding)", e);
            if( outputWriter != null ) {
                try { outputWriter.close(); } catch(Throwable t) {}
            }
        }
        return false;
    }

    /**
     * Deletes the given directory and ALL FILES/DIRS IN IT !!!
     * USE CAREFUL !!!
     */
    public static boolean deleteDir(File dir) {
        if( dir.isDirectory() ) {
            String[] children = dir.list();
            for( int i = 0; i < children.length; i++ ) {
                boolean success = deleteDir(new File(dir, children[i]));
                if( !success ) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }

    /**
     * This method copies the contents of one file to another. If the destination file didn't exist, it is created. If
     * it did exist, its contents are overwritten.
     *
     * @param sourceName
     *            name of the source file
     * @param destName
     *            name of the destination file
     */
    public static boolean copyFile(String sourceName, String destName) {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        boolean wasOk = false;
        try {
            sourceChannel = new FileInputStream(sourceName).getChannel();
            destChannel = new FileOutputStream(destName).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            wasOk = true;
        } catch (Throwable exception) {
            logger.log(Level.SEVERE, "Exception in copyFile", exception);
        } finally {
            if (sourceChannel != null) {
                try { sourceChannel.close(); } catch (IOException ex) {}
            }
            if (destChannel != null) {
                try { destChannel.close(); } catch (IOException ex) {}
            }
        }
        return wasOk;
    }


    /**
     * This method compares 2 file byte by byte.
     * Returns true if they are equals, or false.
     */
    public static boolean compareFiles(File f1, File f2) {
        try {
            BufferedInputStream s1 = new BufferedInputStream(new FileInputStream(f1));
            BufferedInputStream s2 = new BufferedInputStream(new FileInputStream(f2));
            int i1, i2;
            boolean equals = false;
            while(true) {
                i1 = s1.read();
                i2 = s2.read();
                if( i1 != i2 ) {
                    equals = false;
                    break;
                }
                if( i1 < 0 && i2 < 0 ) {
                    equals = true; // both at EOF
                    break;
                }
            }
            s1.close();
            s2.close();
            return equals;
        } catch(Throwable e) {
            return false;
        }
    }

    /**
     * Copys a file from the jar file to disk
     * @param resource This is the file's name in the jar
     * @param file This is the destination file
     */
    public static void copyFromResource(String resource, File file) throws IOException {
        if (!file.isFile()) {
            InputStream input = MainFrame.class.getResourceAsStream(resource);
            FileOutputStream output = new FileOutputStream(file);
            byte[] data = new byte[4096];
            int bytesRead;
            while ((bytesRead = input.read(data)) != -1) {
                output.write(data, 0, bytesRead);
            }
            input.close();
            output.close();
        }
    }
}
