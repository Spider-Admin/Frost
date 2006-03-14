/*
*   Copyright (c) 2002 Martin Newstead (mseries@brundell.fsnet.co.uk).  All Rights Reserved.
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

import javax.swing.*;

public class IconPanel extends JPanel
{

    // Placement constants

    /**
     * Place the image in the top left position
     */
    public static final int NORTH_WEST    = 1;
    /**
     * Place the image in the top centre position
     */
    public static final int NORTH         = 2;
    /**
     * Place the image in the top right position
     */
    public static final int NORTH_EAST    = 3;
    /**
     * Place the image in the centre right position
     */
    public static final int EAST          = 4;
    /**
     * Place the image in the bottom right position
     */
    public static final int SOUTH_EAST    = 5;
    /**
     * Place the image in the bottom centre position
     */
    public static final int SOUTH         = 6;
    /**
     * Place the image in the bottom left position
     */
    public static final int SOUTH_WEST    = 7;
    /**
     * Place the image in the centre left position
     */
    public static final int WEST          = 8;
    /**
     * Place the image in the centre position
     */
    public static final int CENTER        = 9;
    /**
     * Fill the panel with multiple copies of the image
     */
    public static final int TILED         = 10;
    /**
     * Fill the panel with a single scaled copy of the image (only
     * avaliable if the icon is an ImageIcon)
     */
    public static final int STRETCHED     = 11;

    protected int placement               = 1;
    protected Color background;
    protected Icon icon                   = null;

    /**
     * Construct a panel with no icon set
     */
    public IconPanel()
    {
        super();
        background = super.getBackground();
    }

    /**
     * Construct a panel with the specified icon in the top left position
     * @param icon the icon for the panel background
     */
    public IconPanel(Icon icon)
    {
        this.icon = icon;
        background = super.getBackground();
    }


    /**
     * Construct a panel with the specified icon in specified position
     * @param icon the icon for the panel background
     * @param placement the position of the image in the panel
     */
    public IconPanel(Icon icon, int placement)
    {
        this.icon = icon;
        setPlacement(placement);
        background = super.getBackground();
    }

    /**
     * Set the image placement attribute
     * @param placement the position of the image in the panel
     */
    public void setPlacement(int placement)
    {
        if (placement < 1 || placement > 11)
        {
            throw new IllegalArgumentException("Placement value is not valid");
        }

        this.placement = placement;
    }

    /**
     * Set the icon attribute
     * @param icon the icon for the panel background
     */
    public void setIcon(Icon icon)
    {
        this.icon = icon;
    }

    /**
     * Set the background colour for the panel
     * @param background the background colour
     */
    public void setBackground(Color background)
    {
        super.setBackground(background);
        this.background = background;
    }

    /**
     * Get the current placement value
     * @return the placement value
     */
    public int getPlacement()
    {
        return placement;
    }

    /**
     * Get the current icon
     * @return the current icon
     */
    public Icon getIcon()
    {
        return icon;
    }

    /**
     * Paint the component
     * @param g the graphics instance
     */
    protected void paintComponent(Graphics g)
    {
        Dimension size=getSize();
        int w=getSize().width;
        int h=getSize().height;
        int x=0;
        int y=0;

        if (g==null)
        {
            return;
        }

        g.setColor(background);
        g.fillRect(x,y,w,h);

        if (icon==null)
        {
            return;
        }

        if (placement == NORTH_WEST)
        {
            x = 0;
            y = 0;
        } else if (placement == NORTH)
        {
            x = (w - icon.getIconWidth()) / 2;
            y = 0;
        } else if (placement == NORTH_EAST)
        {
            x = w - icon.getIconWidth();
            y = 0;
        } else if (placement == EAST)
        {
            x = w - icon.getIconWidth();
            y = (h - icon.getIconHeight()) / 2;
        } else if (placement == SOUTH_EAST)
        {
            x = w - icon.getIconWidth();
            y = h - icon.getIconHeight();
        } else if (placement == SOUTH)
        {
            x = (w - icon.getIconWidth()) / 2;
            y = h - icon.getIconHeight();
        } else if (placement == SOUTH_WEST)
        {
            x = 0;
            y = h - icon.getIconHeight();
        } else if (placement == WEST)
        {
            x = 0;
            y = (h - icon.getIconHeight()) / 2;
        } else if (placement == CENTER)
        {
            x = (w - icon.getIconWidth()) / 2;
            y = (h - icon.getIconHeight()) / 2;
        } else if (placement == TILED)
        {
            x = 0;
            while (x < w)
            {
                y = 0;
                while (y < h)
                {
                    if (icon != null)
                    {
                        icon.paintIcon(this, g, x, y);
                    }
                    y = y + icon.getIconHeight();
                }
                x = x + icon.getIconWidth();
            }
        } else if (placement == STRETCHED)
        {
            if (icon instanceof ImageIcon)
            {
                g.drawImage(((ImageIcon)icon).getImage(),0,0,w,h,this);
            }
            else
            {
                System.out.println("Can only stretch ImageIcons");
                icon.paintIcon(this, g, 0, 0);
            }
        }

        if (placement != TILED && placement != STRETCHED)
        {
            icon.paintIcon(this, g, x, y);
        }

        return;
    }
}

