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
import java.awt.Component;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.swing.JFileChooser;

import org.w3c.dom.Document;

import frost.messages.*;
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
                    //chunks[i].getName().endsWith(".xml")
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
            fileOut.flush();
            fileOut.close();
        }
        catch( IOException e ) {
            Core.getOut().println("writeByteArray: " + e);
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
            DataInputStream din = new DataInputStream(fileIn);
            din.readFully(data);
        /*    int count = 0;
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
            }*/
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
        	e.printStackTrace(Core.getOut());
            return("Read Error");
        }
        return content.toString();
    }

    /**
     * Returns all files starting from given directory/file that have a given extension.
     */
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
            if( extension.length() == 0 ||
                file.getName().endsWith(extension) )
            {
                filesLst.add( file );
            }
        }
    }

    /**
     * Writes zip file
     */
    public static void writeZipFile(byte[] content, String entry, File file)
    {
    	if (content.length==0) {
    		Exception e = new Exception();
    		e.fillInStackTrace();
    		Core.getOut().println("Tried to zip an empty file!  Send this output to a dev"+
    									" and describe what you were doing.");
    		e.printStackTrace(Core.getOut());
    		return;
    	}
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ZipOutputStream zos = new ZipOutputStream(fos);
            ZipEntry ze = new ZipEntry(entry);
            ze.setSize(content.length);
            zos.putNextEntry(ze);
            zos.write(content);
			zos.flush(); //do this before closeEntry()
            zos.closeEntry();
            zos.close();
            fos.close();
        }
        catch( IOException e ) {
            Core.getOut().println("files.writeZipFile: " + e);
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
        byte[] content = readZipFileBinary( file );
        if( content != null )
        {
            return new String(content);
        }
        return null;
    }
    
    public static byte[] readZipFileBinary(File file)
    {
		if( !file.isFile() || file.length() == 0 )
			return null;

		int bufferSize = 4096;
		try
		{
			FileInputStream fis = new FileInputStream(file);
			ZipInputStream zis = new ZipInputStream(fis);

			try {
				ArrayList chunks = new ArrayList();
				ArrayList chunkLen = new ArrayList();
                
				zis.getNextEntry();
				byte[] zipData = null;
				int len;
				boolean bur = true; // Buffer underrun
				int off = 0;
				int num = bufferSize;

				while( zis.available() == 1 )
				{
					bur = true;
					off = 0;
					num = bufferSize;
					zipData = new byte[bufferSize];
					while( bur && zis.available() == 1 )
					{
						len = zis.read(zipData, off, num);
						off += len;
						if( off >= bufferSize )
							bur = false;
						else
							num = num - len;
					}
					// first add allo chunks and assemble them later
					chunks.add(zipData);
					chunkLen.add( new Integer(off));
				}
				fis.close();
				zis.close();
				// now put all chunks into 1 byte array
				int overallLen = 0;
				Iterator i = chunkLen.iterator();
				while(i.hasNext())
				{
					Integer aChunkLen = (Integer)i.next();
					overallLen += aChunkLen.intValue();
				}
				overallLen++;
				byte[] resultbytes = new byte[overallLen];
				int actOffset = 0;
				for(int x=0; x<chunks.size(); x++)
				{
					byte[] aChunk = (byte[])chunks.get(x);
					int aChunkLen = ((Integer)chunkLen.get(x)).intValue();
                    
                    if (x==chunks.size()-1) aChunkLen++;
					System.arraycopy(aChunk, 0, resultbytes, actOffset, aChunkLen);
					actOffset += aChunkLen;
				}
                
				return resultbytes;
			}
			catch( IOException e ) {
				e.printStackTrace(Core.getOut());
				Core.getOut().println("offending file saved as badfile.zip, send to a dev for analysis");
				File badFile = new File("badfile.zip");
				file.renameTo(badFile);
			}
		}
		catch( FileNotFoundException e ) {
			Core.getOut().println("files.readZipFile: " + e);
		}
		return null;
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
        	e.printStackTrace(Core.getOut());
            Core.getOut().println("Read Error: " + path);
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
        	e.printStackTrace(Core.getOut());
            Core.getOut().println("Read Error: " + path);
        }
        return stringBuffer.toString();
    }

    /**
     * Writes a file "file" to "path"
     */
    public static void writeFile(String content, String filename)
    {
        writeFile(content, new File(filename));
    }
    public static void writeFile(String content, File file)
    {
        FileWriter f1;
        try {
            f1 = new FileWriter(file);
            f1.write(content);
            f1.close();
        }
        catch( IOException e ) {
            Core.getOut().println("Write Error: " + file.getPath());
        }
    }
    
	public static void writeFile(String content, File file, String encoding) {
		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, encoding);
			streamWriter.write(content);
			streamWriter.close();
		} catch (IOException e) {
			Core.getOut().println("Write Error: " + file.getPath());
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

    /**
     * Deletes the given directory and ALL FILES/DIRS IN IT !!!
     * USE CAREFUL !!!
     */
    public static boolean deleteDir(File dir)
    {
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++)
            {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success)
                {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }


    /**
     * Reads a keyfile from disk and adds the keys to a map
     * @param source keyfile as String or as File
     * @param chk Map that will be used to add the keys
     * @param exchange the exchange flag of SharedFileObject will be set to this value
     */
    public static FrostIndex readKeyFile(String source)
    {
        return readKeyFile(new File(source));
    }
    public static FrostIndex readKeyFile(File source)
    {
    	if (!source.isFile() || !(source.length() > 0))
    		return new FrostIndex(new HashMap());
        else
        {
            /*BufferedReader f;
            String line = new String();
            String filename = new String();
            String size = new String();
            String date = new String();
            String dateShared = null;
            String key = null;
            String SHA1 = null;
            String owner = new String();
            String batch = null;*/
            int counter = 0;

            //parse the xml file
            Document d= null;
            try {
                d = XMLTools.parseXmlFile(source.getPath(), false);
            } catch (IllegalArgumentException t) {
            	t.printStackTrace(Core.getOut());
            	File badfile = new File("badfile.xml");
            	source.renameTo(badfile);
            	Core.getOut().println("offending file saved as badfile.xml - send it to a dev for analysis");
            }

            if (d == null)
            {
                Core.getOut().println("Couldn't parse index file.");
                return null;
            }

			FrostIndex idx = new FrostIndex(d.getDocumentElement());
			
			
            /*Element main = d.getDocumentElement(); // 'Filelist'
            ArrayList files = XMLTools.getChildElementsByTagName(main, "File");

            if (files.size() == 0)
            {
            	if (source.length() > 0 ) {
            		Core.getOut().println("\n\n****Index empty but file is not!***");
            		Core.getOut().println("send badfile.xml to dev for analysis with the following info: \n\n");
            		Exception e = new Exception();
            		e.fillInStackTrace();
            		e.printStackTrace(Core.getOut());
					File badfile = new File ("badfile.xml");
					source.renameTo(badfile);
            	}
                Core.getOut().println("Index empty!");
                return;
            }
*/
            //now go through all the files
            Iterator i = idx.getFiles().iterator();

            while (i.hasNext())
            {
                //Element current = (Element)i.next();
                SharedFileObject newKey = (SharedFileObject)i.next();
               
                
                //validate the key
                if (!newKey.isValid())
                {
                	i.remove();
                    Core.getOut().println("invalid key found");
                    continue;
                }

            }
        return idx;
        } 
        
    }

    public static void writeKeyFile(FrostIndex idx, String destination)
    {
        writeKeyFile(idx, new File(destination));
    }
    public static void writeKeyFile(FrostIndex idx, File destination)
    {
        if( idx.getFiles().size() == 0 )
        {
            // no items to write
            return;
        }
        
        File tmpFile = new File( destination.getPath() + ".tmp" );
        
        //use FrostIndex object

        int itemsAppended = 0;
        synchronized (idx)
        {
            Iterator i = idx.getFiles().iterator();
            while (i.hasNext())
            {
                SharedFileObject current = (SharedFileObject)i.next();
				if (current.getOwner() != null
								&& frame1.getEnemies().Get(current.getOwner()) != null)
							{
								//Core.getOut().println("removing file from BAD user");
								//FIXME: this has been happening too often.  Debug properly
								i.remove();
								continue;
							}

                itemsAppended++;
            }
        }
		
        
        if( itemsAppended == 0 )
        {
            // don't write file
        	Core.getOut().println("writeKeyFile called with no files to add?");
            return;
        }
        
        // xml tree created, now save
        
        boolean writeOK = false;
        try {
        	Document doc = XMLTools.getXMLDocument(idx);
            writeOK = XMLTools.writeXmlFile(doc, tmpFile.getPath());
        } catch(Throwable t)
        {
            System.out.println("Exception - writeKeyFile:");
            t.printStackTrace(Core.getOut());
        }

        if( writeOK )
        {
            File oldFile = new File(destination.getPath() + ".old");
            oldFile.delete();
            destination.renameTo(oldFile);
            tmpFile.renameTo(destination);
        }
        else
        {
            // delete incomplete file
            tmpFile.delete();
        }
    }

    public static String readFileRaw(String path)
    {
        return readFileRaw(new File(path));
    }

    public static String readFileRaw(File file)
    {
        if (!file.exists())
            return null;
        return readFile(file);    
/*        String result;
        try
        {
            FileChannel fc = (new FileInputStream(file)).getChannel();
            ByteBuffer buf = ByteBuffer.allocate((int)file.length());

            while (buf.remaining() > 0)
            {
                fc.read(buf);
            }

            fc.close();
            buf.flip();
            result = charset.decode(buf).toString();
        }
        catch (IOException e)
        {
            e.printStackTrace(Core.getOut());
            return new String();
        }
        return result;*/
    }
    

}
