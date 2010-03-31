/*
*   Author Martin Newstead (mseries@brundell.fsnet.co.uk).  All Rights Reserved.
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
*
*   Reaped from javax.swing.JPopupMenu with thanks
*/
package mseries.ui;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;
  /**
   * A class used to popup a window.
   * <p>
   * <strong>Warning:</strong>
   * Serialized objects of this class will not be compatible with
   * future Swing releases.  The current serialization support is appropriate
   * for short term storage or RMI between applications running the same
   * version of Swing.  A future release of Swing will provide support for
   * long term persistence.
   */
  public class MPopupDialog extends JDialog implements MPopup, FocusListener, Serializable
  {
    int saveX,saveY;
    boolean  firstShow = true;
    boolean isVisible=false;
    boolean hasShadow=false;
    Component firstComp=null;

    JButton a;

    public MPopupDialog(Frame owner)
    {
	    super(owner);
        init();
    }

    public MPopupDialog(Dialog owner)
    {
        super(owner);
        init();
    }

    public MPopupDialog()
    {
        init();
    }

    private void init()
    {
        addFocusListener(this);
        setUndecorated(true);
    }
    public void focusGained(FocusEvent e)
    {
        firstComp.requestFocus();
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_ACTIVATED));
    }

    public void focusLost(FocusEvent e)
    {
    }

    public void update(Graphics g)
    {
      paint(g);
    }

    public void setParent(Component invoker)
    {
    }

    public void setLocationOnScreen(int x,int y)
    {
      this.setLocation(x,y);
    }

    public void addComponent(Component aComponent,Object constraints)
    {
        this.getContentPane().add(aComponent,constraints);
        if (firstComp == null)
        {
            firstComp=aComponent;
        }
    }

    public void removeComponent(Component c)
    {
      this.getContentPane().remove(c);
    }

    public void setBorder(Border border)
    {
    }

    public int getWeight()
    {
        return MPopup.HEAVY;
    }

    public void setShadow(boolean hasShadow)
    {
    }

    /**
    *   Heavy weight panels can not be opaque and therefore a shadow can not be
    *   transparant.
    */
    public boolean isShadow()
    {
        return hasShadow;
    }

    public void setVisible(boolean visible)
    {
        super.setVisible(visible);
        if (visible)
        {
            toFront();
        }
    }
}
/* $Log$
/* Revision 1.1  2006-03-14 14:09:44  bback
/* new date chooser component
/*
/* Revision 1.5  2003/03/26 23:29:50  martin
/* Changed email address
/*
/* Revision 1.4  2003/03/24 19:45:07  martin
/* Latest 1.4 version
/*
/* Revision 1.2  2003/03/11 22:35:16  martin
/* Upgraded to Java 1.4 on 11/03/03
/*
/* Revision 1.1.2.1  2002/01/31 22:47:15  martin
/* New  for Java 1.4
/*
*/

