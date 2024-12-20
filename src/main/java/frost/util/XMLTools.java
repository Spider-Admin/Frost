/*
  XMLTools.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * A place to hold utility methods for XML processing.
 */
public class XMLTools {

	private static final Logger logger = LoggerFactory.getLogger(XMLTools.class);

    // FIXME: maybe use JDK JAXP parser and drop xerces?

    private static DocumentBuilderFactory nonValidatingFactory = DocumentBuilderFactory.newInstance();

	static {
        nonValidatingFactory.setAttribute("http://apache.org/xml/features/disallow-doctype-decl", Boolean.TRUE);
        nonValidatingFactory.setAttribute("http://xml.org/sax/features/external-general-entities",Boolean.FALSE);
        nonValidatingFactory.setAttribute("http://xml.org/sax/features/external-parameter-entities",Boolean.FALSE);
        nonValidatingFactory.setAttribute("http://apache.org/xml/features/nonvalidating/load-dtd-grammar",Boolean.FALSE);
        nonValidatingFactory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd",Boolean.FALSE);
        nonValidatingFactory.setValidating(false);
    }

    /**
     * creates a document containing a single element - the one
     * returned by getXMLElement of the argument
     * @param element the object that will be contained by the document
     * @return the document
     */
    public static Document getXMLDocument(final XMLizable element) {
        final Document doc = createDomDocument();
        doc.appendChild(element.getXMLElement(doc));
        return doc;
    }

    /**
     * Parses an XML file and returns a DOM document.
     * If validating is true, the contents is validated against the DTD
     * specified in the file.
     */
    public static Document parseXmlFile(final String filename)
    throws IllegalArgumentException
    {
        return parseXmlFile(new File(filename));
    }

    /**
     * Parses an XML file and returns a DOM document.
     * If validating is true, the contents is validated against the DTD
     * specified in the file.
     */
    public static Document parseXmlFile(final File file)
        throws IllegalArgumentException {
        try {
            DocumentBuilder builder;
            synchronized (nonValidatingFactory) {
                builder = nonValidatingFactory.newDocumentBuilder();
            }
            return builder.parse(file);
        } catch (final SAXException e) {
            // A parsing error occurred; the xml input is not valid
            logger.error("Parsing of xml file failed (send badfile.xml to a dev for analysis) - File name: '{}'", file.getName(), e);
            file.renameTo(new File("badfile.xml"));
            throw new IllegalArgumentException();
        } catch (final ParserConfigurationException e) {
            logger.error("Exception thrown in parseXmlFile(File file, boolean validating) - File name: '{}'", file.getName(), e);
        } catch (final IOException e) {
            logger.error("Exception thrown in parseXmlFile(File file, boolean validating) - File name: '{}'", file.getName(), e);
        }
        return null;
    }

    /**
     * This method writes a DOM document to a file.
     */
    public static boolean writeXmlFile(final Document doc, final String filename) {
        return writeXmlFile(doc, new File(filename));
    }

	/**
	 * This method writes a DOM document to a file.
	 */
	public static boolean writeXmlFile(final Document doc, final File file) {
		try (FileOutputStream fileout = new FileOutputStream(file);) {
			final StreamResult output = new StreamResult(fileout);

			final Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "no");
			tr.setOutputProperty(OutputKeys.METHOD, "xml");
			tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			final Source input = new DOMSource(doc);

			tr.transform(input, output);
			return true;
		} catch (IOException | TransformerException | TransformerFactoryConfigurationError e) {
			logger.error("Exception thrown in writeXmlFile(Document doc, String filename)", e);
		}
		return false;
	}

    /**
     * This method creates a new DOM document.
     */
    public static Document createDomDocument() {

        try {
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document doc = builder.newDocument();
            return doc;
        } catch (final ParserConfigurationException e) {
            logger.error("Exception thrown in createDomDocument()", e);
        }
        return null;
    }

    /**
     * gets a true or false attribute from an element
     */
    public static boolean getBoolValueFromAttribute(final Element el, final String attr, final boolean defaultVal) {

        final String res = el.getAttribute(attr);

        if( res == null ) {
            return defaultVal;
        }

        if( res.toLowerCase().equals("true") == true ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a list containing all Elements of this parent with given tag name.
     */
    public static List<Element> getChildElementsByTagName(final Element parent, final String name) {

        final LinkedList<Element> newList = new LinkedList<Element>();

        final NodeList childs = parent.getChildNodes();
        for( int x=0; x<childs.getLength(); x++ ) {
            final Node child = childs.item(x);
            if( child.getNodeType() == Node.ELEMENT_NODE ) {
                final Element ele = (Element)child;
                if( ele.getTagName().equals( name ) == true ) {
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
    public static String getChildElementsTextValue( final Element parent, final String childname ) {

        final List<Element> nodes = getChildElementsByTagName( parent, childname );
        if( nodes.size() == 0 ) {
            return null;
        }
        final Text txtname = (Text) (((Node)nodes.get(0)).getFirstChild());
        if( txtname == null ) {
            return null;
        }
        return txtname.getData();
    }

    /**
     * Gets the Element by name from parent and extracts the CDATASection child node.
     */
    public static String getChildElementsCDATAValue( final Element parent, final String childname ) {

        final List<Element> nodes = getChildElementsByTagName( parent, childname );
        if( nodes.size() == 0 ) {
            return null;
        }
        CDATASection txtname = (CDATASection) ((Node)nodes.get(0)).getFirstChild();
        if( txtname == null ) {
            return null;
        }
        // if the text contained control characters then it was maybe splitted into multiple CDATA sections.
        if( txtname.getNextSibling() == null ) {
            return txtname.getData();
        }

        final StringBuilder sb = new StringBuilder(txtname.getData());
        while( txtname.getNextSibling() != null ) {
            txtname = (CDATASection)txtname.getNextSibling();
            sb.append(txtname.getData());
        }
        return sb.toString();
    }
}
