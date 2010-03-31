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
package mseries.plaf.basic;

import mseries.Calendar.CalendarPanel;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.*;

/**
 *   The BASIC look and feel UIDelegate for the CalendarPanel. It is envisaged
 *   that this is subclasses to provide something more acceptable. A Windows
 *   and Metal one are provided.

 *    A Look and Feel delegate for the CalenderPanel. This version renders the
 *    selected date with a raised border and dashed focus indicator. TAB moves
 *    around the calendar, Shift-Tab jumps out to the next component. The arrow
 *    keys move around the panel stopping (i.e. not rolling over) at the ends.
 */
public class BasicCalendarPanelUI extends ComponentUI
{
    protected Color background = UIManager.getColor("control");
    protected Color foreground = UIManager.getColor("Button.foreground");
    protected Color selectedBackground = UIManager.getColor("controlShadow");
    protected Color selectedForeground = UIManager.getColor("Button.foreground");

    protected CalendarPanel calendarPanel;

    protected KeyListener keyHandler;
    protected MouseListener mouseHandler;
    protected FocusListener focusHandler;

    protected int cellWidth, cellHeight;
    protected int DAYS, WEEKS;
    protected Font font;
    private Toolkit tk = Toolkit.getDefaultToolkit();

    /**
     *    Inner class to handle presses of the arrow keys
     */
    protected class ArrowKeyAction implements ActionListener
    {
        private boolean horizontal;
        private int inc;
        private Point selected;
        private Point first;
        private Point last;

        public ArrowKeyAction(int inc, boolean horizontal)
        {
            this.inc = inc;
            this.horizontal = horizontal;
        }

        public void actionPerformed(ActionEvent e)
        {
            doAction(e);
        }

        private void doAction(ActionEvent e)
        {
            selected = calendarPanel.getSelectedCell();
            int x = selected.x;
            int y = selected.y;
            first = calendarPanel.firstCell;
            last = calendarPanel.lastCell;

            if (horizontal)
            {
                int left = (selected.y == first.y) ? first.x : 0;
                int right = (selected.y == last.y) ? last.x : DAYS - 1;
                selected.x += inc;
                selected.x = Math.min(Math.max(selected.x, left), right);
            }
            else
            {
                int top = (selected.x < first.x) ? first.y + 1 : 0;
                int bottom = (selected.x > last.x) ? last.y - 1 : last.y;
                selected.y += inc;
                selected.y = Math.min(Math.max(selected.y, top), bottom);
            }
            if (calendarPanel.isInRange(selected.y, selected.x))
            {
                calendarPanel.setSelectedCell(selected);
                calendarPanel.notifyListeners();
            }
            else
            {
                selected.x = x;
                selected.y = y;
                tk.beep();
            }
        }
    }

    /**
     *    This method is called by the UIManager to get an instance of
     *    this class and must be overridden in subclasses.
     */
    public static ComponentUI createUI(JComponent x)
    {
        return new BasicCalendarPanelUI();
    }

    /*
    *    Called by the UIManager to install the UI of the component
    */
    public void installUI(JComponent c)
    {
        calendarPanel = (CalendarPanel)c;
        installDefaults();
        installListeners();
    }

    public void uninstallUI(JComponent c)
    {
        uninstallDefaults();
        uninstallListeners();
    }

    protected void installDefaults()
    {
        DAYS = calendarPanel.DAYS;
        WEEKS = calendarPanel.WEEKS;
    }

    protected void uninstallDefaults()
    {
    };



    protected void installListeners()
    {
        if ((keyHandler = createKeyHandler()) != null)
        {
            calendarPanel.addKeyListener(keyHandler);
        }

        if ((mouseHandler = createMouseHandler()) != null)
        {
            calendarPanel.addMouseListener(mouseHandler);
        }

        if ((focusHandler = createFocusHandler()) != null)
        {
            calendarPanel.addFocusListener(focusHandler);
        }

        installKeyboardActions();
    }

