package frost;
import org.w3c.dom.*;
//FIXME: decide which exception is most appropriate and use that
import org.xml.sax.SAXException;
import java.io.*;

/**
 * @author zlatinb
 * Interface for objects that will be serializable in xml
 */
public interface XMLizable extends Serializable {
	
	/**
	 * Creates an xml element of those objects that can be serialized to xml
	 * @param container the parent document
	 * @return the element that's ready to be returned
	 */
	public Element getXMLElement(Document container); 
	
	public void loadXMLElement(Element e) throws SAXException; //this probably shouldn't be SAXException
	
}