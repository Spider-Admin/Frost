package frost.identities;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import frost.*;
import frost.FcpTools.*;

/**
 * Represents a user identity, should be immutable.
 */
public class Identity implements XMLizable
{
    private String name;
    private String uniqueName;
    protected String key, keyaddress;
    protected transient FcpConnection con;
    public static final String NA = "NA";
    private static ThreadLocal tmpfile;
    
    //some trust map methods
    protected int noMessages,noFiles;
    protected List trustees;

	//if this was C++ LocalIdentity wouldn't work
	//fortunately we have virtual construction so loadXMLElement will be called
	//for the inheriting class ;-)
	Identity(Element el) {
		try {
			loadXMLElement(el);
		} catch (SAXException e) {
			e.printStackTrace(Core.getOut());
		}
	}

	/**
	 * creates an Element with specific fields for this classes
	 * inheriting classes should call this method to fill in their elements
	 */
	protected Element baseIdentityPopulateElement(Element el, Document doc){
		//name
		Element element = doc.createElement("name");
		CDATASection cdata = doc.createCDATASection(getUniqueName());
		element.appendChild( cdata );
		el.appendChild( element );
		
		//key address - we really need to get rid of this, it slows down everything
		element = doc.createElement("CHK");
		cdata = doc.createCDATASection(getKeyAddress());
		element.appendChild( cdata );
		el.appendChild( element );
		
		//key itself
		element = doc.createElement("key");
		cdata = doc.createCDATASection(getKey());
		element.appendChild( cdata );
		el.appendChild( element );
		
		//# of files
		element = doc.createElement("files");
		Text text = doc.createTextNode(""+noFiles);
		element.appendChild(text);
		el.appendChild(element);
		
		//# of messages
		element = doc.createElement("messages");
		text = doc.createTextNode(""+noMessages);
		element.appendChild(text);
		el.appendChild(element);
		
		//trusted identities
		if (trustees != null) {
			element = doc.createElement("trustedIds");
			Iterator it = trustees.iterator();
			while (it.hasNext()) {
				String id = (String)it.next();
				Element trustee = doc.createElement("trustee");
				cdata = doc.createCDATASection(id);
				trustee.appendChild(cdata);
				element.appendChild(trustee);
			}
			el.appendChild(element);
		}
		
		return el;
	}

	public Element getXMLElement(Document doc)  {
		Element el = doc.createElement("Identity");
		el = baseIdentityPopulateElement(el,doc);
		return el;
	}
	
	protected void baseIdentityPopulateFromElement(Element e) throws SAXException {
				uniqueName = XMLTools.getChildElementsCDATAValue(e, "name");
				name = uniqueName.substring(0,uniqueName.indexOf("@"));
				keyaddress =  XMLTools.getChildElementsCDATAValue(e, "CHK");
				key =  XMLTools.getChildElementsCDATAValue(e, "key");
				try {
					noMessages = (new Integer(XMLTools.getChildElementsTextValue(e,"messages"))).intValue();
					noFiles = (new Integer(XMLTools.getChildElementsTextValue(e,"files"))).intValue();
				}catch (NullPointerException npe) {
					Core.getOut().println("no data about # of messages found for identity " + uniqueName);
				}
				
				
				Element trusteesList = (Element) XMLTools.getChildElementsByTagName(e,"trustees").get(0);
				if (trusteesList != null) {
					if (trustees == null)
						trustees = new LinkedList();
					List trusteeEntities = XMLTools.getChildElementsByTagName(trusteesList,"trustee");
					Iterator it = trusteeEntities.iterator();
					while (it.hasNext()) {
						Element trustee = (Element)it.next();
						String id = ((CDATASection) trustee.getFirstChild()).getData().trim();
						trustees.add(id);
					}
				}
				
	}
	
	public void loadXMLElement(Element e) throws SAXException {
		baseIdentityPopulateFromElement(e);
	}

    /**
     * we use this constructor whenever we have all the info
     */
    public Identity(String name, String keyaddress, String key)
    {
        this.keyaddress = keyaddress;
        this.key = key;
        setName(name);
    }

    /**
     * this constructor fetches the key from a SSK,
     * it blocks so it should be done from the TOFDownload thread (I think)
     */
    public Identity(String name, String keyaddress) throws IllegalArgumentException
    {
        this.keyaddress = keyaddress;

        con = FcpFactory.getFcpConnectionInstance();
        if( con == null )
        {
            this.key = NA;
            return;
        }

        if( !keyaddress.startsWith("CHK@") )
        {
            this.key = NA;
            throw (new IllegalArgumentException("not a CHK"));
        }

        System.out.println("Identity: Starting to request CHK for '" + name +"'");
        String targetFile = frame1.frostSettings.getValue("temp.dir") + name + ".key.tmp";

        // try X times to get identity, its too important to not to try it ;)
        // will lower the amount of N/A messages because of non found keys
        boolean wasOK = false;
        int maxTries = 3;
        int tries = 0;
        while( wasOK == false && tries < maxTries )
        {
            try {
                wasOK = FcpRequest.getFile(keyaddress, null, new File(targetFile), 25, false);
            }
            catch(Exception e) { ; }
            mixed.wait(3500);
            tries++;
        }

        if( wasOK )
        {
            key = FileAccess.read(targetFile);
            System.out.println("Identity: CHK received for " +name);
        }
        else
        {
            key=NA;
            System.out.println("Identity: Failed to get CHK for " +name);
        }
        File tfile = new File(targetFile);
        tfile.delete();

        setName(name); // must be called after key is got!
    }

    private void setName(String nam)
    {
        this.name = nam;
        if( getKey().equals( NA ) )
            this.uniqueName = nam;
        else
            this.uniqueName = nam + "@" + frame1.getCrypto().digest( getKey() );
    }

    //obvious stuff
    public String getName()
    {
        return name;
    }
    public String getKeyAddress()
    {
        return keyaddress;
    }
    public String getKey()
    {
        return key;
    }
    public String getStrippedName()
    {
        return new String(name.substring(0,name.indexOf("@")));
    }


    public String getUniqueName()
    {
        return uniqueName;
    }
	/**
	 * @return list of identities this identity trusts
	 */
	public List getTrustees() {
		return trustees;
	}

}
