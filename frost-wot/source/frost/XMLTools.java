package frost;

import java.io.*;
import java.util.ArrayList;

import javax.xml.parsers.*;

import org.apache.xml.serialize.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * A place to hold utility methods for XML processing.
 */
public class XMLTools
{
    /**
     * Parses an XML file and returns a DOM document.
     * If validating is true, the contents is validated against the DTD
     * specified in the file.
     */
    public static Document parseXmlFile(String filename, boolean validating)
    throws IllegalArgumentException
    {
        try
        {
            // Create a builder factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(validating);

            // Create the builder and parse the file
            Document doc = factory.newDocumentBuilder().parse(new File(filename));
            return doc;
        }
        catch( SAXException e )
        {
            // A parsing error occurred; the xml input is not valid
            throw new IllegalArgumentException();
        }
        catch( ParserConfigurationException e )
        {
        }
        catch( IOException e )
        {
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
            return true;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
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
            e.printStackTrace();
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
