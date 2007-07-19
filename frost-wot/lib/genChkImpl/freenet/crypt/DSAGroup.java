package freenet.crypt;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Logger;

/**
 * Holds DSA group parameters.  These are the public (possibly shared) values
 * needed for the DSA algorithm
 */
public class DSAGroup extends CryptoKey {

	private static Logger logger = Logger.getLogger(DSAGroup.class.getName());
    private BigInteger p,q,g;
    private String pAsHexString,gAsHexString,qAsHexString; //Cached versions of the hexadecimal string representations of p,q and g 

    public DSAGroup(BigInteger p, BigInteger q, BigInteger g) {
		this.p=p;
		this.q=q;
		this.g=g;
		updateCachedHexStrings();
    }
    
    private void updateCachedHexStrings()
    {
		pAsHexString = p.toString(16);
		qAsHexString = q.toString(16);	
		gAsHexString = g.toString(16);		
    }

    /**
     * Parses a DSA Group from a string, where p, q, and g are 
     * in unsigned hex-strings, separated by a commas
     */
    // see readFromField() below
    //public static DSAGroup parse(String grp) {
    //    StringTokenizer str=new StringTokenizer(grp, ",");
    //    BigInteger p,q,g;
    //    p = new BigInteger(str.nextToken(), 16);
    //    q = new BigInteger(str.nextToken(), 16);
    //    g = new BigInteger(str.nextToken(), 16);
    //    return new DSAGroup(p,q,g);
    //}

    public static CryptoKey read(InputStream i) throws IOException {
	BigInteger p,q,g;
	p=Util.readMPI(i);
	q=Util.readMPI(i);
	g=Util.readMPI(i);
	return new DSAGroup(p,q,g);
    }
	
    public void writeForWire(OutputStream out) throws IOException {
        Util.writeMPI(p,out);
        Util.writeMPI(q,out);
        Util.writeMPI(g,out);
    }

    public void write(OutputStream out) throws IOException {
		DataOutputStream dos=write(out, getClass().getName());
		writeForWire(out);
    }

    public String writeAsField() {
		StringBuffer b=new StringBuffer();
		b.append(pAsHexString).append(',');
		b.append(qAsHexString).append(',');
		b.append(gAsHexString);
		return b.toString();
    }

    public static DSAGroup readFromField(String field) {
		BigInteger p,q,g;
		StringTokenizer str=new StringTokenizer(field, ",");
		if (str.countTokens() != 3)
			throw new NumberFormatException();
		p = new BigInteger(str.nextToken(), 16);
		q = new BigInteger(str.nextToken(), 16);
		g = new BigInteger(str.nextToken(), 16);
		DSAGroup r = new DSAGroup(p,q,g);
		return (r.equals(Global.DSAgroupA) ?
				Global.DSAgroupA :
				(r.equals(Global.DSAgroupB) ?
				 Global.DSAgroupB :
				 (r.equals(Global.DSAgroupC) ?
				  Global.DSAgroupC :
				  r)));
    }

    public String keyType() {
	return "DSA.g-"+p.bitLength();
    }

    public BigInteger getP() {
	return p;
    }

    public BigInteger getQ() {
	return q;
    }

    public BigInteger getG() {
	return g;
    }
    
	public String getPAsHexString() {
		return pAsHexString;
	}
	
	public String getQAsHexString() {
		return qAsHexString;
	}
		
	public String getGAsHexString() {
		return gAsHexString;
	}


    public byte[] fingerprint() {
	BigInteger fp[]=new BigInteger[3];
	fp[0]=p; fp[1]=q; fp[2]=g;
	return fingerprint(fp);
    }

    static class QG extends Thread {
	public Vector qs=new Vector();
	protected Random r;
	
	public QG(Random r) {
	    setDaemon(true);
	    this.r=r;
	}

	public void run() {
	    while(true) {
		qs.addElement(makePrime(160,80,r));
		synchronized(this) {
		    notifyAll();
		}
		while (qs.size()>=3) {
		    synchronized(this) {
			try {
			    wait(50);
			} catch (InterruptedException ie) {}
		    }
		}
	    }
	}
    }

    static BigInteger smallPrimes[]=new BigInteger[]
	{
	    BigInteger.valueOf(3),
	    BigInteger.valueOf(5), BigInteger.valueOf(7),
	    BigInteger.valueOf(11), BigInteger.valueOf(13), 
	    BigInteger.valueOf(17), BigInteger.valueOf(19), 
	    BigInteger.valueOf(23), BigInteger.valueOf(29) 
	};

