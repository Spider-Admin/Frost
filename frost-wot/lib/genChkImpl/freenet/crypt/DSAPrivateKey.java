package freenet.crypt;

import java.io.*;
import java.math.BigInteger;
import java.util.Random;

public class DSAPrivateKey extends CryptoKey {

    private final BigInteger x;

    public DSAPrivateKey(BigInteger x) {
        this.x = x;
    }

    // this is dangerous...  better to force people to construct the
    // BigInteger themselves so they know what is going on with the sign
    //public DSAPrivateKey(byte[] x) {
    //    this.x = new BigInteger(1, x);
    //}

    public DSAPrivateKey(DSAGroup g, Random r) {
        BigInteger x;
        do {
            x = new BigInteger(160, r);
        } while (x.compareTo(g.getQ()) > -1);
        this.x = x;
    }

    public String keyType() {
        return "DSA.s";
    }
    
    public BigInteger getX() {
        return x;
    }
    
    public static CryptoKey read(InputStream i) throws IOException {
        return new DSAPrivateKey(Util.readMPI(i));
    }
    
    public void write(OutputStream out) throws IOException {
        DataOutputStream o=write(out, getClass().getName());
        Util.writeMPI(x, out);
    }
    
    public String writeAsField() {
        return x.toString(16);
    }
    
    // what?  why is DSAGroup passed in?
    //public static CryptoKey readFromField(DSAGroup group, String field) {
    //    //BigInteger x=Util.byteArrayToMPI(Util.hexToBytes(field));
    //    return new DSAPrivateKey(new BigInteger(field, 16));
    //}
    
    public byte[] asBytes() {
        return Util.MPIbytes(x);
    }
    
    public byte[] fingerprint() {
        return fingerprint(new BigInteger[] {x});
    }
    
    public static void main(String[] args) throws Exception {
        Yarrow y=new Yarrow();
        DSAPrivateKey p=new DSAPrivateKey(Global.DSAgroupC, y);
        DSAPublicKey pk=new DSAPublicKey(Global.DSAgroupC, p);
        p.write(System.out);
    }
}

