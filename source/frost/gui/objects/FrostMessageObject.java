package frost.gui.objects;

import java.io.File;

import frost.messages.VerifyableMessageObject;

public class FrostMessageObject extends VerifyableMessageObject implements FrostMessage
{
    /**
     * Constrcutor can be used to build an empty message for uploading.
     */
    public FrostMessageObject()
    {
        super();
    }
    /**
     * This constructor can be used to build a messageobject from
     * an existing file.
     * 
     * @param file  The xml file to read
     * @throws Exception  If the file could'nt be loaded
     */
    public FrostMessageObject(File file) throws Exception
    {
        super(file);
    }
    /**
     * This constructor can be used to build a messageobject from
     * an existing file.
     * 
     * @param file  The xml file to read
     * @throws Exception  If the file could'nt be loaded
     */
    public FrostMessageObject(String filename) throws Exception
    {
        this(new File(filename));
    }
}