package freenet.crypt;

import java.io.*;
import java.math.BigInteger;
import java.util.StringTokenizer;

/**
 * Holds a Diffie-Hellman key-exchange group
 */
public class DHGroup extends CryptoKey {

    public final BigInteger p,g;

    public DHGroup(BigInteger p, BigInteger g) {
	this.p=p;
	this.g=g;
    }

    public void write(OutputStream out) throws IOException {
	super.write(new DataOutputStream(out), getClass().getName());
    }

    public String writeAsField() {
	StringBuffer b=new StringBuffer();
	b.append(p.toString(16)).append(',');
	b.append(g.toString(16));
	return b.toString();
    }

    public static CryptoKey readFromField(String field) {
	BigInteger p,q,g;
	StringTokenizer str=new StringTokenizer(field, ",");
	p=Util.byteArrayToMPI(Util.hexToBytes(str.nextToken()));
	g=Util.byteArrayToMPI(Util.hexToBytes(str.nextToken()));
	return new DHGroup(p,g);
    }

    public static CryptoKey read(DataInputStream i) throws IOException {
	BigInteger p,g;
	p=Util.readMPI(i);
	g=Util.readMPI(i);
	return new DHGroup(p,g);
    }

    public BigInteger getP() {
	return p;
    }

    public BigInteger getG() {
	return g;
    }

    public String keyType() {
	return "DHG-"+p.bitLength();
    }

    public byte[] fingerprint() {
	return fingerprint(new BigInteger[] {p,g});
    }

    public byte[] asBytes() {
	byte[] pb = Util.MPIbytes(p);
	byte[] gb = Util.MPIbytes(g);
	byte[] tb = new byte[pb.length + gb.length];
	System.arraycopy(pb, 0, tb, 0, pb.length);
	System.arraycopy(gb, 0, tb, pb.length, gb.length);
	return tb;
    }
}






