/*
*   Copyright (c) 2000 Martin Newstead (mseries@brundell.fsnet.co.uk).  All Rights Reserved.
*   Reaped from javax.swing.JPopupMenu
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
/**
* The following interface describes what a popup should implement.
* We do this because Popup Calendar uses popup that can be windows or
* panels.
*/
public interface MPopup
{
    /** Lightweight popup type (JPanel) */
    public static final int LIGHT=0;
    /** Mediumweight popup type (Panel) not yet implemented */
    public static final int MEDIUM=1;
    /** Heavyweight popup type (JWindow) */
    public static final int HEAVY=2;

    public void addComponent(Component aComponent,Object constraints);
    public void removeComponent(Component c);
    public void pack();
    public void setParent(Component invoker);
    public void setVisible(boolean visible);
    public boolean isVisible();
    public void setLocationOnScreen(int x,int y);
    public void requestFocus();
    public int getWeight();
    public void setShadow(boolean hasShadow);
    public boolean isShadow();
}
/* $Log$
/* Revision 1.1  2006-03-14 14:09:44  bback
/* new date chooser component
/*
/* Revision 1.9  2003/10/04 09:47:39  martin
/* *** empty log message ***
/*
/* Revision 1.8  2003/03/26 23:29:50  martin
/* Changed email address
/*
/* Revision 1.7  2002/01/31 21:31:28  martin
/* no message
/*
/* Revision 1.6  2002/01/31 21:20:04  martin
/* 131
/*
/* Revision 1.5  2002/01/31 21:14:50  martin
/* 131
/*
/* Revision 1.4  2002/01/22 20:38:15  martin
/* Tiny change
/*
/* Revision 1.3  2001/11/21 21:18:43  martin
/* removed controlMs
/*
/* Revision 1.2  2001/11/21 21:17:44  martin
/* Added Revision 1.6  2002/01/31 21:20:04  martin
/* Added 131
/* Added
/* Added Revision 1.5  2002/01/31 21:14:50  martin
/* Added 131
/* Added
/* Added Revision 1.4  2002/01/22 20:38:15  martin
/* Added Tiny change
/* Added
/* Added Revision 1.3  2001/11/21 21:18:43  martin
/* Added removed controlMs
/* Added
/* */
