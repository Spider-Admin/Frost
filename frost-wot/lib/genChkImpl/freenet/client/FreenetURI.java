package freenet.client;

import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Logger;

import freenet.FieldSet;

/**
 * FreenetURI handles parsing and creation of the Freenet URI format, defined 
 * as follows:
 *
 * freenet:[KeyType@]RoutingKey[,CryptoKey][,n1=v1,n2=v2,...][/docname][//metastring]
 *
 * where
 * KeyType is the TLA of the key (currently SVK, SSK, KSK, or CHK).
 * If omitted, KeyType defaults to KSK
 *
 * For KSKs, the string keyword (docname) takes the RoutingKey position
 * and the remainder of the fields are inapplicable (except metastring).
 * Examples: freenet:KSK@foo//bar  freenet:KSK@test.html  freenet:test.html
 *
 * RoutingKey is the modified Base64 encoded key value.
 * CryptoKey is the modified Base64 encoded decryption key.
 *
 * Following the RoutingKey and CryptoKey there may be a series of
 * name=value pairs representing URI meta-information.
 *
 * The docname is only meaningful for SSKs, and is hashed with the PK
 * fingerprint to get the key value.
 *
 * The metastring is meant to be passed to the metadata processing systems
 * that act on the retrieved document.
 */
public class FreenetURI {
	
	private static Logger logger = Logger.getLogger(FreenetURI.class.getName());
    
    private String keyType, docName;
    private String[] metaStr;
    private byte[] routingKey, cryptoKey;
    private FieldSet metaInfo;

    public FreenetURI(String keyType, String docName) {
        this(keyType, docName, (String[]) null, null, null, null);
    }

    public FreenetURI(String keyType, String docName,
                      byte[] routingKey, byte[] cryptoKey) {
        this(keyType, docName, (String[]) null, null, routingKey, cryptoKey);
    }
    
    public FreenetURI(String keyType, String docName, String metaStr,
                      FieldSet metaInfo, byte[] routingKey, byte[] cryptoKey) {
        this(keyType, docName, (metaStr == null ? 
                                (String[]) null : 
                                new String[] { metaStr }), 
             metaInfo, routingKey, cryptoKey); 
        
    }

    public FreenetURI(String keyType, String docName, String[] metaStr,
                      FieldSet metaInfo, byte[] routingKey, byte[] cryptoKey) {
        this.keyType    = keyType.trim().toUpperCase();
        this.docName    = docName;
        this.metaStr    = metaStr;
        this.metaInfo   = metaInfo;
        this.routingKey = routingKey;
        this.cryptoKey  = cryptoKey;
    }

    public FreenetURI(String URI) throws MalformedURLException {
        // check scheme
        int colon  = URI.indexOf(':');
        if (colon != -1 && !URI.substring(0, colon).equalsIgnoreCase("freenet"))
            throw new MalformedURLException("Invalid scheme for Freenet URI");

        // decode keyType
        int atchar = URI.indexOf('@');
        if (atchar == -1) {
            keyType = "KSK";
            atchar  = colon;
        }
        else {
            keyType = URI.substring(colon+1, atchar).toUpperCase().trim();
        }
        URI = URI.substring(atchar + 1);
        
        // decode metaString
        int slash2;
        Vector sv = new Vector();
        while ((slash2 = URI.lastIndexOf("//")) != -1) {
            String s = urlDecode(URI.substring(slash2 + 
                                               "//".length()));
            if (s != null)
                sv.addElement(urlDecode(s));
            URI = URI.substring(0, slash2);
        }
        if (!sv.isEmpty()) {
            metaStr = new String[sv.size()];
            for (int i = 0 ; i < metaStr.length ; i++)
                metaStr[i] = (String) sv.elementAt(metaStr.length - 1 - i);
        }
        
        // decode docName
        if ("KSK".equals(keyType)) {
            docName = urlDecode(URI);
            return;
        }
        
        int slash1 = URI.indexOf('/');
        if (slash1 != -1) {
            docName = urlDecode(URI.substring(slash1 + 1));
            URI = URI.substring(0, slash1);
        }

        // URI now contains: routingKey[,cryptoKey][,metaInfo]
        StringTokenizer st = new StringTokenizer(URI, ",");
        try {
            routingKey = Base64.decode(st.nextToken());
            String t   = st.nextToken();
            if (t.indexOf('=') == -1) {
                cryptoKey = Base64.decode(t);
                t = st.nextToken();
            }
            metaInfo = new FieldSet();
            while (true) {
                int eq = t.indexOf('=');
                if (eq == -1) throw new MalformedURLException(
                    "URI meta-info must be of the form n1=v1[,n2=v2,...]"
                );
                String k = urlDecode(t.substring(0, eq));
                String v = urlDecode(t.substring(eq + 1));
                metaInfo.put(k, v);
                t = st.nextToken();
            }
        }
        catch (NoSuchElementException e) {}
        catch (IllegalBase64Exception e) {
            throw new MalformedURLException("Invalid Base64 quantity: "+e);
        }
        finally {
            if (metaInfo != null && metaInfo.isEmpty()) metaInfo = null;
        }
    }

