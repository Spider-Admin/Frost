package frost;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import java.io.*;

/**
 * Interface for objects that will be serializable in xml
 */
public interface XMLizable extends Serializable {
	
	public Element getXMLElement() throws SAXException;
	
	public void loadXMLElement(Element e) throws SAXException;
	
}