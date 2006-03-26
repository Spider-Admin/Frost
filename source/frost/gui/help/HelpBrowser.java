/*
 HelpBrowser.java / Frost
 Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * Browser Component
 * @author Jantho
 * modified by notitaccu
 */
public class HelpBrowser extends JPanel {

    private static Logger logger = Logger.getLogger(HelpBrowser.class.getName());
    
   // private String last_url;
    private String url_prefix;
    private String url_locale;
    private String homePage;
    
    private BrowserHistory browserHistory = null;
    
    // Global Variables
    JFrame parent;
    // GUI Objects
    JPanel contentPanel;
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton backButton = new JButton(new ImageIcon(this.getClass().getResource("/data/back.png")));
    JButton homeButton = new JButton(new ImageIcon(this.getClass().getResource("/data/gohome.png")));
    JButton forwardButton = new JButton(new ImageIcon(this.getClass().getResource("/data/forward.png")));
   // JButton addPageButton = new JButton(new ImageIcon(this.getClass().getResource("/data/bookmark_add.png")));

    JEditorPane editorPane = new JEditorPane();

  //  JComboBox urlComboBox = new JComboBox();
 //   JComboBox favComboBox = new JComboBox();

    JScrollPane scrollPane = new JScrollPane(editorPane);

 //   JSplitPane splitPane = new JSplitPane();

    public HelpBrowser(JFrame parent, String locale, String zipfile, String homePage) {
        this.parent = parent;
        this.url_prefix = zipfile;
        this.homePage = homePage;
        SetHelpLocale(locale);
        init();
    }
    
    private void init() {
      
        // history init
        browserHistory = new BrowserHistory();
        browserHistory.resetToHomepage(homePage);

   //     editorPane.setEditorKit(new HelpHTMLEditorKit(url_prefix));
   //     urlComboBox.setEditable(true);

        // Browser Link Listener
        editorPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if( e.getEventType() == HyperlinkEvent.EventType.ENTERED ) {
                    ((JEditorPane) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return;
                }
                if( e.getEventType() == HyperlinkEvent.EventType.EXITED ) {
                    ((JEditorPane) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    return;
                }
                if( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED ) {
                    ((JEditorPane) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    browserHistory.setCurrentPage(e.getURL().toString());
                    setHelpPage(e.getURL().toString());
                    
//                    JEditorPane pane = (JEditorPane) e.getSource();
               /*     if( e instanceof HTMLFrameHyperlinkEvent ) {
                        HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
                        HTMLDocument doc = (HTMLDocument) pane.getDocument();
                        doc.processHTMLFrameHyperlinkEvent(evt);
                    } else { */
//                        setHelpPage(e.getURL().toString());
               //     }
                }
            }
        });

        // backButton Action Listener
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if( browserHistory.isBackwardPossible() ) {
                    setHelpPage(browserHistory.backwardPage());
                }
            }
        });

        // forwardButton Action Listener
        forwardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if( browserHistory.isForwardPossible() ) {
                    setHelpPage(browserHistory.forwardPage());
                }
            }
        });

        // homeButton Action Listener
        homeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                browserHistory.resetToHomepage(homePage);
                setHelpPage(homePage);
            }
        });

/*
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
          removePageButton.addActionListener(new java.awt.event.ActionListener() {
              public void actionPerformed(ActionEvent e) {
                  favComboBox.removeItem(editorPane.getPage().toString());
        //writeSettings(new File("browser.ini"));
              }
            });

   
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
*/

        contentPanel = this;
        contentPanel.setLayout(new BorderLayout());

        buttonPanel.add(backButton);
        buttonPanel.add(homeButton);
        buttonPanel.add(forwardButton);

      //  buttonPanel.add(addPageButton);
      //  buttonPanel.add(favComboBox);

        editorPane.setEditable(false);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.NORTH);
   
      //  contentPanel.add(urlComboBox, BorderLayout.SOUTH);

        //readSettings(new File("browser.ini"));

        setHelpPage(homePage);
    }

    void setHelpPage(String url) {

        editorPane.setEditorKit(new HelpHTMLEditorKit(url_prefix));

        if( url == null ) {
            url = homePage;
        }
        
        if( url.startsWith(url_prefix) ) {
            url = url.substring(url_prefix.length());
        }

        // TODO: - internationalisierung ueberarbeiten, sowas geht schoener
        //       - datum/zeit bei intl beruecksichtigen
        //  temporaer aus 
   //     try {
   //         editorPane.setPage(url_prefix + url_locale + url);
   //     } catch (IOException e) {
   //         logger.log(Level.INFO, "Help: Missing translation '" + url_locale + "' for: " + url);
            try {
                editorPane.setPage(url_prefix + url);
            } catch (IOException e1) {
                logger.log(Level.INFO, "HELP: Missing file: '" + url + "'");
            }
   //     }
            updateBrowserButtons();
    }

    private void updateBrowserButtons() {
        forwardButton.setEnabled( browserHistory.isForwardPossible() ); 
        backButton.setEnabled( browserHistory.isBackwardPossible() ); 
    }

    void SetHelpLocale(String newLocale) {
        // Hier ist ne schoene stelle zum pruefen.        
        if( newLocale.equals("default") ) {
            url_locale = "";
        } else {
            url_locale = newLocale;
        }
    }
    
    private class BrowserHistory {

        private ArrayList history = new ArrayList();
        private int historypos = -1; // this means history ist invalid

        public boolean isForwardPossible() {
            if( historypos < history.size()-1 ) {
                return true;
            }
            return false;
        }
        public String forwardPage() {
            if( !isForwardPossible() ) {
                return null;
            }
            historypos++;
            return (String)history.get(historypos);
        }
        public boolean isBackwardPossible() {
            if( historypos > 0 ) {
                return true;
            }
            return false;
        }
        public String backwardPage() {
            if( !isBackwardPossible() ) {
                return null;
            }
            historypos--;
            return (String)history.get(historypos);
        }
        public void setCurrentPage(String page) {
            // a link was clicked, add this new page after current historypos and clear alll forward pages
            // this is the behaviour of Mozilla too
            if( historypos < history.size()-1 ) {
                history.subList(historypos+1, history.size()).clear();
            }
            history.add(page);
            historypos++;
        }
        public void resetToHomepage(String homepage) {
            history.clear();
            history.add(homepage);
            historypos = 0; // current page is page at index 0
        }
    }
}
