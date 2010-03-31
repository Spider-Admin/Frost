/*
*   Copyright (c) 2001 Martin Newstead (mseries@brundell.fsnet.co.uk).  All Rights Reserved.
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
package mseries.plaf.Metal;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;


public class MetalDateEntryUI extends mseries.plaf.basic.BasicDateEntryUI
{


    /**
    *    This method is called by the UIManager to get an instance of
    *    this class and must be overridden in subclasses.
    */
    public static ComponentUI createUI(JComponent x)
    {
        return new MetalDateEntryUI();
    }

    public void configureDisplay(JComponent display)
    {
        display.setBorder(null);
        //display.setBackground(UIManager.getColor("control"));
        display.setPreferredSize(new Dimension(75, 25));
    }

    protected void configureBorder(JComponent c)
    {
        c.setBorder(BorderFactory.createEtchedBorder(MetalLookAndFeel.getControlHighlight(),
                                                    MetalLookAndFeel.getControlDarkShadow() ));
    }
    protected JButton createArrowButton()
    {
        MetalArrowButton b = new MetalArrowButton(BasicArrowButton.SOUTH);
        b.drawBorder(false, true, false, false);
        b.setBorder(BorderFactory.createEtchedBorder(MetalLookAndFeel.getControlHighlight(),
                                                    MetalLookAndFeel.getControlDarkShadow() ));
        return b;
    }
}
