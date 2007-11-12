/*
  TriStateCheckBox2.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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
package frost.util.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

public class TriStateCheckBox2 extends JCheckBox {

    public static class State {
        private String desc = "";

        private State() {
        }

        private State(final String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return desc;
        }
    }

    public static final State NOT_SELECTED = new State("NOT_SELECTED");
    public static final State SELECTED = new State("SELECTED");
    public static final State DONT_CARE = new State("DONT_CARE");

    private final TristateModel model;

    public TriStateCheckBox2(final String text, final State initial) {
        super(text);

        final Icon icon = new TristateCheckBoxIcon();
        super.setIcon(icon);

        // Add a listener for when the mouse is pressed and released
        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                TriStateCheckBox2.this.mousePressed();
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                TriStateCheckBox2.this.mouseReleased();
            }
        });
        // Reset the keyboard action map
        final ActionMap map = new ActionMapUIResource();
        map.put("pressed", new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                TriStateCheckBox2.this.mousePressed();
            }
        });
        map.put("released", new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                TriStateCheckBox2.this.mouseReleased();
            }
        });
        SwingUtilities.replaceUIActionMap(this, map);
        // set the model to the adapted model
        model = new TristateModel(getModel());
        setModel(model);
        setState(initial);
    }

    private void mousePressed() {
        grabFocus();
        model.setPressed(true);
        model.setArmed(true);
    }

    private void mouseReleased() {
        model.nextState();
        model.setArmed(false);
        model.setPressed(false);
    }

    @Override
    public void doClick() {
        mousePressed();
        mouseReleased();
    }

    public TriStateCheckBox2(final String text) {
        this(text, DONT_CARE);
    }

    public TriStateCheckBox2() {
        this(null);
    }

    /** No one may add mouse listeners, not even Swing! */
    @Override
    public void addMouseListener(final MouseListener l) {
    }

    /** No one may set a new icon */
    @Override
    public void setIcon(final Icon icon) {
    }

    /**
     * Set the new state to either CHECKED, CROSSED or NOT_SELECTED.
     */
    public void setState(final State state) {
        model.setState(state);
    }

    /**
     * Return the current state, which is determined by the selection status of
     * the model.
     */
    public State getState() {
        return model.getState();
    }

    public Boolean getBooleanState() {
        if( model.getState() == DONT_CARE ) {
            return null;
        } else if( model.getState() == SELECTED ) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public void setSelected(final boolean selected) {
        if( selected ) {
            setState(SELECTED);
        } else {
            setState(NOT_SELECTED);
        }
    }

    private class TristateModel implements ButtonModel {
        private final ButtonModel other;

        private State currentState = NOT_SELECTED;

        private TristateModel(final ButtonModel other) {
            this.other = other;
        }

        private State getState() {
            return currentState;
        }

        private void setState(final State state) {
            this.currentState = state;
        }

        public boolean isSelected() {
            return (currentState == SELECTED || currentState == DONT_CARE);
        }

        /** We rotate between NOT_SELECTED, CHECKED and CROSSED. */
        private void nextState() {
            final State current = getState();
            if( current == NOT_SELECTED ) {
                setState(SELECTED);
            } else if( current == SELECTED ) {
                setState(DONT_CARE);
            } else if( current == DONT_CARE ) {
                setState(NOT_SELECTED);
            }

            //This is to enforce a call to the fireStateChanged method
            other.setSelected(!other.isSelected());
        }

        public void setArmed(final boolean b) {
            other.setArmed(b);
        }

        /**
         * We disable focusing on the component when it is not enabled.
         */
        public void setEnabled(final boolean b) {
            try {
                setFocusable(b);
            } catch (final Exception ex) {
                ex.printStackTrace();
            }//catch

            other.setEnabled(b);
        }

        /**
         * All these methods simply delegate to the "other" model that is being
         * decorated.
         */
        public boolean isArmed() {
            return other.isArmed();
        }

        /* public boolean isSelected() { return other.isSelected(); } */
        public boolean isEnabled() {
            return other.isEnabled();
        }

        public boolean isPressed() {
            return other.isPressed();
        }

        public boolean isRollover() {
            return other.isRollover();
        }

        public void setSelected(final boolean b) {
            other.setSelected(b);
        }

        public void setPressed(final boolean b) {
            other.setPressed(b);
        }

        public void setRollover(final boolean b) {
            other.setRollover(b);
        }

        public void setMnemonic(final int key) {
            other.setMnemonic(key);
        }

        public int getMnemonic() {
            return other.getMnemonic();
        }

        public void setActionCommand(final String s) {
            other.setActionCommand(s);
        }

        public String getActionCommand() {
            return other.getActionCommand();
        }

        public void setGroup(final ButtonGroup group) {
            other.setGroup(group);
        }

        public void addActionListener(final ActionListener l) {
            other.addActionListener(l);
        }

        public void removeActionListener(final ActionListener l) {
            other.removeActionListener(l);
        }

        public void addItemListener(final ItemListener l) {
            other.addItemListener(l);
        }

        public void removeItemListener(final ItemListener l) {
            other.removeItemListener(l);
        }

        public void addChangeListener(final ChangeListener l) {
            other.addChangeListener(l);
        }

        public void removeChangeListener(final ChangeListener l) {
            other.removeChangeListener(l);
        }

        public Object[] getSelectedObjects() {
            return other.getSelectedObjects();
        }
    }

    private class TristateCheckBoxIcon implements Icon, UIResource, Serializable {

        protected int getControlSize() {
            return 13;
        }

        public void paintIcon(final Component c, final Graphics g, int x, final int y) {
            final JCheckBox cb = (JCheckBox) c;
            final TristateModel model = (TristateModel) cb.getModel();
            final int controlSize = getControlSize();

            final boolean drawCheck = model.getState() == SELECTED;
            final boolean drawCross = model.getState() == DONT_CARE;

            if( model.isEnabled() ) {
                if( model.isPressed() && model.isArmed() ) {
                    g.setColor(MetalLookAndFeel.getControlShadow());
                    g.fillRect(x, y, controlSize - 1, controlSize - 1);
                    drawPressed3DBorder(g, x, y, controlSize, controlSize);
                } else {
                    drawFlush3DBorder(g, x, y, controlSize, controlSize);
                }
                g.setColor(MetalLookAndFeel.getControlInfo());
            } else {
                g.setColor(MetalLookAndFeel.getControlShadow());
                g.drawRect(x, y, controlSize - 1, controlSize - 1);
            }

            if( drawCross ) {
                drawCross(c, g, x, y);
            }

            if( drawCheck ) {
                if( cb.isBorderPaintedFlat() ) {
                    x++;
                }
                drawCheck(c, g, x, y);
            }

        }// paintIcon

        protected void drawCross(final Component c, final Graphics g, final int x, final int y) {
            final int controlSize = getControlSize();
            g.fillRect(x + 3, y + 3, controlSize-4-3, controlSize-4-3);
//            g.drawLine(x + (controlSize - 4), y + 2, x + 3, y + (controlSize - 5));
//            g.drawLine(x + (controlSize - 4), y + 3, x + 3, y + (controlSize - 4));
//            g.drawLine(x + 3, y + 2, x + (controlSize - 4), y + (controlSize - 5));
//            g.drawLine(x + 3, y + 3, x + (controlSize - 4), y + (controlSize - 4));
        }

        protected void drawCheck(final Component c, final Graphics g, final int x, final int y) {
            final int controlSize = getControlSize();
            g.fillRect(x + 3, y + 5, 2, controlSize - 8);
            g.drawLine(x + (controlSize - 4), y + 3, x + 5, y + (controlSize - 6));
            g.drawLine(x + (controlSize - 4), y + 4, x + 5, y + (controlSize - 5));
        }

        private void drawFlush3DBorder(final Graphics g, int x, int y, final int w, final int h) {
            g.translate(x, y);
            g.setColor(MetalLookAndFeel.getControlDarkShadow());
            g.drawRect(0, 0, w - 2, h - 2);
            g.setColor(MetalLookAndFeel.getControlHighlight());
            g.drawRect(1, 1, w - 2, h - 2);
            g.setColor(MetalLookAndFeel.getControl());
            g.drawLine(0, h - 1, 1, h - 2);
            g.drawLine(w - 1, 0, w - 2, 1);
            g.translate(-x, -y);
        }

        private void drawPressed3DBorder(final Graphics g, int x, int y, final int w, final int h) {
            g.translate(x, y);
            drawFlush3DBorder(g, 0, 0, w, h);
            g.setColor(MetalLookAndFeel.getControlShadow());
            g.drawLine(1, 1, 1, h - 2);
            g.drawLine(1, 1, w - 2, 1);
            g.translate(-x, -y);
        }

        public int getIconWidth() {
            return getControlSize();
        }

        public int getIconHeight() {
            return getControlSize();
        }
    }
}
