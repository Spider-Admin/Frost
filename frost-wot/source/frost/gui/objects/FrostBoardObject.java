package frost.gui.objects;

import javax.swing.tree.*;

public class FrostBoardObject extends DefaultMutableTreeNode implements FrostBoard
{
    private String boardName = null;

    public FrostBoardObject()
    {
        super();
    }

    public String getBoardName()
    {
        return boardName;
    }
}
