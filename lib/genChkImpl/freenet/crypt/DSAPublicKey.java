package freenet.crypt;

import java.io.*;
import java.math.BigInteger;

public class DSAPublicKey extends CryptoKey {
    
    protected final BigInteger y;
    protected final DSAGroup group;
    
    public DSAPublicKey(DSAGroup g, BigInteger y) {
	this.y=y;
	this.group=g;
    }

    public DSAPublicKey(DSAGroup g, DSAPrivateKey p) {
	this(g,g.getG().modPow(p.getX(), g.getP()));
    }

    public BigInteger getY() {
	return y;
    }

    public BigInteger getP() {
	return group.getP();
    }

    public BigInteger getQ() {
	return group.getQ();
    }

    public BigInteger getG() {
	return group.getG();
    }

    public String keyType() {
	return "DSA.p";
    }

    // Nope, this is fine    
    public DSAGroup getGroup() {
	return group;
    }

    public void write(OutputStream out) throws IOException {
	DataOutputStream o=writeWithoutGroup(out);
	group.write(out);
    }

    public void writeForWireWithoutGroup(OutputStream out) throws IOException {
	Util.writeMPI(y, out);
    }

    public void writeForWire(OutputStream out) throws IOException {
	Util.writeMPI(y, out);
	group.writeForWire(out);
    }

    public DataOutputStream writeWithoutGroup(OutputStream out) 
	throws IOException {
	DataOutputStream o=write(out, getClass().getName());
	Util.writeMPI(y, o);
	return o;
    }

    public static CryptoKey read(InputStream i) throws IOException {
	BigInteger y=Util.readMPI(i);
	DSAGroup g=(DSAGroup)CryptoKey.read(i);
	return new DSAPublicKey(g, y);
    }

    public int keyId() {
	return y.intValue();
    }

    public String writeAsField() {
        return y.toString(16);
    }

    // this won't correctly read the output from writeAsField
    //public static CryptoKey readFromField(DSAGroup group, String field) {
    //    BigInteger y=Util.byteArrayToMPI(Util.hexToBytes(field));
    //    return new DSAPublicKey(group, y);
    //}

    public byte[] asBytes() {
	byte[] groupBytes=group.asBytes();
	byte[] ybytes=Util.MPIbytes(y);
	byte[] bytes=new byte[groupBytes.length + ybytes.length];
	System.arraycopy(groupBytes, 0, bytes, 0, groupBytes.length);
	System.arraycopy(ybytes, 0, bytes, groupBytes.length, ybytes.length);
	return bytes;
    }

    public byte[] fingerprint() {
	return fingerprint(new BigInteger[] {y});
    }
	
    public boolean equals(DSAPublicKey o) {
	return y.equals(o.y) && group.equals(o.group);
    }

    public boolean equals(Object o) {
	return (o instanceof DSAPublicKey)
	    && y.equals(((DSAPublicKey) o).y)
	    && group.equals(((DSAPublicKey) o).group);
    }
    
    public int compareTo(Object other) {
	if (other instanceof DSAPublicKey) {
	    return getY().compareTo(((DSAPublicKey)other).getY());
	} else return -1;
    }
}
