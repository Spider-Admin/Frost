package frost.identities;

import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import frost.*;
import frost.messages.*;
import frost.gui.objects.*;
import frost.FcpTools.*;

/**
 * Represents the main user's identity
 */
public class LocalIdentity extends Identity
{
    private String privKey;

	protected Element localIdentityPopulateElement(Element el, Document doc){
		el = baseIdentityPopulateElement(el,doc);
		Element element = doc.createElement("privKey");
		CDATASection cdata = doc.createCDATASection(privKey);
		element.appendChild(cdata);
		el.appendChild(element);
		return el;
	}

	public Element getXMLElement(Document doc) {
		Element el = doc.createElement("MyIdentity");
		el = localIdentityPopulateElement(el,doc);
		return el;
	}
	
	public Element getSafeXMLElement(Document doc){
			Element el = getXMLElement(doc);
			List sensitive = XMLTools.getChildElementsByTagName(el,"trustedIds");
			sensitive.addAll(XMLTools.getChildElementsByTagName(el,"files"));
			sensitive.addAll(XMLTools.getChildElementsByTagName(el,"messages"));
			sensitive.addAll(XMLTools.getChildElementsByTagName(el,"privKey"));
		
			Iterator it = sensitive.iterator();
			while (it.hasNext()) {
				el.removeChild((Element)it.next());
			}
			return el;
		}
	
	protected void localIdentityPopulateFromElement(Element el ) throws SAXException {
		baseIdentityPopulateFromElement(el);
		privKey =  XMLTools.getChildElementsCDATAValue(el, "privKey");
	}
	
	public void loadXMLElement(Element el) throws SAXException {
		localIdentityPopulateFromElement(el);
	}

    /**
     * a constructor that assumes that the user has inserted the
     * key in his SSK already
     */
    public LocalIdentity(String name, String[] keys)
    {
        super(name,  keys[1]);
        privKey=keys[0];
    }

	public LocalIdentity(Element el){
		super(el);
	}
    /**
     * constructor that creates an RSA Keypair
     */
    public LocalIdentity(String name)
    {
        this(name, frame1.getCrypto().generateKeys());

        con = FcpFactory.getFcpConnectionInstance();
        if( con == null )
        {
            this.key=NA;
            return;
        }
        try{
        	String []svk=con.getKeyPair();
			board = new BoardAttachment(new FrostBoardObject(
									getUniqueName(),svk[1],svk[0]));
			
        }catch(IOException ex){
        	ex.printStackTrace(Core.getOut());
        	board = null;
        }
        

    }

    public String getPrivKey()
    {
        return privKey;
    }
}

