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


/**
 *   An arrow button with no border, until the mouse rolls over when the L&F border is drawn
 */
public class RollOverButton extends ArrowButton
{
    int direction;

    public RollOverButton(int dir)
    {
        super(dir);
        setRolloverEnabled(true);
    }

    protected void paintBorder(Graphics g)
    {
        setBorderPainted(getModel().isRollover());
        super.paintBorder(g);
    }

}

// $Log$
// Revision 1.1  2006-03-14 14:09:44  bback
// new date chooser component
//
// Revision 1.5  2003/03/26 23:29:50  martin
// Changed email address
//
// Revision 1.4  2002/12/21 22:53:16  martin
// *** empty log message ***
//
// Revision 1.3  2002/07/21 16:25:20  martin
// no message
//
// Revision 1.2  2002/06/18 21:32:55  martin
// no message
//
// Revision 1.1  2002/06/16 21:46:43  martin
// new file
//