    private void installKeyboardActions()
    {
        calendarPanel.registerKeyboardAction(new ArrowKeyAction(-1, true), KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), JComponent.WHEN_FOCUSED);
        calendarPanel.registerKeyboardAction(new ArrowKeyAction(1, true), KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), JComponent.WHEN_FOCUSED);
        calendarPanel.registerKeyboardAction(new ArrowKeyAction(-1, false), KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), JComponent.WHEN_FOCUSED);
        calendarPanel.registerKeyboardAction(new ArrowKeyAction(1, false), KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), JComponent.WHEN_FOCUSED);
    }

    protected void uninstallListeners()
    {
        if (keyHandler != null)
        {
            calendarPanel.removeKeyListener(keyHandler);
        }

        if (mouseHandler != null)
        {
            calendarPanel.removeMouseListener(mouseHandler);
        }

        if (focusHandler != null)
        {
            calendarPanel.removeFocusListener(focusHandler);
        }
    }

    public void update(Graphics g, JComponent c)
    {
        paint(g, c);
    }

    public void paint(Graphics g, JComponent c)
    {
        cellWidth = getCellSize().width;
        cellHeight = getCellSize().height;

        doPaint(g, c);
    }

    private void doPaint(Graphics g, JComponent c)
    {
        String legend;
        boolean selectCell;
        int day;
        Point thisCell = calendarPanel.getSelectedCell();

        background = c.getBackground();
        foreground = c.getForeground();

        font = c.getFont();

/*
        if(isOpaque())
        {
            g.setColor(background);
            g.fillRect(0, 0, cellWidth, cellHeight);
        }
*/

        for (int d = 0; d < WEEKS; d++)
        {
            for (int w = 0; w < DAYS; w++)
            {
                selectCell = false;
                if (thisCell.x == w && thisCell.y == d)
                {
                    selectCell = true;
                }

                if (!calendarPanel.isInMonth(d, w))
                {
                    legend = null;
                    background = calendarPanel.getBackground();
                    foreground = calendarPanel.getForeground();
                }
                else
                {
                    day = calendarPanel.getLegendFor(d, w);
                    if (day == 1)
                        calendarPanel.firstCell.x = w;
                    legend = Integer.toString(day);
                    calendarPanel.lastCell.x = w;
                    calendarPanel.lastCell.y = d;

                    background = calendarPanel.getBackground(d, w);
                    foreground = calendarPanel.getForeground(d, w);
                }

                g.translate(cellWidth * w, cellHeight * d);


                drawSelectedEffect(g, selectCell);
                if (legend != null)
                    drawLegend(g, legend);

                drawFocusedEffect(g, calendarPanel.hasFocus() && selectCell, selectCell);

                g.translate(-(cellWidth * w), -(cellHeight * d));
            }
        }
    }

    /*
    *    This is where we would draw/remove borders, focus highlights,
    *    colours etc. Override this method in a subclass to render the
    *    selected date.
    */
    protected void drawSelectedEffect(Graphics g, boolean selected)
    {
        int width, height;

        width = getCellSize().width;
        height = getCellSize().height;

        if (isOpaque())
        {
            g.setColor(background);
            g.fillRect(0, 0, width, height);
        }

        if (selected)
        {
            g.setColor(SystemColor.controlDkShadow);
            // Draw Raised Border TOP-LEFT
            g.drawRoundRect(0, 0, width-1, height-1, 2, 2);
            /*
            g.drawLine(0, 0, width, 0);
            g.drawLine(0, 0, 0, height);

            // BOTTOM-RIGHT
            g.drawLine(0, height - 1, width - 1, height - 1);
            g.drawLine(width - 1, height - 1, width - 1, 0);
            */
        }
    }

    /**
     *   This is where the legend, the day is drawn
     */
    public void drawLegend(Graphics g, String legend)
    {
        int x,y;
        FontMetrics fm;
        int strWidth = 0;
        int strHeight = 0;
        int width, height;

        width = getCellSize().width;
        height = getCellSize().height;

        g.setFont(calendarPanel.getFont());
        fm = g.getFontMetrics();
        strWidth = fm.stringWidth(legend);
        strHeight = fm.getHeight();
        x = width - strWidth - 4;
        y = ((height - strHeight) / 2) + strHeight - 4;

        g.setColor(foreground);
        g.drawString(legend, x, y);
    }

    /**
     *    Draws the dashed rectangle around the number in the cell which is selected
     *    when the calendar panel has focus. Over ride this method in a subclass
     *    to change the appearance.
     */
    protected void drawFocusedEffect(Graphics g, boolean focused, boolean selected)
    {
/*
        int width, height;

        width = getCellSize().width;
        height = getCellSize().height;
        if (focused)
        {
            g.setColor(foreground);
            g.drawLine(3, height-5, width-3, height-5);
            //drawDashedRect(g, 3, , width-5, height-5);
        }
*/
    }

    /**
     *    Creates the mouse listener for the panel.
     */
    protected MouseListener createMouseHandler()
    {

        return new MouseAdapter()
        {
            int x, y;

            public void mousePressed(MouseEvent e)
            {
                x = e.getX() / cellWidth;
                y = e.getY() / cellHeight;
                if (calendarPanel.isInMonth(y, x) && calendarPanel.isInRange(y, x))
                {
                    calendarPanel.setSelectedCell(x, y);
                    /*
                    *    Only repaint if the click is in a legitimate cell
                    */
                    if (calendarPanel.hasFocus())
                    {
                        // Already go the focus so a simple repaint will do
                        calendarPanel.repaint();
                    }
                    else
                    {
                        /*
                        *    First click to get the focus, repaint is done by the
                        *    processFocusEvent method
                        */
                        calendarPanel.requestFocus();
                    }
                    calendarPanel.notifyListeners();
                }
            }
        };
    }


    /**
     *    KeyHandler factory method, this implementation handles the push
     *    of the TAB and SHIFT-TAB key
     *    @return a KeyListener Object
     */
    protected KeyListener createKeyHandler()
    {
        return new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                int key = e.getKeyCode();
                int modifiers = e.getModifiers();
                if (key == KeyEvent.VK_TAB)
                {
                    // Manage tabbing around the Button Panel
                    int inc = (modifiers == InputEvent.SHIFT_MASK) ? -1 : 1;
                    moveToNextCell(inc, true);
                    calendarPanel.notifyListeners();
                    e.consume();
                }
                calendarPanel.repaint();
            }
        };
    }

    protected FocusListener createFocusHandler()
    {
        return new FocusAdapter()
        {
            public void focusGained(FocusEvent e)
            {
                doFocus();
            }

            public void focusLost(FocusEvent e)
            {
                doFocus();
            }

            private void doFocus()
            {
                calendarPanel.repaint();
            }
        };
    }

    protected Dimension getCellSize()
    {
        return calendarPanel.getCellSize();
    }

    /**
     *    Calculates the next cell that contains a valid date and therefore
     *    can be selected
     *    @param inc minus -1 moves back one day, +1 forward wrapping around at
     *    both the start and end of the month
     *    @param roll true if the selection rols to the next row at the end
     */
    protected void moveToNextCell(int inc, boolean roll)
    {
        move(inc, roll);
    }

    private void move(int inc, boolean roll)
    {
        Point firstCell = calendarPanel.firstCell;
        Point lastCell = calendarPanel.lastCell;
        Point selectedCell = (Point)(calendarPanel.getSelectedCell()).clone();
        /*
        *    First check the boundaries
        */
        if (inc > 0 && selectedCell.equals(lastCell))
        {
            calendarPanel.setSelectedCell(firstCell);
            return;
        }
        if (inc < 0 && selectedCell.equals(firstCell))
        {
            calendarPanel.setSelectedCell(lastCell);
            return;
        }
        /*
        *    Now move within the calendar, rolling around at the end/start
        *    of each row
        */
        selectedCell.x += inc;

        if (selectedCell.x == DAYS)
        {
            selectedCell.x = 0;
            selectedCell.y++;

        }
        else if (selectedCell.x < 0)
        {
            selectedCell.x = DAYS - 1;
            selectedCell.y--;
        }

        if (selectedCell.y == WEEKS)
        {
            selectedCell.y = 0;
        }
        else if (selectedCell.y < 0)
        {
            selectedCell.y = WEEKS - 1;
        }
        if (calendarPanel.isInRange(selectedCell.y, selectedCell.x))
        {
            calendarPanel.setSelectedCell(selectedCell);
        }
        else
        {
            tk.beep();
        }
    }

    public boolean isOpaque()
    {
        return !calendarPanel.hasImage();
    }
}


