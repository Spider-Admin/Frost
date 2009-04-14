/*
  Browser.java
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

package frost.util.externalbrowser;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * Browser Component
 * @author Jantho
 */
public class Browser extends JPanel {

    private static final Logger logger = Logger.getLogger(Browser.class.getName());

    // Global Variables
    String[] imageExtensions = {".jpg", ".gif", ".jpeg", ".png", ".bmp"};
    JFrame parent;

    // GUI Objects
    JPanel contentPanel;
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    JButton backButton = new JButton("<");
    JButton forwardButton = new JButton(">");
    JButton homeButton = new JButton("~");
    JButton addPageButton = new JButton(": )");
    JButton removePageButton = new JButton(": (");

    JEditorPane editorPane = new JEditorPane();

    JComboBox urlComboBox = new JComboBox();
    JComboBox favComboBox = new JComboBox();

    JScrollPane scrollPane = new JScrollPane(editorPane);

    private void init() {
    urlComboBox.setEditable(true);

        // Browser Link Listener
    editorPane.addHyperlinkListener(new HyperlinkListener() {
        public void hyperlinkUpdate(final HyperlinkEvent e) {
            parent.setTitle(e.getURL().toString());
            hyperlink_actionPerformed(e);
        }
        });

    // backButton Action Listener
    backButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(final ActionEvent e) {
            int i = urlComboBox.getSelectedIndex();
            if (i > 0) {
            i--;
            urlComboBox.setSelectedIndex(i);
            }
        }
        });

    // forwardButton Action Listener
    forwardButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(final ActionEvent e) {
            int i = urlComboBox.getSelectedIndex();
            if (i < urlComboBox.getItemCount() - 1) {
            i++;
            urlComboBox.setSelectedIndex(i);
            }
        }
        });

    // homeButton Action Listener
    homeButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(final ActionEvent e) {
            editorPane.setText(makeStartPage());
        }
        });

    // addPageButton Action Listener
    addPageButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(final ActionEvent e) {
            // Add url to favComboBox
            final String url = editorPane.getPage().toString();
            boolean exists = false;
            for (int i = 0; i < favComboBox.getItemCount(); i++) {
            if (((String)favComboBox.getItemAt(i)).equals(url)) {
                exists = true;
                favComboBox.setSelectedItem(url);
            }
            }
            if (!exists) {
            favComboBox.addItem(url);
            favComboBox.setSelectedItem(url);
            writeSettings(new File("browser.ini"));
            }
        }
        });

    // removePageButton Action Listener
    removePageButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(final ActionEvent e) {
            favComboBox.removeItem(editorPane.getPage().toString());
            writeSettings(new File("browser.ini"));
        }
        });

    // urlComboBox Action Listener
    urlComboBox.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(final ActionEvent e) {
            setPage((String)urlComboBox.getSelectedItem());
        }
        });

    // favComboBox Action Listener
    favComboBox.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(final ActionEvent e) {
            setPage((String)favComboBox.getSelectedItem());
        }
        });

    contentPanel = this;
    contentPanel.setLayout(new BorderLayout());

    buttonPanel.add(backButton);
    buttonPanel.add(forwardButton);
    buttonPanel.add(homeButton);
    buttonPanel.add(addPageButton);
    buttonPanel.add(removePageButton);
    buttonPanel.add(favComboBox);

    editorPane.setEditable(false);
    contentPanel.add(scrollPane, BorderLayout.CENTER);
    contentPanel.add(buttonPanel, BorderLayout.NORTH);
    contentPanel.add(urlComboBox, BorderLayout.SOUTH);

    readSettings(new File("browser.ini"));
    setPage("http://localhost:8888");
    }

    String makeStartPage() {
    String html = new String();
    html = "<html><body>";
    for (int i = 0; i < favComboBox.getItemCount(); i++) {
        html = html + "<a href=\"" + (String)favComboBox.getItemAt(i) + "\">" +
        (String)favComboBox.getItemAt(i) + "</a><br>";
    }

    html = html + "</body></html>";
    return html;
    }

    void setPage(String url) {
    if (url != null) {
        // Check URL
        if (!url.startsWith("http://")) {
            url = "http://" + url;
        }

        // Add url to urlComboBox
        boolean exists = false;
        for (int i = 0; i < urlComboBox.getItemCount(); i++) {
        if (((String)urlComboBox.getItemAt(i)).equals(url)) {
            exists = true;
            urlComboBox.setSelectedItem(url);
        }
        }

        if (!exists) {
        int i = urlComboBox.getSelectedIndex();
        if (i == -1 || urlComboBox.getItemCount() == 0) {
            i = 0;
        } else {
            i++;
        }
        urlComboBox.insertItemAt(url, i);
        urlComboBox.setSelectedItem(url);
        }

        // Generate image wrapper
        boolean image = false;
        for( final String element : imageExtensions ) {
            if (url.endsWith(element)) {
                image = true;
            }
        }

        try {
        if (image) {
            final String html ="<html><img src=\"" + url + "\"></html>";
            editorPane.setText(html);
        }
        else {
            editorPane.setPage(url);
        }
        } catch(final Throwable exception) {}
    }
    }

    void hyperlink_actionPerformed(final HyperlinkEvent e) {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        setPage(e.getURL().toString());
    }
    }

    void readSettings(final File file) {
    final Vector<String> favs = readLines(file);
    for (int i = 0; i < favs.size(); i++) {
        favComboBox.addItem(favs.elementAt(i));
    }
    }

    void writeSettings(final File file) {
    String output = new String();
    for (int i = 0; i < favComboBox.getItemCount(); i++) {
        output = output + (String)favComboBox.getItemAt(i) + "\r\n";
    }
    writeFile(output, file);
    }

    /**
     * Reads file and returns a Vector of lines
     */
    Vector<String> readLines(final File file) {
        return readLines(file.getPath());
    }
    Vector<String> readLines(final String path) {
    BufferedReader f;
    String line;
    line = "";
    final Vector<String> data = new Vector<String>();

    try {
        f = new BufferedReader(new FileReader(path));
        while ((line = f.readLine()) != null) {
            data.add(line.trim());
        }
        f.close();
    }
    catch (final IOException e){
        logger.log(Level.SEVERE, "Read Error: "+ path, e);
    }
    return data;
    }

    /**
     * Writes a file "file" to "path"
     */
    void writeFile(final String content, final File file) {
    writeFile(content, file.getPath());
    }
    void writeFile(final String content, final String filename) {
    FileWriter f1;
    try {
        f1 = new FileWriter(filename);
        f1.write(content);
        f1.close();
    } catch (final IOException e) {
        logger.log(Level.SEVERE, "Write Error: "+ filename, e);
    }
    }

    /**Constructor*/
    public Browser(final JFrame parent) {
    this.parent = parent;
    init();
    }

}