    public static BigInteger makePrime(int bits, int confidence, Random r) {
	BigInteger rv;
	do {
	    rv=new BigInteger(bits, r).setBit(0).setBit(bits-1);
	} while (!isPrime(rv, confidence));
	return rv;
    }

    public static boolean isPrime(BigInteger b, int confidence) {
	for (int i=0; i<smallPrimes.length; i++) {
	    if (b.mod(smallPrimes[i]).equals(Util.ZERO))
		return false;
	}
	return b.isProbablePrime(80);
    }

    static boolean multithread=true;

    public static DSAGroup generate(int bits, Random r) {
	BigInteger p,q,g;
	int cc=0;
	QG qg=null;
	if (multithread) {
	    qg=new QG(r);
	    qg.start();
	}
	   
    step1:
	do {
	    if ((cc++)%15 == 0)
		logger.finer(".");
	    if (multithread) {
		while (qg.qs.size() < 1) {
		    try {
			synchronized(qg) {
			    qg.wait(50);
			}
		    } catch (InterruptedException ie) {}
		} 
		q=(BigInteger)qg.qs.elementAt(0);
		qg.qs.removeElementAt(0);

		synchronized(qg) {
		    qg.notify();
		}
	    } else 
		q=makePrime(160, 80, r);

	    BigInteger X=new BigInteger(bits, r).setBit(bits-1);

	    BigInteger c=X.mod(q.multiply(Util.TWO));
	    p=X.subtract(c.subtract(Util.ONE));
	    if (isPrime(p,80))
		break;
	} while (true);
	qg.qs.trimToSize();
	BigInteger pmin1=p.subtract(Util.ONE);
	BigInteger h;
	do {
	    if ((cc++)%5 == 0)
		logger.finer("+");
	    h=new BigInteger(160, r);
	    g=h.modPow(pmin1.divide(q), p);
	} while ((h.compareTo(p.subtract(Util.ONE)) != -1) ||
		 (h.compareTo(Util.ONE) < 1) ||
		 (g.compareTo(Util.ONE) == 0) ||
		 (g.bitLength() != bits));	
	return new DSAGroup(p,q,g);
    }
	/*
	SHA1 ctx=new SHA1();
	BigInteger twoToLmin1=Util.TWO.pow(1023);

	int n=(bits-1)/160;
	int b=(bits-1)-(160*n);

	//Lifted constants
	BigInteger n160=Util.TWO.pow(160*n);
	BigInteger bx2=Util.TWO.pow(b);

	BigInteger i160[]=new BigInteger[n];
	for (int i=0; i<n; i++) {
	    i160[i]=Util.TWO.pow(160*i);
	}

	int p1c=0, p2c=0;
	BigInteger S, q, twoToG;
	do {
	    if (p1c++%10==0)
		System.err.print(".");
	    S=new BigInteger(256, r);

	    if (DEBUG) System.err.println("Step1: S="+S.toString(16));
	    int g=S.bitLength();
	    twoToG=Util.TWO.pow(g);
	    if (DEBUG) System.err.println("Step1: g="+g);
	    byte[] h1,h2;
	    h1=hash(ctx, S);
	    h2=hash(ctx, S.add(Util.ONE).mod(twoToG));

	    byte[] U=xor(h1, h2);

	    
	    if (DEBUG) System.err.println("Step2: U="+Util.byteArrayToMPI(U).toString(16));
	    U[0]|=(byte)0x80;
	    U[19]|=(byte)0x01;

	    q=Util.byteArrayToMPI(U);
			
	    if (DEBUG) System.err.println("Step3: q="+q.toString(16));	    
	} while (q.bitLength()!=160 || !q.isProbablePrime(80));
	
	int C=0, N=2;
	//Lifted constants
	BigInteger qx2=Util.TWO.multiply(q);

	BigInteger[] V=new BigInteger[n+1];
	step7: 
	do {
	    for (int k=0; k<=n; k++) {
		V[k]=Util.byteArrayToMPI(
		      hash(ctx, S.add(BigInteger.valueOf(N+k))	      
			   .mod(twoToG)));
				
		
		if (DEBUG) System.err.println("Step7: V["+k+"]="+V[k].toString(16));
	    }
	    
	    BigInteger W=Util.ZERO;
	    for (int i=0; i<n; i++) {
		BigInteger T=i160[i].multiply(V[i]);
		//		System.err.println("T: "+T.toString(16));
		W=W.add(T);
	    }
	    //	    System.err.println("!"+bx2.toString(16)+" "+V[n].mod(bx2));
	    //	    System.err.println("T: "+n160.multiply(V[n].mod(bx2)).toString(16));
	    W=W.add(n160.multiply(V[n].mod(bx2)));
	    if (DEBUG) System.err.println("Step8: W="+W.toString(16));
	    
	    BigInteger X=W.add(twoToLmin1);
	    if (DEBUG) System.err.println("Step8: X="+X.toString(16));
	    
	    BigInteger pTmp=X.mod(qx2).subtract(Util.ONE);
	    BigInteger p=X.subtract(pTmp);
	    if (DEBUG) System.err.println("Step9: p="+p.toString(16));

	    if (p.compareTo(twoToLmin1) != -1) {
		if (p2c++%20==0)
		    System.err.print("+");

		BigInteger pmin1=p.subtract(Util.ONE);
		
		if (p.isProbablePrime(80) &&
		    pmin1.divide(Util.TWO).isProbablePrime(80)) {
		    System.err.println("\nS=0x"+S.toString(16));
		    System.err.println("C="+C);
		    System.err.println("N="+N);
		    
		    BigInteger h, g;
		    
		    do {
			h=new BigInteger(bits, r);
			g=h.modPow(pmin1.divide(q), p);
		    } while ((h.compareTo(pmin1) != -1) ||
			     (g.compareTo(Util.ONE) < 1));
		    return new DSAGroup(p, q, g);
		} else {
		    C++;
		    N+=n+1;
		    if (DEBUG) System.err.println("Step13: C="+C+", N="+N);
		    if (C==4096)
			return generate(bits, r);
		    else
			continue step7;
		}
	    }
	} while (true);
    }

    public static byte[] xor(byte[] b1, byte[] b2) {
	byte[] res=new byte[b1.length];
	for (int i=0; i<b1.length; i++) {
	    res[i]=(byte)(b1[i]^b2[i]);
	}
	return res;
    }

    public static byte[] hash(SHA1 ctx, BigInteger v) {
	byte[] vb=v.toByteArray();
	int offs=vb[0]==0 ? 1 : 0;
	boolean zeroFirstByte=vb[0]==0;
	ctx.update(vb, offs, vb.length-offs);
	return ctx.digest();
    }

    public static byte[] hash(SHA1 ctx, byte[] val) {
	ctx.update(val, 0, val.length);
	byte[] res=ctx.digest();
	return res;
    }
	*/

