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

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
*   An Image panel that provides a component showing an image on its background. It allows
*   an image to be placed on the IconPanel
*
*/
public class ImagePanel extends IconPanel
{

    URL imageURL;

    public ImagePanel()
    {
        super();
        Color background=UIManager.getColor("ImagePanel.background");
        if (background==null)
        {
            background=Color.white;
        }
        setBackground(background);
    }


    /**
     * Construct a panel with the specified image
     * @param imageURL the URL of the image to be used in the background
     */
    public ImagePanel(URL imageURL)
    {
        this();
        setImageURL(imageURL);
    }

    public URL getImageURL()
    {
        return imageURL;
    }

    public void setImageURL(URL imageURL)
    {
        this.imageURL = imageURL;
        try
        {
            setIcon(new ImageIcon(imageURL));
        }
        catch(Throwable t)
        {
        }
    }
}

