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
package mseries.plaf.Metal;

import javax.swing.*;
import javax.swing.plaf.*;

/**
*    The Metal Look and Feel UIDelagate for Header
*
*/
public class MetalHeaderUI extends mseries.plaf.basic.BasicHeaderUI
{
    /**
    *    This method is called by the UIManager to get an instance of
    *    this class and must be overridden in subclasses.
    */
    public static ComponentUI createUI(JComponent x)
    {
        return new MetalHeaderUI();
    }
}
