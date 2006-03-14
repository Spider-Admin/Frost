/*
*   Copyright (c) 2000 Martin Newstead (mseries@brundell.fsnet.co.uk).  All Rights Reserved.
*
*   The author makes no representations or warranties about the suitability of the
*   software, either express or implied, including but not limited to the
*   implied warranties of merchantability, fitness for a particular
*   purpose, or non-infringement. The author shall not be liable for any damages
*   suffered by licensee as a result of using, modifying or distributing
*   this software or its derivatives.
*
*   The author requests that he be notified of any application, applet, or other binary that
*   makes use of this code and that some acknowedgement is given. Comments, questions and
*   requests for change will be welcomed.
*/
package mseries.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

public class ScreenUtilities
{


    /**
    *   Finds the parent container (Window, JRootPane) of the component. This is useful
    *   for drawing components in the container since all co-ords are reltive to the component
    *   and not its parent
    *   @param c the component
    *   @return its parent
    */
    public static Container getParentWindow(Component c)
    {
        Container parent = null;
        if (c != null)
        {
            parent = c.getParent();
        }

        for(Container p = parent; p != null; p = p.getParent())
        {
            if(p instanceof JRootPane)
            {
                    if(p.getParent() instanceof JInternalFrame)
                {
                        continue;
                }

                parent = ((JRootPane)p).getLayeredPane();
                for(p = parent.getParent(); p != null && (!(p instanceof java.awt.Window));
                p = p.getParent());
                break;
            }
            else if(p instanceof Window)
            {
                parent = p;
                break;
            }
        }
        return parent;
    }

    /**
    *   Given a point ona screen this method calculates the absolute point in the
    *   parent container (Frame)
    */
    public static Point convertScreenLocationToParent(Container parent,int x,int y)
    {
        Window parentWindow = null;
        for(Container p = parent; p != null; p = p.getParent())
        {
            if(p instanceof Window)
            {
                parentWindow = (Window)p;
                break;
            }
        }
        if(parentWindow != null)
        {
            Point p = new Point(x,y);
            SwingUtilities.convertPointFromScreen(p,parent);
            return p;
        }
        else
        {
            throw new Error("convertScreenLocationToParent: no window ancestor found");
        }
    }

    /**
    *   @return a Dialog that is the first one in the hierarchy
    *   containing the component passed component
    */
    public static Dialog getParentDialog(Component child)
    {
        Dialog parentWindow = null;
        for(Container p = child.getParent(); p != null; p = p.getParent())
        {
            if(p instanceof Dialog)
            {
                parentWindow = (Dialog)p;
                break;
            }
        }
        return parentWindow;
    }

    /**
    *   @return a Frame that is the first one in the hierarchy
    *   containing the component passed component
    */
    public static Frame getParentFrame(Component child)
    {
        Frame parentWindow = null;
        for(Container p = child.getParent(); p != null; p = p.getParent())
        {
            if(p instanceof Frame)
            {
                parentWindow = (Frame)p;
                break;
            }
        }
        return parentWindow;
    }

    /**
    *   @return a set of KeyStrokes for traversal of custom components
    */
    public static HashSet getDefaultFocusTraversalKeys(int id)
    {
        HashSet keys=new HashSet();

        switch (id)
        {
            case KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS:
                keys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.CTRL_MASK));
                break;
            case KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS:
                keys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.CTRL_MASK+InputEvent.SHIFT_MASK));
                break;
        }
        return keys;

    }
}
/*
$:Log$
*/
