/*
 HelpBrowser.java / Frost
 Copyright (C) 2006  Jan-Thomas Czornack <jantho@users.sourceforge.net>
 
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
package frost.gui.help;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;

/**
 * Browser Component
 * @author Jantho
 */
public class HelpBrowser extends JPanel {

    private static Logger logger = Logger.getLogger(HelpBrowser.class.getName());

    private String last_url;
    private String url_prefix;
    private String url_locale;

    // Global Variables
    JFrame parent;
    // GUI Objects
    JPanel contentPanel;
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton backButton = new JButton(new ImageIcon(this.getClass().getResource("/data/back.png")));
    JButton homeButton = new JButton(new ImageIcon(this.getClass().getResource("/data/gohome.png")));
    JButton forwardButton = new JButton(new ImageIcon(this.getClass().getResource("/data/forward.png")));
    JButton addPageButton = new JButton(new ImageIcon(this.getClass().getResource("/data/bookmark_add.png")));

    JEditorPane editorPane = new JEditorPane();

    JComboBox urlComboBox = new JComboBox();
    JComboBox favComboBox = new JComboBox();

    JScrollPane scrollPane = new JScrollPane(editorPane);

    JSplitPane splitPane = new JSplitPane();

    private void init() {

        editorPane.setEditorKit(new HelpHTMLEditorKit());
        urlComboBox.setEditable(true);

        // Browser Link Listener
        editorPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                hyperlink_actionPerformed(e);
            }
        });

        // backButton Action Listener
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int i = urlComboBox.getSelectedIndex();
                if( i > 0 ) {
                    i--;
                    urlComboBox.setSelectedIndex(i);
                }
            }
        });

        // forwardButton Action Listener
        forwardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int i = urlComboBox.getSelectedIndex();
                if( i < urlComboBox.getItemCount() - 1 ) {
                    i++;
                    urlComboBox.setSelectedIndex(i);
                }
            }
        });

        // homeButton Action Listener
        homeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setHelpPage("index.html");
            }
        });

        // addPageButton Action Listener
        addPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Add url to favComboBox
                boolean exists = false;
                for( int i = 0; i < favComboBox.getItemCount(); i++ ) {
                    if( ((String) favComboBox.getItemAt(i)).equals(last_url) ) {
                        exists = true;
                        favComboBox.setSelectedItem(last_url);
                    }
                }
                if( !exists ) {
                    favComboBox.addItem(last_url);
                    favComboBox.setSelectedItem(last_url);
                    //writeSettings(new File("browser.ini"));
                }
            }
        });

        // removePageButton Action Listener
        //removePageButton.addActionListener(new java.awt.event.ActionListener() {
        //      public void actionPerformed(ActionEvent e) {
        //          favComboBox.removeItem(editorPane.getPage().toString());
        //writeSettings(new File("browser.ini"));
        //      }
        //    });

        // urlComboBox Action Listener
        urlComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setHelpPage((String) urlComboBox.getSelectedItem());
            }
        });

        // favComboBox Action Listener
        favComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setHelpPage((String) favComboBox.getSelectedItem());
            }
        });

        contentPanel = this;
        contentPanel.setLayout(new BorderLayout());

        buttonPanel.add(backButton);
        buttonPanel.add(homeButton);
        buttonPanel.add(forwardButton);

        buttonPanel.add(addPageButton);
        buttonPanel.add(favComboBox);

        editorPane.setEditable(false);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.NORTH);
        contentPanel.add(urlComboBox, BorderLayout.SOUTH);

        //readSettings(new File("browser.ini"));

        setHelpPage("index.html");
    }

    /*
     String makeStartPage() {
     String html = new String();
     html = "<html><body>Start<HR>";
     for (int i = 0; i < favComboBox.getItemCount(); i++) {
     html = html + "<a href=\"" + (String)favComboBox.getItemAt(i) + "\">" + 
     (String)favComboBox.getItemAt(i) + "</a><br>";
     }           
     
     html = html + "</body></html>";
     return html;
     }
     */
    void setHelpPage(String url) {

        if( url == null ) {
            url = "index.html";
        }
        //System.out.println("Bum01:" + url);
        //System.out.println("Bum01:" + url_prefix);
        if( url.startsWith(url_prefix) ) {
            //System.out.println("Bum02:" + url_prefix.length());
            url = url.substring(url_prefix.length());
            //System.out.println("Bum03:" + url);
        }

        last_url = url;

        // Add url to urlComboBox
        boolean exists = false;
        for( int i = 0; i < urlComboBox.getItemCount(); i++ ) {
            if( ((String) urlComboBox.getItemAt(i)).equals(url) ) {
                exists = true;
                urlComboBox.setSelectedItem(url);
            }
        }

        if( !exists ) {
            int i = urlComboBox.getSelectedIndex();
            if( i == -1 || urlComboBox.getItemCount() == 0 )
                i = 0;
            else
                i++;
            urlComboBox.insertItemAt(url, i);
            urlComboBox.setSelectedItem(url);
        }

        try {
            editorPane.setPage(url_prefix + url_locale + url);
        } catch (IOException e) {
            logger.log(Level.INFO, "Help: Missing translation '" + url_locale + "' for: " + url);
            try {
                editorPane.setPage(url_prefix + url);
            } catch (IOException e1) {
                logger.log(Level.INFO, "Help: Missing file: " + url);
            }
        }
    }

    void hyperlink_actionPerformed(HyperlinkEvent e) {
        if( e.getEventType() == HyperlinkEvent.EventType.ENTERED ) {
            ((JEditorPane) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }

        if( e.getEventType() == HyperlinkEvent.EventType.EXITED ) {
            ((JEditorPane) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return;
        }

        if( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED ) {
            JEditorPane pane = (JEditorPane) e.getSource();
            if( e instanceof HTMLFrameHyperlinkEvent ) {
                HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
                HTMLDocument doc = (HTMLDocument) pane.getDocument();
                doc.processHTMLFrameHyperlinkEvent(evt);
            } else {
                setHelpPage(e.getURL().toString());
            }
        }
    }

    /**Constructor*/
    public HelpBrowser(String locale, JFrame parent, String zipfile) {
        this.parent = parent;
        this.url_prefix = "jar:file:" + zipfile + "!/";
        SetHelpLocale(locale);
        init();
    }

    void SetHelpLocale(String newLocale) {
        // Hier ist ne schoene stelle zum pruefen.
        if( newLocale.equals("default") ) {
            url_locale = "";
        } else {
            url_locale = newLocale;
        }
    }
}
