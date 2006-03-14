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
*
*   Thanks to James Shiell for supplying amendments to this for Win XP Look & Feel in J2SE 1.4.2
*/
package mseries.plaf.Windows;

import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.*;
import java.awt.*;


public class WindowsDateEntryUI extends mseries.plaf.basic.BasicDateEntryUI
{
    private final ComboBoxUI comboBoxUI = new ComboBoxUI();

    /**
    *    This method is called by the UIManager to get an instance of
    *    this class and must be overridden in subclasses.
    */
    public static ComponentUI createUI(JComponent x)
    {
        //System.out.println("WindowsDateEntryUI");
        return new WindowsDateEntryUI();
    }

    public void configureDisplay(JComponent display)
    {
        display.setBorder(null);
        display.setPreferredSize(new Dimension(75, 21));
    }
    public void uninstallUI(JComponent c)
    {
        super.uninstallUI(c);
        dateEntry.setBorder(null);
    }
    protected void configureBorder(JComponent c)
    {
        c.setBorder(UIManager.getBorder("TextField.border"));
    }
    protected JButton createArrowButton()
    {
        return comboBoxUI.createArrowButton();
    }

    // hack to get createArrowButton
    private class ComboBoxUI extends WindowsComboBoxUI
    {

        public JButton createArrowButton()
        {
            return super.createArrowButton();
        }

    }
}
