/*
  TristateCheckBoxTest.java / Frost
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
package frost.util.gui.tristatecheckbox;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Derived from  The Java Specialists' Newsletter Issue 145 (2007-05-25)
 * by Dr. Heinz M. Kabutz
 */
public class TristateCheckBoxTest {

	private static final Logger logger =  LoggerFactory.getLogger(TristateCheckBoxTest.class);

    public static void main(final String args[]) throws Exception {
      final JFrame frame = new JFrame("TristateCheckBoxTest");
      frame.setLayout(new GridLayout(0, 1, 15, 15));
      final UIManager.LookAndFeelInfo[] lfs =
          UIManager.getInstalledLookAndFeels();
      for (final UIManager.LookAndFeelInfo lf : lfs) {
        logger.info("Look&Feel {}", lf.getName());
        UIManager.setLookAndFeel(lf.getClassName());
        frame.add(makePanel(lf));
      }
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);
    }

    private static JPanel makePanel(final UIManager.LookAndFeelInfo lf) {
      final TristateCheckBox tristateBox = new TristateCheckBox(
          "Tristate checkbox");
      tristateBox.addItemListener(new ItemListener() {
        public void itemStateChanged(final ItemEvent e) {
          switch(tristateBox.getState()) {
            case SELECTED:
                logger.info("Selected");
                break;
            case DESELECTED:
                logger.info("Not Selected");
                break;
            case INDETERMINATE:
                logger.info("Tristate Selected");
                break;
          }
        }
      });
      tristateBox.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
            logger.info("{}", e);
        }
      });
      final JCheckBox normalBox = new JCheckBox("Normal checkbox");
      normalBox.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
            logger.info("{}", e);
        }
      });

      final JCheckBox enabledBox = new JCheckBox("Enable", true);
      enabledBox.addItemListener(new ItemListener() {
        public void itemStateChanged(final ItemEvent e) {
          tristateBox.setEnabled(enabledBox.isSelected());
          normalBox.setEnabled(enabledBox.isSelected());
        }
      });

      final JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
      panel.add(new JLabel(UIManager.getLookAndFeel().getName()));
      panel.add(tristateBox);
      panel.add(normalBox);
      panel.add(enabledBox);
      return panel;
    }
}