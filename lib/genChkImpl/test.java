import freenet.client.*;
import freenet.support.*;
import java.io.*;
/*
fred:
    URI=freenet:CHK@9K9f5xmqG0253v9bPYPGLuvOswIRAwI,MU2T9hPghQwkzn58Vs2G5A
own:
    chk=freenet:CHK@9K9f5xmqG0253v9bPYPGLuvOswIRAwI,MU2T9hPghQwkzn58Vs2G5A
*/

public class test
{
    public static void main(String[] argv) throws Throwable
    {
        new test().run();
    }

    public void run() throws Throwable
    {
System.out.println("Starting");
        File input = new File("frost_japanese.jpg");
        long size = input.length();

        ClientCHK chk = new ClientCHK();
//        chk.setCipher("Twofish");

        File tempfile = new File("tempfile.1");
        RandomAccessFile raf = new RandomAccessFile(tempfile,"rw");
        raf.setLength( chk.getTotalLength( size ) );
        raf.close();
        FileBucket ctBucket = new FileBucket(tempfile);

        Bucket data = new FileBucket(input);

        try {
            chk.encode(data, 0, ctBucket).close();
        }
        finally {
            ctBucket.getFile().delete();
        }
        String chkKey = chk.getURI().toString();
        System.out.println("chk="+chkKey);
    }
}