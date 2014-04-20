/*
  MouseHyperlinkEvent.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>
  Some changes by Stefan Majewski <e9926279@stud3.tuwien.ac.at>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.util.gui.textpane;

import java.awt.event.*;
import java.net.*;

import javax.swing.event.*;
import javax.swing.text.*;

/**
 * Own hyperlink event containing the mouse position.
 * Needed to show a popup menu on the clicked point.
 */
@SuppressWarnings("serial")
public class MouseHyperlinkEvent extends HyperlinkEvent {
    
    MouseEvent mouseEvent;

    public MouseHyperlinkEvent(Object arg0, EventType arg1, URL arg2, String arg3, Element arg4, MouseEvent me) {
        super(arg0, arg1, arg2, arg3, arg4);
        mouseEvent = me;
    }
    
    public MouseEvent getMouseEvent() {
        return mouseEvent;
    }
}
