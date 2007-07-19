/*
*   Copyright (c) 2003 Martin Newstead (mseries@brundell.fsnet.co.uk).  All Rights Reserved.
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
*   Created on Jan 14, 2003 at 7:40:42 PM by martin
*   Commited on $Date$
*/
package mseries.plaf.Mac;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

public class MacDateEntryUI extends mseries.plaf.basic.BasicDateEntryUI
{
    public MacDateEntryUI()
    {
        super();
    }

    /**
    *    This method is called by the UIManager to get an instance of
    *    this class and must be overridden in subclasses.
    */
    public static ComponentUI createUI(JComponent x)
    {
        return new MacDateEntryUI();
    }

    public void configureDisplay(JComponent display)
    {
        display.setBorder(UIManager.getBorder("TextField.border"));
        display.setPreferredSize(new Dimension(75, 21));
    }

    public void uninstallUI(JComponent c)
    {
        super.uninstallUI(c);
        dateEntry.setBorder(null);
    }

    protected void configureBorder(JComponent c)
    {
    }

    protected JButton createArrowButton()
    {
        JButton x=new MacArrowButton(SwingConstants.SOUTH);
        return x;
    }
}
