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
import java.io.*;
import java.net.*;

/**
*   A simple subclass of JPanel that provides a background image
*/
public class MImagePanel extends javax.swing.JPanel
{
    URL imageURL;

    String file="";
    File f;
    Image img;

    public MImagePanel(LayoutManager layout)
    {
        super(layout);
    }

    protected void paintComponent(Graphics g)
    {
        if (g==null)
            return;

        if (!file.equals(""))
        {
            img = loadImage(this);
            g.drawImage(img, 0, 0, this);
            return;
        }
        super.paintComponent(g);
        return;
    }

    protected Image loadImage(Component comp)
    {
        Image i=null;

        i = comp.getToolkit().getImage(imageURL);
        MediaTracker m = new MediaTracker(comp);
        m.addImage(i, 0);
        try
        {
            m.waitForAll();
        }
        catch(InterruptedException ee)
        {
        }
        return i;
    }

    public void setImageURL(URL imageURL)
    {
        this.imageURL=imageURL;
    }

    public void setImageFile(String file)
    {
        f = new File(file);
        if (f.exists())
        {
            this.file=file;
            try
            {
                imageURL=new URL("file:///"+file);
            }
            catch(MalformedURLException e)
            {
                e.printStackTrace();
            }
        }
    }

    public String getImageFile()
    {
        return file;
    }

    public boolean hasImage()
    {
        return (!file.equals(""));
    }

/* Uncomment for unit testing

    public static void main(String[] argv)
    {
        MImagePanel panel = new MImagePanel(new FlowLayout());
        panel.setImageFile(argv[0]);
        panel.add(new javax.swing.JButton("OK"));
        panel.add(new javax.swing.JButton("OK"));
        panel.add(new javax.swing.JButton("OK"));
        panel.add(new javax.swing.JButton("OK"));

        javax.swing.JFrame f = new javax.swing.JFrame("Panel Test");

        f.getContentPane().setLayout(new FlowLayout());

        f.getContentPane().add(panel);

        f.pack();
        f.show();
    }
*/


}
