/*
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
import java.io.*;
import javax.swing.*;

public class MPopupPanel extends JPanel implements MPopup, Serializable
{
    int desiredLocationX,desiredLocationY;
    boolean isVisible=false;
    boolean hasShadow=false;
    Component invoker;
    Component firstComp=null;

    public MPopupPanel() 
    {
        super();
        setLayout(new BorderLayout());
        setDoubleBuffered(true);
        this.setOpaque(true);
    }

    public void addComponent(Component aComponent,Object constraints) 
    {
        this.add(aComponent,constraints);
        if (firstComp==null) 
        {
            firstComp=aComponent;
        }
    }

    public void setShadow(boolean hasShadow)
    {
        if (hasShadow)
        {
            this.add(new Shadow(SwingUtilities.HORIZONTAL), BorderLayout.SOUTH);
            this.add(new Shadow(SwingUtilities.VERTICAL), BorderLayout.EAST);
        }
        this.hasShadow=hasShadow;
    }

    public boolean isShadow()
    {
        return hasShadow;
    }

    public void removeComponent(Component c) 
    {
        this.remove(c);
    }

    public void requestFocus()
    {
        firstComp.requestFocus();
    }

    public void update(Graphics g) 
    {
        paint(g);
    }
    
    public void pack() 
    {
        setSize(getPreferredSize());
    }

    public void setParent(Component invoker)
    {
        this.invoker=invoker;
    }

    private void showPanel(Component invoker) 
    {
        Container parent = ScreenUtilities.getParentWindow(invoker);

        Point p = ScreenUtilities.convertScreenLocationToParent(parent,desiredLocationX,desiredLocationY);

        this.setLocation(p.x,p.y);
        if(parent instanceof JLayeredPane) 
        {
            ((JLayeredPane)parent).add(this,JLayeredPane.POPUP_LAYER,0);
        } 
        else
        {
            parent.add(this);
        }

        this.isVisible=true;
    }

    public void setVisible(boolean show) 
    {
        if (show)
        {
            showPanel(invoker);
        }
        else
        {
            hidePanel();
        }
    }

    private void hidePanel()
    {
        Container parent = getParent();
        Rectangle r = this.getBounds();
        if(parent != null)
        {
            parent.remove(this);
        }
        parent.repaint(r.x,r.y,r.width,r.height);
        this.isVisible=false;
    }

    public void setLocationOnScreen(int x,int y) 
    {
        Container parent = getParent();
        if(parent != null) 
        {
            Point p = ScreenUtilities.convertScreenLocationToParent(parent,x,y);
            this.setLocation(p.x,p.y);
        } 
        else 
        {
            desiredLocationX = x;
            desiredLocationY = y;
        }
    }

    public boolean isVisible()
    {
        return this.isVisible;
    }

    public int getWeight()
    {
        return MPopup.LIGHT;
    }

    public boolean isLightweight()
    {
        return true;
    }

    public boolean isOpaque()
    {
        return false;
    }
}

