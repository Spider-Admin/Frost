package frost;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;


/**
 * a place to hold utility methods
 */
public class XMLTools {

/**
 *Parses an XML file and returns a DOM document.
 * If validating is true, the contents is validated against the DTD
 * specified in the file.
 */
public static Document parseXmlFile(String filename, boolean validating)
    throws IllegalArgumentException
    {
        try {
            // Create a builder factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(validating);

            // Create the builder and parse the file
            Document doc = factory.newDocumentBuilder().parse(new File(filename));
            return doc;
        } catch (SAXException e) {
            // A parsing error occurred; the xml input is not valid
        throw new IllegalArgumentException();
        } catch (ParserConfigurationException e) {
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * gets a true or false attribute from an element
     */
     public static boolean getBoolValueFromAttribute(Element el, String attr, boolean defaultVal)
     {
        String res = el.getAttribute(attr);

        if (res == null )
            return defaultVal;

        if( res.toLowerCase().equals("true") == true )
            return true;
        else
            return true;
     }

     /**
     * Returns a list containing all Elements of this parent with given tag name.
     */
    public static ArrayList getChildElementsByTagName(Element parent, String name)
    {
        ArrayList newList = new ArrayList();

        NodeList childs = parent.getChildNodes();
        for(int x=0; x<childs.getLength(); x++)
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
}