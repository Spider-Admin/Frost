/*
  AboutBox.java / About Box
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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
package frost.gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import frost.util.gui.JDialogWithDetails;

/**
 * @author $Author$
 * @version $Revision$
 */
public class AboutBox extends JDialogWithDetails {

    private final static String product = "Frost";

    // because a growing amount of users use CVS version:
    private String version = null;

    private final static String copyright = "Copyright (c) 2007 Frost Project";
    private final static String comments2 = "http://jtcfrost.sourceforge.net/";

    private JPanel imagePanel = new JPanel();
    private JPanel messagesPanel = new JPanel();

    private JLabel imageLabel = new JLabel();
    private JLabel productLabel = new JLabel();
    private JLabel versionLabel = new JLabel();
    private JLabel copyrightLabel = new JLabel();
    private JLabel licenseLabel = new JLabel();
    private JLabel websiteLabel = new JLabel();

    private static final ImageIcon frostImage = new ImageIcon(AboutBox.class.getResource("/data/jtc.jpg"));

    /**
     * @param parent
     */
    public AboutBox(Frame parent) {
        super(parent);
        initialize();
    }

    /**
     * Component initialization
     */
    private void initialize() {
        imageLabel.setIcon(frostImage);
        setTitle(language.getString("AboutBox.title"));
        setResizable(false);

        // Image panel
        imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        imagePanel.add(imageLabel);

        // Messages panel
        GridLayout gridLayout = new GridLayout(5, 1);
        messagesPanel.setLayout(gridLayout);
        messagesPanel.setBorder(new EmptyBorder(10, 50, 10, 10));
        productLabel.setText(product);
        versionLabel.setText(getVersion());
        copyrightLabel.setText(copyright);
        licenseLabel.setText(language.getString("AboutBox.label.openSourceProject"));
        websiteLabel.setText(comments2);
        messagesPanel.add(productLabel);
        messagesPanel.add(versionLabel);
        messagesPanel.add(copyrightLabel);
        messagesPanel.add(licenseLabel);
        messagesPanel.add(websiteLabel);

        // Putting everything together
        getUserPanel().setLayout(new BorderLayout());
        getUserPanel().add(imagePanel, BorderLayout.WEST);
        getUserPanel().add(messagesPanel, BorderLayout.CENTER);

        fillDetailsArea();
    }

    private void fillDetailsArea() {
        StringBuilder details = new StringBuilder();
        details.append(language.getString("AboutBox.text.development") + "\n");
        details.append("   Karsten Graul\n");
        details.append("   S. Amoako\n");
        details.append("   Roman Glebov (inactive)\n");
        details.append("   Jan-Thomas Czornack (left)\n");
        details.append("   Thomas Mueller (left)\n");
        details.append("   Jim Hunziker (left)\n");
        details.append("   Stefan Majewski (left)\n");
        details.append("   José Manuel Arnesto (left)\n\n");
        details.append(language.getString("AboutBox.text.windowsInstaller") + "\n");
        details.append("   Benoit Laniel\n\n");
        details.append(language.getString("AboutBox.text.systemTrayExecutables") + "\n");
        details.append("   Ingo Franzki\n\n");
        details.append(language.getString("AboutBox.text.translationSupport") + "\n");
        details.append("   Rudolf Krist\n");
        details.append("   RapHHfr\n\n");
        details.append(language.getString("AboutBox.text.splashScreenLogo") + "\n");
        details.append("   Frédéric Scheer\n\n");
        details.append(language.getString("AboutBox.text.miscCodeContributions") + "\n");
        details.append("   SuperSlut Yoda");
        setDetailsText(details.toString());
    }

    /**
     * @return
     */
    private String getVersion() {
        if (version == null) {
            version =
                language.getString("AboutBox.label.version")
                    + ": "
                    + getClass().getPackage().getSpecificationVersion();
        }
        return version;
    }
}