    public static boolean testGroup(DSAGroup grp) {
	BigInteger p,q,g;
	p=grp.getP();
	q=grp.getQ();
	g=grp.getG();
	BigInteger pmin1=p.subtract(Util.ONE);
	boolean rv=
	    !(p.bitLength() > 1024 || p.bitLength() < 512) &&
	    (p.bitLength() % 64) == 0 &&
	    q.bitLength() == 160 &&
	    q.compareTo(p) == -1 &&
	    isPrime(p, 80) &&
	    isPrime(q, 80) &&
	    pmin1.mod(q).equals(Util.ZERO) &&
	    g.compareTo(Util.ONE) == 1 &&
	    !g.equals(pmin1.modPow(pmin1.divide(q), p));
	return rv;
    }
	
    public static void main(String[] args) throws IOException {
	if (args[0].equals("test")) {
	    System.out.print("GroupA: ");
	    System.out.println(testGroup(Global.DSAgroupA));
	    System.out.print("GroupB: ");
	    System.out.println(testGroup(Global.DSAgroupB));
	    System.out.print("GroupC: ");
	    System.out.println(testGroup(Global.DSAgroupC));
	} else {
	    DSAGroup g=generate(Integer.parseInt(args[0]), 
				new Yarrow("/dev/urandom","SHA1","Rijndael"));
	    System.err.print("\nVerifying group: ");
	    System.err.println(testGroup(g) ? "passed" : "failed");
	    g.write(System.out);
	    //	    System.out.println("Identity.group="+g.writeAsField());
	}
    }

    public byte[] asBytes() {
	byte[] pb = Util.MPIbytes(p);
	byte[] qb = Util.MPIbytes(q);
	byte[] gb = Util.MPIbytes(g);
	byte[] tb = new byte[pb.length + qb.length + gb.length];
	System.arraycopy(pb, 0, tb, 0, pb.length);
	System.arraycopy(qb, 0, tb, pb.length, qb.length);
	System.arraycopy(gb, 0, tb, pb.length + qb.length, gb.length); 
	return tb;
    }

	public boolean equals(Object o) {
		return (o instanceof DSAGroup)
			&& p.equals(((DSAGroup) o).p)
			&& q.equals(((DSAGroup) o).q)
			&& g.equals(((DSAGroup) o).g);
	}
	
	public boolean equals(DSAGroup o) {
		return p.equals(o.p) && q.equals(o.q) && g.equals(o.g);
	}

    public void destroy() {
	p=q=g=null;
    }
}
