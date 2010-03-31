package mseries.ui;

import java.awt.*;

import javax.swing.*;

public class ShadowImage implements Icon
{
    protected BumpBuffer    buffer;
    int width, height, orientation;

    public ShadowImage(int orientation)
    {
        this.orientation=orientation;

        if (buffer == null)
        {
            createBuffer();
        }
    }

    protected void createBuffer()
    {
        buffer = new BumpBuffer();
    }

    public void setArea(int w, int h)
    {
        this.width=width;
        this.height=height;
    }


    /*
    *   Paints the texture on the texture area, repeating (tiling) as often as it needs
    *   to fill the area.
    */
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        int bufferWidth = buffer.getImageSize().width;
        int bufferHeight = buffer.getImageSize().height;
        int w = getIconWidth();
        int h = getIconHeight();


        if (orientation==SwingUtilities.VERTICAL)
        {
            for (int i=height; i>bufferWidth; i-=bufferWidth)
            {
                g.drawImage(buffer.getImage(), x, i,  w,  h,  null);
            }
        }
        else
        {
            for (int i=width; i>bufferWidth; i-=bufferWidth)
            {
                g.drawImage(buffer.getImage(), i, y, i + w, y + h, null);
            }
        }

    }

    public int getIconWidth()
    {
        return width;
    }

    public int getIconHeight()
    {
        return height;
    }

}

/**
 * One instance of the texture image. Each image is 64x64. Different colours and different
 * orientation make a different image. This class is capable of drawing the ArmatureImage
 * in any colours and etiher horizontal and vertical.
 */
class BumpBuffer
{
    static Frame        frame;
    static Component    component;

    static final int    IMAGE_SIZE = 6;
    static Dimension    imageSize = new Dimension(IMAGE_SIZE, IMAGE_SIZE);

    transient Image     image;
    Color               topColor;
    Color               backColor;
    boolean             horizontal = true;

    public BumpBuffer()
    {

        createComponent();

        image = getComponent().createImage(IMAGE_SIZE, IMAGE_SIZE);

        fillBumpBuffer();
    }


    public Image getImage()
    {
        if (image == null)
        {
            image = getComponent().createImage(IMAGE_SIZE, IMAGE_SIZE);

            fillBumpBuffer();
        }

        return image;
    }

    public Dimension getImageSize()
    {
        return imageSize;
    }

    /**
     *  The texture is drawn in this method, a graphic could be retrieved and drawn or simply
     *  a texture using lines
     */
    protected void fillBumpBuffer()
    {
        Graphics    g = image.getGraphics();


        g.setColor(Color.black);

                g.drawLine(0,0,0,0);
                g.drawLine(0,2,0,2);
                g.drawLine(0,4,0,4);

                g.drawLine(1,1,1,1);
                g.drawLine(1,3,1,3);
                g.drawLine(1,5,1,5);

                g.drawLine(2,0,2,0);
                g.drawLine(2,2,2,2);
                g.drawLine(2,4,2,4);

                g.drawLine(3,0,3,0);
                g.drawLine(3,2,3,2);
                g.drawLine(3,4,3,4);

                g.drawLine(4,1,4,1);
                g.drawLine(4,3,4,3);
                g.drawLine(4,5,4,5);

                g.drawLine(5,0,5,0);
                g.drawLine(5,2,5,2);
                g.drawLine(5,4,5,4);


        g.dispose();
    }

    protected Component getComponent()
    {
        return component;
    }

    protected void createComponent()
    {
        if (frame == null)
        {
            frame = new Frame("bufferCreator");
        }

        if (component == null)
        {
            component = new Canvas();

            frame.add(component, BorderLayout.CENTER);
        }

        frame.addNotify();
    }
}
