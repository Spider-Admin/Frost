/*
 * Created on Oct 25, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost;
import org.w3c.dom.*;
/**
 * @author zlatinb
 *
 * creates "safe" xml elements which do not contain any information
 * that may be a privacy risk
 */
public interface SafeXMLizable extends XMLizable {
	public Element getSafeXMLElement(Document container);

}
