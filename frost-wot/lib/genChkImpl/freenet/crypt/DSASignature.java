package freenet.crypt;

import java.io.*;
import java.math.BigInteger;


public class DSASignature implements CryptoElement, java.io.Serializable {

    private final BigInteger r, s;

    public DSASignature(InputStream in) throws IOException {
	r=Util.readMPI(in);
	s=Util.readMPI(in);
    }

    /**
     * Parses a DSA Signature pair from a string, where r and s are 
     * in unsigned hex-strings, separated by a comma
     */
    public DSASignature(String sig) throws NumberFormatException {
	int x=sig.indexOf(',');
	if (x <= 0)
	    throw new NumberFormatException("DSA Signatures have two values");
	r = new BigInteger(sig.substring(0,x), 16);
	s = new BigInteger(sig.substring(x+1), 16);
    }

    public static DSASignature read(InputStream in) throws IOException {
	BigInteger r, s;
	r=Util.readMPI(in);
	s=Util.readMPI(in);
	return new DSASignature(r,s);
    }

    public void write(OutputStream o) throws IOException {
	Util.writeMPI(r, o);
	Util.writeMPI(s, o);
    }
/*
	public String writeAsField() {
		return new StringBuffer(r.toString(16))
			.append(',').append(s.toString(16)).toString();
	}
*/
    /** @deprecated
      * @see toString()
      */
    public String writeAsField() {
        return toString();
    }
    
    public DSASignature(BigInteger r, BigInteger s) {
	this.r=r;
	this.s=s;
    }

    public BigInteger getR() {
	return r;
    }

    public BigInteger getS() {
	return s;
    }

    public String toString() {
        //StringBuffer sb=new StringBuffer();
        //sb.append(r.toString(16).toUpperCase()).append(',');
        //sb.append(s.toString(16).toUpperCase());
        //return sb.toString();
        return r.toString(16) + "," + s.toString(16);
    }
		  
}