    public void decompose() {
        String r = routingKey == null
            ? "none"
            : freenet.support.Fields.bytesToHex(routingKey, 0, routingKey.length);
        String k = cryptoKey == null
            ? "none"
            : freenet.support.Fields.bytesToHex(cryptoKey, 0, cryptoKey.length);
        StringBuffer message = 
        	new StringBuffer(this + "\n" +
							 "Key type   : " + keyType + "\n" +
							 "Routing key: " + r + "\n" +
							 "Crypto key : " + k + "\n" +
							 "Doc name   : " + (docName  == null ? "none" : docName) + "\n" +
							 "Meta strings:");
        if (metaStr == null) {
        	message.append("none\n");
        } else for (int i = 0 ; i < metaStr.length ; i++) {
			message.append(metaStr[i]);
            if (i == metaStr.length - 1) {
				message.append("\n");
            } else {
				message.append(", ");
            }
        }
        message.append("Meta info  : " + (metaInfo == null ? "none" : ""+metaInfo));
        logger.fine(message.toString());
    }

    public String getGuessableKey() {
        return getDocName();
    }

    public String getDocName() {
        return docName;
    }

    public String getMetaString() {
        return ( metaStr == null || metaStr.length == 0 ? 
                 null :
                 metaStr[0] );
    }

    public String[] getAllMetaStrings() {
        return metaStr;
    }


    public FieldSet getMetaInfo() {
        return metaInfo;
    }
    
    public byte[] getKeyVal() {
        return getRoutingKey();
    }
    
    public byte[] getRoutingKey() {
        return (byte []) routingKey;
    }
    
    public byte[] getCryptoKey() {
        return (byte []) cryptoKey;
    }

    public String getKeyType() {
        return keyType;
    }

    /**
     * Returns a copy of this URI with the first meta string removed.
     */
    public FreenetURI popMetaString() {
        String[] newMetaStr = null;
        if (metaStr != null && metaStr.length > 1) {
            newMetaStr = new String[metaStr.length - 1];
            System.arraycopy(metaStr, 1, newMetaStr, 0, newMetaStr.length);
        }
        return setMetaString(newMetaStr);
    }

    /**
     * Returns a copy of this URI with the those meta strings appended.
     */
    public FreenetURI addMetaStrings(String[] strs) {
        String[] newMetaStr;
        if (metaStr == null)
            return setMetaString(strs);
        else {
            newMetaStr = new String[metaStr.length + strs.length];
            System.arraycopy(metaStr, 0, newMetaStr, 0, metaStr.length);
            System.arraycopy(strs, 0, newMetaStr, metaStr.length, 
                             strs.length);
            return setMetaString(strs);
        }
    }

    /**
     * Returns a copy of this URI with a new Document name set.
     */
    public FreenetURI setDocName(String name) {
        return new FreenetURI(keyType, name, metaStr, metaInfo,
                              routingKey, cryptoKey);

    }


    public FreenetURI setMetaString(String[] newMetaStr) {
        return new FreenetURI(keyType, docName, newMetaStr, metaInfo,
                              routingKey, cryptoKey);
    }


    protected static String urlDecode(String s) {
        StringBuffer b=new StringBuffer();
        for (int i=0; i<s.length(); i++) {
            if (s.charAt(i)=='+')
                b.append(' ');
            else if (s.charAt(i)=='%') {
                int n=Integer.parseInt(s.substring(i+1, i+3), 16);
                b.append((char)n);
                i+=2;
            } else 
                b.append(s.charAt(i));
        }
        return b.toString();
    }

    protected static String urlEncode(String s) {
        StringBuffer b=new StringBuffer();
        for (int i=0; i<s.length(); i++) {
            if (s.charAt(i)==' ')
                b.append('+');
            else if (s.charAt(i)>128 || s.charAt(i)<44) {
                b.append('%').append(Integer.toString(s.charAt(i), 16));
            } else 
                b.append(s.charAt(i));
        }
        return b.toString();
    }

// I really don't think this is correct...
//
//    public String toURI() {
//        return toString(true);
//    }
    
    public String toString() {
	return toString(true);
    }

    public String toString(boolean prefix) {
        StringBuffer b;
	if(prefix) b = new StringBuffer("freenet:");
	else b = new StringBuffer();
	
        b.append(keyType);
        b.append('@');

        if (!"KSK".equals(keyType)) {
            if (routingKey != null)
                b.append(Base64.encode(routingKey));
            if (cryptoKey != null)
                b.append(',').append(Base64.encode(cryptoKey));
            if (metaInfo != null) {
                Enumeration keys = metaInfo.keys();
                while (keys.hasMoreElements()) {
                    String k = (String) keys.nextElement();
                    String v = metaInfo.get(k);
                    b.append(',');
                    b.append(urlEncode(k));
                    b.append('=');
                    b.append(urlEncode(v));
                }
            }
            if (docName != null) b.append('/');
        }

        if (docName != null) b.append(urlEncode(docName));
        if (metaStr != null) {
            for (int i = 0 ; i < metaStr.length ; i++) {
                b.append("//" + urlEncode(metaStr[i]));
            }
        }  
        return b.toString();
    }

    public static void main(String[] args) throws Exception {
        (new FreenetURI(args[0])).decompose();
    }
}
        




