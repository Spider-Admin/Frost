package frost.identities;

import java.io.IOException;
import java.util.logging.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import frost.*;
import frost.fcp.FcpFactory;
import frost.gui.objects.FrostBoardObject;
import frost.messages.BoardAttachment;

/**
 * Represents the main user's identity
 */
public class LocalIdentity extends Identity
{
    private String privKey;
    
	private static Logger logger = Logger.getLogger(LocalIdentity.class.getName());


	public Element getXMLElement(Document doc) {
	
		//have to copy all children, no Element.rename()unfortunately
		Element el = super.getXMLElement(doc);
		Element el2 = doc.createElement("MyIdentity");
		NodeList list = el.getChildNodes();
		while(list.getLength()>0)
			el2.appendChild(list.item(0));
		
		
		Element element = doc.createElement("privKey");
		CDATASection cdata = doc.createCDATASection(privKey);
		element.appendChild(cdata);
		el2.appendChild(element);
		return el2;
	}
	/**
	 * just renames the element to MyIdentity to maintain
	 * backward compatibility
	 */
	public Element getSafeXMLElement(Document doc){
		Element el = super.getSafeXMLElement(doc);
		Element el2 = doc.createElement("MyIdentity");
		NodeList list = el.getChildNodes();
		while (list.getLength()>0)
			el2.appendChild(list.item(0));
		return el2;	
		
	}
	

	public void loadXMLElement(Element el) throws SAXException {
		super.loadXMLElement(el);
		privKey =  XMLTools.getChildElementsCDATAValue(el, "privKey");
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
        this(name, Core.getCrypto().generateKeys());

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
			logger.log(Level.SEVERE, "Exception thrown in constructor", ex);
        	board = null;
        }
        

    }

    public String getPrivKey()
    {
        return privKey;
    }
}

