/*
TofTreeCellRenderer.java / Frost
Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation; either version 2 of
the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package frost.gui;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.ToolTipManager;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import frost.*;

public class TofTreeCellRenderer extends DefaultTreeCellRenderer
{
    ImageIcon writeAccessIcon;
    ImageIcon writeAccessNewIcon;
    ImageIcon readAccessIcon;
    ImageIcon readAccessNewIcon;
    ImageIcon boardIcon;
    ImageIcon boardNewIcon;
    ImageIcon boardSpammedIcon;
    String fileSeparator;

    public TofTreeCellRenderer()
    {
        fileSeparator = System.getProperty("file.separator");
        boardIcon = new ImageIcon(frame1.class.getResource("/data/board.gif"));
        boardNewIcon = new ImageIcon(frame1.class.getResource("/data/boardnew.gif"));
        boardSpammedIcon = new ImageIcon(frame1.class.getResource("/data/boardspam.gif"));
        writeAccessIcon = new ImageIcon(frame1.class.getResource("/data/waboard.jpg"));
        writeAccessNewIcon = new ImageIcon(frame1.class.getResource("/data/waboardnew.jpg"));
        readAccessIcon = new ImageIcon(frame1.class.getResource("/data/raboard.jpg"));
        readAccessNewIcon = new ImageIcon(frame1.class.getResource("/data/raboardnew.jpg"));
        this.setLeafIcon(new ImageIcon(frame1.class.getResource("/data/board.gif")));
        this.setClosedIcon(new ImageIcon(frame1.class.getResource("/data/closed.gif")));
        this.setOpenIcon(new ImageIcon(frame1.class.getResource("/data/open.gif")));
    }

    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        String boardname = ((DefaultMutableTreeNode)value).toString();
        boolean containsNewMessage = false;
        if( frame1.getInstance().getBoardsThatContainNewMsg().get(boardname) != null )
        {
            containsNewMessage = true;
        }

        if( leaf == true )
        {
            if( isPublicBoard(boardname) )
            {
                if( containsNewMessage )
                {
                    setIcon(boardNewIcon);
                }
                else
                {
                    setIcon(boardIcon);
                }
            }
            else if( isSpammed(boardname) )
            {
                setIcon(boardSpammedIcon);
            }
            else if( isWriteAccessBoard(boardname) )
            {
                if( containsNewMessage )
                {
                    setIcon(writeAccessNewIcon);
                }
                else
                {
                    setIcon(writeAccessIcon);
                }
            }
            else if( isReadAccessBoard(boardname) )
            {
                if( containsNewMessage )
                {
                    setIcon(readAccessNewIcon);
                }
                else
                {
                    setIcon(readAccessIcon);
                }
            }
        }
        return this;
    }

    protected boolean isSpammed(String boardname)
    {
        //that should be the name
        String nodeText = mixed.makeFilename(boardname);
        if( !frame1.boardStats.containsKey(nodeText) )
        {
            return false;
        }
        else
        {
            return((BoardStat)frame1.boardStats.get(nodeText)).spammed();
        }
    }

    protected boolean isPublicBoard(String boardname)
    {
        String nodeText = mixed.makeFilename(boardname);
        String boardKeyFileName = new StringBuffer().append(frame1.keypool).append(nodeText).append(".key").toString();
        if( !(new File(boardKeyFileName).exists()) )
            return true;
        if( SettingsFun.getValue(boardKeyFileName, "state").equals("publicBoard") )
            return true;
        else
            return false;
    }

    protected boolean isWriteAccessBoard(String boardname)
    {
        String nodeText = mixed.makeFilename(boardname);
        String boardKeyFileName = new StringBuffer().append(frame1.keypool).append(nodeText).append(".key").toString();
        if( SettingsFun.getValue(boardKeyFileName, "state").equals("writeAccess") )
            return true;
        else
            return false;
    }

    protected boolean isReadAccessBoard(String boardname)
    {
        String nodeText = mixed.makeFilename(boardname);
        String boardKeyFileName = new StringBuffer().append(frame1.keypool).append(nodeText).append(".key").toString();
        if( SettingsFun.getValue(boardKeyFileName, "state").equals("readAccess") )
            return true;
        else
            return false;
    }
}
