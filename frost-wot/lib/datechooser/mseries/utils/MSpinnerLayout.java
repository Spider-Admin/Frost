/*
*   Copyright (c) 2001 Martin Newstead (mseries@brundell.fsnet.co.uk).  All Rights Reserved.
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
package mseries.utils;
import java.awt.*;
/**
*   Layout manager for a spinner type display.
*   Divides the space into three, add the textfield first, then the top right component
*   then the bottom right. The image shows buttons on the right hand side which is the
*   most likely usage but <i>any</i> component could be added.
*<PRE>
*         <img src="MSpinnerLayout.jpg"/>
*</PRE>
*   The right hand components get resized to be twice as wide as the are high, and they will always
*   occupy the full height of the component as assigned by the parent layout manager
*
*/
public class MSpinnerLayout implements LayoutManager
{
    private Component display, up, down;
    int width, height;
    Insets insets;

    public void addLayoutComponent(String name, Component comp) {}

    public void removeLayoutComponent(Component comp) {}

    public Dimension preferredLayoutSize(Container parent)
    {
        synchronized (parent.getTreeLock())
        {
            display = parent.getComponent(0);
            Dimension d = new Dimension(display.getPreferredSize().width+20,
                                    display.getPreferredSize().height);
            return d;
        }
    }

    public Dimension minimumLayoutSize(Container parent)
    {

        synchronized (parent.getTreeLock())
        {
            display = parent.getComponent(0);

/*
            Dimension d = new Dimension(display.getMinimumSize().width+50,
                                        display.getMinimumSize().height);
*/
            Dimension d = new Dimension(50, 22);
            return d;
        }
    }

    public void layoutContainer(Container parent)
    {
        synchronized (parent.getTreeLock())
        {
            insets = parent.getInsets();
            width = parent.getWidth();
            height = parent.getHeight();

            Rectangle cvb;

            int ncomponents = parent.getComponentCount();

            display = parent.getComponent(0);
            if (ncomponents > 1)
                up = parent.getComponent(1);
            if (ncomponents > 2)
                down = parent.getComponent(2);

            int buttonWidth = height - (insets.right + insets.left);
            int buttonHeight = (height-(insets.top + insets.bottom))/2;

            if (up != null)
            {

                up.setBounds(width - (insets.right + buttonWidth),
                                        insets.top,
                                    buttonWidth, buttonHeight);
            }
            if (down != null)
            {
                down.setBounds(width - (insets.right + buttonWidth),
                                    buttonHeight+insets.top,
                                    buttonWidth, buttonHeight);
            }
            if (display != null)
            {
                cvb = rectangleForCurrentValue();
                display.setBounds(cvb);
            }
        }
    }

    /**
    * Returns the area that is reserved for drawing the currently selected item.
    */
    protected Rectangle rectangleForCurrentValue()
    {
        int buttonSize=0;
        if ( up != null )
        {
            buttonSize = up.getWidth();
        }
        return new Rectangle(insets.left, insets.top,
                             width - (insets.left + insets.right + buttonSize),
                             height - (insets.top + insets.bottom));
    }


/*
    public static void main(String[] argv)
    {
        javax.swing.JPanel p=new javax.swing.JPanel();
        javax.swing.JFrame f = new javax.swing.JFrame();

        f.getContentPane().setLayout(new MSpinnerLayout());

        f.getContentPane().add(new javax.swing.JButton("Java"));
        f.getContentPane().add(new javax.swing.JTextField("Hello"));
        f.getContentPane().add(new javax.swing.JButton("World"));


        f.pack();
        f.show();
    }
*/
}
