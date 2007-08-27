package freenet.crypt;

import java.io.*;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.logging.*;

public abstract class CryptoKey implements CryptoElement, Serializable {

    protected static final Digest shactx=SHA1.getInstance();
    
	private static Logger logger = Logger.getLogger(CryptoKey.class.getName());
    
    //    protected final CryptoKey dependent;

    CryptoKey() {
	//	dependent=null;
    }

    /*
      CryptoKey(CryptoKey dependent) {
      this.dependent=dependent;
      }
    */

    public static CryptoKey read(InputStream i) throws IOException {
	DataInputStream dis=new DataInputStream(i);
	String type=dis.readUTF();
	/*	boolean dep=dis.readBoolean();
		CryptoKey dependent=null;

	if (dep) {
	    dependent=read(dis);
	}
	*/
	try {
	    Class keyClass=Class.forName(type);
	    Method m=
		/*(dep ?
		 keyClass.getMethod("read", 
				    new Class[] { DataInputStream.class,
						  CryptoKey.class })
						  :*/
		 keyClass.getMethod("read", 
				    new Class[] { InputStream.class });//);
	    CryptoKey k=null;
	    k=(CryptoKey)m.invoke(null, 
				/*  (dep ? 
				    new Object[] {dis, dependent} :*/
				  new Object[] {dis});//);

	    return k;
	} catch (Exception e) {
		logger.log(Level.SEVERE, "Exception thrown in read(InputStream i)", e);
	    if (e instanceof IOException)
		throw (IOException)e;
	    return null;
	}
    }

    public abstract void write(OutputStream o) throws IOException;

    public abstract String keyType();
    public abstract byte[] fingerprint();
    public abstract byte[] asBytes();

    protected byte[] fingerprint(BigInteger[] quantities) {
	synchronized(shactx) {
	    for (int i=0; i<quantities.length; i++) {
		byte[] mpi=Util.MPIbytes(quantities[i]);
		shactx.update(mpi, 0, mpi.length);
	    } 
	    return shactx.digest();
	}
    }

    public String verboseToString() {
	StringBuffer b=new StringBuffer();
	b.append(toString()).append('\t').append(fingerprintToString());
	/*	if (dependent!=null)
		b.append("\n \\_").append(dependent.verboseToString());*/
	return b.toString();
    }

    public String toString() {
	StringBuffer b=new StringBuffer();
	b.append(keyType()).append('/');
	b.append(freenet.support.Fields.bytesToHex(fingerprint(), 16, 4));
	return b.toString();
    }

    protected DataOutputStream write(OutputStream o, 
				     String clazz) throws IOException {
	DataOutputStream dos=new DataOutputStream(o);
	dos.writeUTF(clazz);
	return dos;
	/*	o.writeBoolean(dependent!=null);
	if (dependent!=null) 
	dependent.write(o);*/
    }


    public String fingerprintToString() {
	byte[] fingerprint=fingerprint();
	String fphex=freenet.support.Fields.bytesToHex(fingerprint, 0, 
						       fingerprint.length);
	StringBuffer b=new StringBuffer();
	b.append(fphex.substring(0, 4)).append(' ');
	b.append(fphex.substring(4, 8)).append(' ');
	b.append(fphex.substring(8, 12)).append(' ');
	b.append(fphex.substring(12, 16)).append(' ');
	b.append(fphex.substring(16, 20)).append("  ");
	b.append(fphex.substring(20, 24)).append(' ');
	b.append(fphex.substring(24, 28)).append(' ');
	b.append(fphex.substring(28, 32)).append(' ');
	b.append(fphex.substring(32, 36)).append(' ');
	b.append(fphex.substring(36, 40));
	return b.toString();
    }

    public static void main(String[] args) throws Exception {
	for (;;) {
	    CryptoKey kp=CryptoKey.read(System.in);
	    System.err.println("-+ " + kp.verboseToString());
	}
    }
}
