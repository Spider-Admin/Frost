/*
  XMLTools.java / Frost
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
package frost;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.*;

import javax.xml.parsers.*;

import org.apache.xml.serialize.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * A place to hold utility methods for XML processing.
 */
public class XMLTools
{
	
	private static Logger logger = Logger.getLogger(XMLTools.class.getName());
	
	/**
	 * creates a document containing a single element - the one 
	 * returned by getXMLElement of the argument
	 * @param element the object that will be contained by the document
	 * @return the document
	 */
	public static Document getXMLDocument(XMLizable element){
		Document doc = createDomDocument();
		doc.appendChild(element.getXMLElement(doc));
		return doc;
	}
	
	public static byte [] getRawXMLDocument (XMLizable element){
		Document doc = getXMLDocument(element);
        // create a proper temp file (deleted on VM emergency exit)
        File tmp = null;
        try {
            tmp = File.createTempFile("xmltools_", 
                                      ".tmp", 
                                      new File(frame1.frostSettings.getValue("temp.dir")));
        }
        catch(Exception ex) {
            // this should never happen, but for the case ...
            tmp = new File("xmltools_"+System.currentTimeMillis());
        }
		byte [] result=null;
		try {
    		writeXmlFile(doc, tmp.getPath());
    		result = FileAccess.readByteArray(tmp);
    		tmp.delete();
		} catch (Throwable t) {
			logger.log(Level.SEVERE, "Exception thrown in getRawXMLDocument(XMLizable element)", t);
		}
		return result;
	}
    /**
     * Parses an XML file and returns a DOM document.
     * If validating is true, the contents is validated against the DTD
     * specified in the file.
     */
    public static Document parseXmlFile(String filename, boolean validating)
    throws IllegalArgumentException
    {
        return parseXmlFile(new File(filename), validating);
    }
    
    /**
     * Parses an XML file and returns a DOM document.
     * If validating is true, the contents is validated against the DTD
     * specified in the file.
     */
    public static Document parseXmlFile(File file, boolean validating)
    throws IllegalArgumentException
    {
        try
        {
            // Create a builder factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setAttribute("http://apache.org/xml/features/disallow-doctype-decl", new Boolean(true));
            factory.setAttribute("http://xml.org/sax/features/external-general-entities",new Boolean(false));
            factory.setAttribute("http://xml.org/sax/features/external-parameter-entities",new Boolean(false));
			factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-dtd-grammar",new Boolean(false));
			factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd",new Boolean(false));
            factory.setValidating(validating);

            // Create the builder and parse the file
            Document doc = factory.newDocumentBuilder().parse(file);
            return doc;
        }
        catch( SAXException e )
        {
            // A parsing error occurred; the xml input is not valid
			logger.log(Level.SEVERE, "Parsing of xml file failed.  Send badfile.xml to a dev for analysis", e);
	    	file.renameTo(new File("badfile.xml"));
            throw new IllegalArgumentException();
        }
        catch( ParserConfigurationException e )
        {
			logger.log(Level.SEVERE, "Exception thrown in parseXmlFile(File file, boolean validating)", e);
        }
        catch( IOException e )
        {
			logger.log(Level.SEVERE, "Exception thrown in parseXmlFile(File file, boolean validating)", e);
        }
        return null;
    }

    /**
     * This method writes a DOM document to a file.
     */
    public static boolean writeXmlFile(Document doc, String filename)
    throws Throwable
    {
        try {
            //OutputFormat format = new OutputFormat(doc);
            OutputFormat format = new OutputFormat(doc, "UTF-16", false);
            format.setLineSeparator(LineSeparator.Windows);
            //format.setIndenting(true);
            format.setLineWidth(0);
            format.setPreserveSpace(true);
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename), "UTF-16");
            XMLSerializer serializer = new XMLSerializer (writer, format);
            serializer.asDOMSerializer();
            serializer.serialize(doc);
            writer.close();  //this also flushes
            return true;
        }
        catch(Exception ex)
        {
			logger.log(Level.SEVERE, "Exception thrown in writeXmlFile(Document doc, String filename)", ex);
        }
        return false;
    }

    /**
     * This method creates a new DOM document.
     */
    public static Document createDomDocument()
    {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            return doc;
        } catch (ParserConfigurationException e)
        {
			logger.log(Level.SEVERE, "Exception thrown in createDomDocument()", e);
        }
        return null;
    }

    /**
     * gets a true or false attribute from an element
     */
    public static boolean getBoolValueFromAttribute(Element el, String attr, boolean defaultVal)
    {
        String res = el.getAttribute(attr);

        if( res == null )
            return defaultVal;

        if( res.toLowerCase().equals("true") == true )
            return true;
        else
            return false;
    }

    /**
     * Returns a list containing all Elements of this parent with given tag name.
     */
    public static ArrayList getChildElementsByTagName(Element parent, String name)
    {
        ArrayList newList = new ArrayList();

        NodeList childs = parent.getChildNodes();
        for( int x=0; x<childs.getLength(); x++ )
        {
            Node child = childs.item(x);
            if( child.getNodeType() == Node.ELEMENT_NODE )
            {
                Element ele = (Element)child;
                if( ele.getTagName().equals( name ) == true )
                {
                    newList.add( ele );
                }
            }
        }
        return newList;
    }

    /**
     * Gets the Element by name from parent and extracts the Text child node.
     * E.g.:
     * <parent>
     *   <child>
     *     text
     */
    public static String getChildElementsTextValue( Element parent, String childname )
    {
        ArrayList nodes = getChildElementsByTagName( parent, childname );
        if( nodes.size() == 0 )
            return null;
	
        Text txtname = (Text) (((Node)nodes.get(0)).getFirstChild());
	
        if( txtname == null )
            return null;
        return txtname.getData().trim();
    }

    /**
     * Gets the Element by name from parent and extracts the CDATASection child node.
     */
    public static String getChildElementsCDATAValue( Element parent, String childname )
    {
        ArrayList nodes = getChildElementsByTagName( parent, childname );
        if( nodes.size() == 0 )
            return null;
        CDATASection txtname = (CDATASection) ((Node)nodes.get(0)).getFirstChild();
        if( txtname == null )
            return null;
        return txtname.getData().trim();
    }
}
