package frost.identities;

import java.io.*;

import frost.frame1;
import frost.FcpTools.FcpFactory;

/**
 * Represents the main user's identity
 */
public class LocalIdentity extends Identity implements Serializable
{
    private String privKey;

    /**
     * a constructor that assumes that the user has inserted the
     * key in his SSK already
     */
    public LocalIdentity(String name, String[] keys,String address)
    {
        super(name, address, keys[1]);
        privKey=keys[0];
    }

    /**
     * constructor that creates an RSA Keypair,
     * and inserts it in freenet
     */
    public LocalIdentity(String name)
    {
        this(name, frame1.getCrypto().generateKeys(), null);

        con = FcpFactory.getFcpConnectionInstance();
        if( con == null )
        {
            this.key=NA;
            return;
        }

        // insert pubkey to freenet
        byte[] pubkeydata1;
        try { pubkeydata1 = key.getBytes("UTF-8"); }
        catch(UnsupportedEncodingException ex) { pubkeydata1 = key.getBytes(); }
        final byte[] pubkeydata = pubkeydata1;

        try {
            String tmp = con.putKeyFromArray("CHK@", pubkeydata, null, 0, false);
            keyaddress = tmp.substring(tmp.indexOf("CHK@"),tmp.indexOf("CHK@") + 58);
            System.out.println("Calculated my public key CHK: " + keyaddress + "\n");
        }
        catch( IOException e ) {
            System.out.println("Couldn't get key CHK.");
        }

        Thread keyUploader = new Thread()
        {
            public void run()
            {
                String ret=null;
                System.out.println("Trying to upload my public key ...");
                try {
                    ret = con.putKeyFromArray("CHK@", pubkeydata, null, 25, true);
                }
                catch( IOException e ) {
                    System.out.println("... couldn't upload public key.");
                }
                System.out.println("... uploaded my public key.");
            }
        };
        keyUploader.start();
    }

    public String getPrivKey()
    {
        return privKey;
    }
}

