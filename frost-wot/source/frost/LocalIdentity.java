package frost;
import java.util.*;
import frost.FcpTools.*;
import frost.crypt.*;
import java.io.*;
import javax.swing.*;

/**
 * represents the main user's identity
 */
public class LocalIdentity extends Identity implements Serializable
{
    private String privKey;
    //private transient final int htl;
    public String getPrivKey()
    {
        return privKey;
    }

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
     *
     */
    public LocalIdentity(String name)
    {
        this(name, frame1.getCrypto().generateKeys(), null);

	try{
	con = new FcpConnection(frame1.frostSettings.getValue("nodeAddress"), frame1.frostSettings.getValue("nodePort"));
	}catch (FcpToolsException e) {System.out.println("fcptools exception");this.key=NA;}
	catch (IOException e){this.key=NA;}
	
        //this is not so brutal anymore.
        FileAccess.writeFile(key,"pubkey.txt");
        try
        {
            String tmp =con.putKeyFromFile("CHK@", "pubkey.txt",0, false);
            keyaddress = tmp.substring(tmp.indexOf("CHK@"),tmp.indexOf("CHK@") + 58);
            System.out.println("\ncalculated my public key CHK: " + keyaddress + "\n");
        }
        catch( IOException e )
        {
            System.out.println("couldn't get key CHK");
        }
        Thread keyUploader = new Thread()
        {
            public void run()
            {
                String ret=null;
                //String nick = JOptionPane.showInputDialog("key upload htl? choose something between 15 and 20\n");
                //int htl = (new Integer (nick)).intValue();
                System.out.println("trying to upload public key");
                try
                {
                    ret =con.putKeyFromFile("CHK@", "pubkey.txt",25, true);
                }
                catch( IOException e )
                {
                    System.out.println("couldn't upload public key");
                }
                //System.out.println("ret is " +ret);
                System.out.println("\nuploaded my public key : " + keyaddress + "\n");
            }
        };
        keyUploader.start();
        // String set= con.putKeyFromFile(keyaddress, "pubkey.txt",15, true);
        //       System.out.println("set is " +set);
    }
}
