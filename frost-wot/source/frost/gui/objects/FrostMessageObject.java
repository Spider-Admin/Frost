package frost.gui.objects;

import java.io.*;

import frost.*;

public class FrostMessageObject extends VerifyableMessageObject implements FrostMessage
{
    public FrostMessageObject()
    {
        super();
    }
    public FrostMessageObject(File file)
    {
        super(file);
    }
    public FrostMessageObject(String filename)
    {
        this(new File(filename));
    }
}