/*
KeyClass.java / Frost
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
package frost.messages;

import java.util.*;
import java.io.File;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import frost.*;
import frost.gui.objects.*;


//Renamed this class to SharedFileObject.
//there will be more refactoring of it:
//base will be IndexedFileObject
//extended by OwnedFileObject

//why do this?  Because in the future we may want to add more descriptions,
//maybe some categories, user comments, etc.  Its all possible with xml ;)

public class SharedFileObject implements XMLizable
{
    private boolean DEBUG = false;
    private final static String[] invalidChars = {"/", "\\", "?", "*", "<", ">", "\"", ":", "|"};

    String key = null; // Name of this key
    String date = null; // Last access
    String lastSharedDate = null; //date the file was shared last
    String SHA1 = null;  //SHA1 of the file
    String owner = null;  //person that uploaded the file
    Long size = new Long(0); // Filesize
    String filename = new String();
    File file = null; //the file itself
    String batch = null;
    FrostBoardObject board;
    boolean exchange;

    public GregorianCalendar getCal()
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        int firstPoint = date.indexOf(".");
        int secondPoint = date.lastIndexOf(".");
        if( firstPoint != -1 && secondPoint != -1 && firstPoint != secondPoint )
        {
            int year = Integer.parseInt(date.substring(0, firstPoint));
            int month = Integer.parseInt(date.substring(firstPoint + 1, secondPoint));
            int day = Integer.parseInt(date.substring(secondPoint + 1, date.length()));
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month - 1);
            cal.set(Calendar.DATE, day - 1);
        }
        return cal;
    }

    /**Returns true if key is outdated*/
    public boolean checkDate()
    {
    	if (date == null) return true;
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
	
        int firstPoint = date.indexOf(".");
        int secondPoint = date.lastIndexOf(".");
        int maxAge = frame1.frostSettings.getIntValue("maxAge");
        if( firstPoint != -1 && secondPoint != -1 && firstPoint != secondPoint )
        {
            try
            {
                int year = Integer.parseInt(date.substring(0, firstPoint));
                int month = Integer.parseInt(date.substring(firstPoint + 1, secondPoint));
                int day = Integer.parseInt(date.substring(secondPoint + 1, date.length()));
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month - 1);
                cal.set(Calendar.DATE, day + maxAge);

                GregorianCalendar today = new GregorianCalendar();
                today.setTimeZone(TimeZone.getTimeZone("GMT"));

                if( today.after(cal) )
                {
                    //if( DEBUG ) 
		    System.out.println(filename + " is outdated");
                    return false;
                }

                today.add(Calendar.DATE, (maxAge + 2)); // Accept one day into future
                if( cal.after(today) )
                {
                    //if( DEBUG ) 
		    System.out.println("Future date of " + filename + " " + year + month + day);
                    return false;
                }

                return true;
            }
            catch( NumberFormatException e )
            {
                //if( DEBUG ) 
		System.out.println("Date of " + filename + " is invalid");
                return false;
            }
        }
        else
        {
            //if( DEBUG ) 
	    System.out.println(filename + " has invalid date");
            return false;
        }
    }

    /**Tests if the filename is valid*/
    public boolean checkFilename()
    {
        if( filename==null || filename.length() == 0 || filename.length() > 255 )
            return false;

        for( int i = 0; i < invalidChars.length; i++ )
        {
            if( filename.indexOf(invalidChars[i]) != -1 )
            {
                if( DEBUG ) System.out.println(filename + " has invalid filename");
                return false;
            }
        }
        return true;
    }

    /** Tests, if size is a valid integer value*/
    public boolean checkSize()
    {
        if( size == null )
            return false;
        return true;

/*
        try {
            long tmp = Integer.parseInt(size);
        }
        catch( NumberFormatException e ) {
            if( DEBUG ) System.out.println("Invalid size in key " + filename);
            return false;
        }
        return true;
*/
    }

    /**Tests if key is valid*/
    public boolean checkKey()
    {
    	if (key == null) return true;
        if( key.startsWith("CHK@") && key.length() == 58 ) return true;
        //  if (DEBUG) System.out.println("Invalid key in " + filename);
        return false;
    }

    /**Returns true if key is valid*/
    public boolean isValid()
    {
        boolean valid = checkDate();
        valid = valid && checkSize();
        valid = valid && checkFilename();
        valid = valid && checkKey();
        return valid;
    }

    /**Set filename*/
    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    /**Get filename*/
    public String getFilename()
    {
        return filename.trim();
    }
    
    /** Get SHA1*/
    public String getSHA1() {
    	return SHA1;
    }
    
    /** Set SHA1*/
    public void setSHA1(String s) {
    	SHA1=s;
    }
    
    /** Set owner*/
    public void setOwner(String owner_id) {
    	owner=owner_id;
    }
    
    /** Get owner*/
    public String getOwner() {
    	return owner;
    }

    /**Set key*/
    public void setKey(String key)
    {
        this.key = key;
    }

    /**Get key*/
    public String getKey()
    {
    	if (key == null)
		return key;
        return key.trim();
    }

    /**Set date*/
    public void setDate(String date)
    {
        this.date = date;
    }

    /**Get date*/
    public String getDate()
    {
    	if (date == null) return date;
        return date.trim();
    }

    /**Set size*/
    public void setSize(String size)
    {
        try {
            this.size = new Long(size);
        }
        catch(NumberFormatException ex) {
            this.size = null;
        }
    }
   
    /**Set size*/
    public void setSize(long size)
    {
        this.size = new Long(size);
    }

    /**Get size*/
    public Long getSize()
    {
        return size;
    }

    public boolean getExchange()
    {
        return exchange;
    }
    public void setExchange(boolean exchange)
    {
        this.exchange = exchange;
    }
    
    public String getLastSharedDate() {
    	return lastSharedDate;
    }
    public void setLastSharedDate(String newdate) {
    	lastSharedDate=newdate;
    }
    
    public String getBatch() {
    	return batch;
    }
    
    public void setBatch(String what) {
    	batch=what;
    	
    }

    /** Constructor*/
    public SharedFileObject(String key)
    {
        this.key = key;
        this.exchange = true;
    }
    
    /** also an empty constructor, just in case*/
    public SharedFileObject() {
    	exchange=true;
    }
    
    /**
     * Creates a sharedFileObject to be uploaded.
     * it can be used both from the uploadTable and from attachments.
     * @param file the file to be uploaded
     * @param board the board to which index to add the file.  If null, the file will
     * not be added to any index and won't participate in the request system.
     */
    public SharedFileObject(File file, FrostBoardObject board) {
    	SHA1 = Core.getCrypto().digest(file);
    	size = new Long(file.length());
    	filename = file.getName();
    	date = DateFun.getDate();
    	
    	this.file = file;
    	//if key == null means file is offline.
    	//when uploading file as attachment, key will change to CHK
    	//when the file is uploaded.
    	key = null;
    	
    	
    	this.board = board;
    	if (board == null)
    		batch = null;
    	else { //this file will be added to index, assign  a batch
    		Iterator it = Core.getMyBatches().entrySet().iterator();
    		
    		while (it.hasNext()){
    			String current = (String)it.next();
    			int size = ((Integer)Core.getMyBatches().get(current)).intValue();
    			if (size < Core.frostSettings.getIntValue("batchSize")) {
    				batch=current;
    				break;
    			} 
    		}
    		
    	} 
    }
    
    public Element getXMLElement(Document doc)  {

			 //we do not add keys who are not signed by people we marked as GOOD!
			 //but we add unsigned keys for now; this will probably change soon
                
			 Element fileelement = doc.createElement("File");
                
			 Element element = doc.createElement("name");
			 CDATASection cdata = doc.createCDATASection(getFilename());
			 element.appendChild( cdata );
			 fileelement.appendChild( element );
              
             //always add SHA1
             
			 element = doc.createElement("SHA1");
			 cdata = doc.createCDATASection(getSHA1());
			 element.appendChild( cdata );
			 fileelement.appendChild( element );
             

              
			 element = doc.createElement("size");
			 Text textnode = doc.createTextNode(""+getSize());
			 element.appendChild( textnode );
			 fileelement.appendChild( element );
                
			 if( getBatch() != null )
			 {
				 element = doc.createElement("batch");
				 textnode = doc.createTextNode(getBatch());
				 element.appendChild( textnode );
				 fileelement.appendChild( element );
			 }

//f1.write(key.getFilename() + "\r\n" + key.getSize() + "\r\n" + key.getDate() + "\r\n" + key.getKey() + "\r\n");

			 if (getOwner() != null)
			 {
				 element = doc.createElement("owner");
				 cdata = doc.createCDATASection(getOwner());
				 element.appendChild( cdata );
				 fileelement.appendChild( element );
			 } /*else 
			 if (board!=null) {
				 element = doc.createElement("owner");
				 cdata = doc.createCDATASection(mixed.makeFilename(Core.getMyId().getUniqueName()));
				 element.appendChild( cdata );
				 fileelement.appendChild( element );
			 }*/
			 
			 if (getKey() != null)
			 {
				 element = doc.createElement("key");
				 textnode = doc.createTextNode(getKey());
				 element.appendChild( textnode );
				 fileelement.appendChild( element );
			 }
			 if (getDate() != null)
			 {
				 element = doc.createElement("date");
				 textnode = doc.createTextNode(getDate());
				 element.appendChild( textnode );
				 fileelement.appendChild( element );
			 }
			 if (getLastSharedDate() != null)
			 {
				 element = doc.createElement("dateShared");
				 textnode = doc.createTextNode(getLastSharedDate());
				 element.appendChild( textnode );
				 fileelement.appendChild( element );
			 }
			 return fileelement;
    }
    
    public void loadXMLElement(Element current) throws SAXException{
//		extract the values
					  try
					  {
						  setFilename(
							  XMLTools.getChildElementsCDATAValue(current, "name"));
						  setSHA1(
							  XMLTools.getChildElementsCDATAValue(current, "SHA1"));
					  }
					  catch (ClassCastException e)
					  {
						  Core.getOut().println(
							  "received an index from early beta. grr");
						  setSHA1(
							  XMLTools.getChildElementsTextValue(current, "SHA1"));
						  setFilename(
							  XMLTools.getChildElementsTextValue(current, "name"));
					  }
					  setOwner(
						  XMLTools.getChildElementsCDATAValue(current, "owner"));

					  setKey(
						  XMLTools.getChildElementsTextValue(current, "key"));
					  setDate(
						  XMLTools.getChildElementsTextValue(current, "date"));
					  setLastSharedDate(
						  XMLTools.getChildElementsTextValue(current, "dateShared"));
					  setSize(
						  XMLTools.getChildElementsTextValue(current, "size"));
					  setBatch(
						  XMLTools.getChildElementsTextValue(current, "batch"));
						  
					 
    }
    
    
	/**
	 * @return the board this file will be uploaded to, if any
	 */
	public FrostBoardObject getBoard() {
		return board;
	}
	
	/**
	 * 
	 * @return true if the file has been inserted in freenet.
	 */
	public boolean isOnline() {
		return key != null;
	}

	/**
	 * @return the File object if such exists
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * @param object
	 */
	public void setBoard(FrostBoardObject object) {
		board = object;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		SharedFileObject other = (SharedFileObject) obj;
		return SHA1.equals(other.getSHA1());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		// TODO Auto-generated method stub
		return SHA1.hashCode();
	}

}
