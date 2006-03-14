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
/**
*   Layout manager for a comobox style display with
*   the arrow button to the right and the editor to the left.
*
*/
package mseries.utils;
import java.awt.*;

public class MComboBoxLayout implements LayoutManager
{
    public void addLayoutComponent(String name, Component comp) {}

    public void removeLayoutComponent(Component comp) {}

    private Component display, pushButton;
    Insets insets;
    private int width, height;

    public Dimension preferredLayoutSize(Container parent)
    {
        display = parent.getComponent(0);
        Dimension d = new Dimension(display.getPreferredSize().width+20, display.getPreferredSize().height);
        return d;
    }

    public Dimension minimumLayoutSize(Container parent)
    {
        display = parent.getComponent(0);
        Dimension d = new Dimension(display.getMinimumSize().width+20, display.getMinimumSize().height);
        return d;
    }

    public void layoutContainer(Container parent)
    {
        int n = parent.getComponentCount();
        if (n>0)
        {
            display = parent.getComponent(0);
        }
        if (n>1)
        {
            pushButton=parent.getComponent(1);
        }

        width = parent.getSize().width;
        height = parent.getSize().height;
        insets = parent.getInsets();

        Rectangle cvb;

        if (pushButton != null)
        {
            int buttonSize = height - (insets.top + insets.bottom);
            pushButton.setBounds(width - (insets.right + buttonSize),
                                   insets.top,
                                   buttonSize, buttonSize);
        }
        if (display != null)
        {
            cvb = rectangleForCurrentValue();
            display.setBounds(cvb);
        }
    }

    /**
    * Returns the area that is reserved for drawing the currently selected item.
    */
    protected Rectangle rectangleForCurrentValue()
    {
        int buttonSize=0;
        if ( pushButton != null )
        {
            buttonSize = pushButton.getSize().width;
        }
        Rectangle r= new Rectangle(insets.left, insets.top,
                            width - (insets.left + insets.right + buttonSize),
                            height - (insets.top + insets.bottom));
        return r;
    }

}
