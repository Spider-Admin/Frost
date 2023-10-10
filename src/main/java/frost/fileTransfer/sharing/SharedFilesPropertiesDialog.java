/*
  SharedFilesPropertiesDialog.java / Frost
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
package frost.fileTransfer.sharing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import frost.fileTransfer.SharedFileXmlFile;
import frost.gui.RatingStringProvider;

/**
 * Configure comment, rating and keywords for one or multiple FrostSharedFileItems. 
 */
@SuppressWarnings("serial")
public class SharedFilesPropertiesDialog extends JDialog {

    private JPanel jContentPane = null;
    private JPanel buttonPanel = null;
    private JPanel mainPanel = null;
    private JButton Bok = null;
    private JButton Bcancel = null;
    private JLabel descLabel = null;
    private JLabel Lcomment = null;
    private JLabel Lkeywords = null;
    private JLabel Lrating = null;
    private JComboBox CBrating = null;
    private JTextField TFcomment = null;
    private JTextField TFkeywords = null;
    
    private boolean exitState;
    private String comment = null;
    private String keywords = null;
    private int rating = 0;

    /**
     * @param owner
     */
    public SharedFilesPropertiesDialog(Frame owner) {
        super(owner, true);
        initialize();
        
        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setTitle("Shared files properties");
        this.setContentPane(getJContentPane());
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if( jContentPane == null ) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
            jContentPane.add(getMainPanel(), BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * This method initializes buttonPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getButtonPanel() {
        if( buttonPanel == null ) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.RIGHT);
            buttonPanel = new JPanel();
            buttonPanel.setLayout(flowLayout);
            buttonPanel.add(getBok(), null);
            buttonPanel.add(getBcancel(), null);
        }
        return buttonPanel;
    }

    /**
     * This method initializes mainPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getMainPanel() {
        if( mainPanel == null ) {
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.fill = GridBagConstraints.BOTH;
            gridBagConstraints6.gridy = 3;
            gridBagConstraints6.weightx = 1.0;
            gridBagConstraints6.insets = new Insets(3, 3, 5, 3);
            gridBagConstraints6.anchor = GridBagConstraints.WEST;
            gridBagConstraints6.gridx = 1;
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.fill = GridBagConstraints.BOTH;
            gridBagConstraints5.gridy = 2;
            gridBagConstraints5.weightx = 1.0;
            gridBagConstraints5.insets = new Insets(3, 3, 0, 3);
            gridBagConstraints5.anchor = GridBagConstraints.WEST;
            gridBagConstraints5.gridx = 1;
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.fill = GridBagConstraints.VERTICAL;
            gridBagConstraints4.gridy = 1;
            gridBagConstraints4.weightx = 1.0;
            gridBagConstraints4.anchor = GridBagConstraints.WEST;
            gridBagConstraints4.insets = new Insets(3, 3, 0, 3);
            gridBagConstraints4.gridx = 1;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 0;
            gridBagConstraints3.insets = new Insets(3, 5, 0, 0);
            gridBagConstraints3.anchor = GridBagConstraints.WEST;
            gridBagConstraints3.gridy = 1;
            Lrating = new JLabel();
            Lrating.setText("Rating:");
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.insets = new Insets(3, 5, 5, 0);
            gridBagConstraints2.anchor = GridBagConstraints.WEST;
            gridBagConstraints2.gridy = 3;
            Lkeywords = new JLabel();
            Lkeywords.setText("Keywords:");
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.insets = new Insets(3, 5, 0, 0);
            gridBagConstraints1.anchor = GridBagConstraints.WEST;
            gridBagConstraints1.gridy = 2;
            Lcomment = new JLabel();
            Lcomment.setText("Comment:");
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new Insets(5, 5, 5, 0);
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 0.0;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.gridy = 0;
            descLabel = new JLabel();
            descLabel.setText("Edit properties of file 'abc' / multiple files");
            mainPanel = new JPanel();
            mainPanel.setLayout(new GridBagLayout());
            mainPanel.add(descLabel, gridBagConstraints);
            mainPanel.add(Lcomment, gridBagConstraints1);
            mainPanel.add(Lkeywords, gridBagConstraints2);
            mainPanel.add(Lrating, gridBagConstraints3);
            mainPanel.add(getCBrating(), gridBagConstraints4);
            mainPanel.add(getTFcomment(), gridBagConstraints5);
            mainPanel.add(getTFkeywords(), gridBagConstraints6);
        }
        return mainPanel;
    }

    /**
     * This method initializes Bok	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBok() {
        if( Bok == null ) {
            Bok = new JButton();
            Bok.setText("Ok");
            Bok.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String str;

                    str = getTFcomment().getText().trim(); 
                    if( str.length() > 0 ) {
                        comment = str;
                    } else {
                        comment = null;
                    }

                    str = getTFkeywords().getText().trim(); 
                    if( str.length() > 0 ) {
                        keywords = str;
                    } else {
                        keywords = null;
                    }
                    
                    int selIx = getCBrating().getSelectedIndex();
                    rating = selIx;

                    exitState = true;
                    setVisible(false);
                }
            });
        }
        return Bok;
    }

    /**
     * This method initializes Bcancel	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBcancel() {
        if( Bcancel == null ) {
            Bcancel = new JButton();
            Bcancel.setText("Cancel");
            Bcancel.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    exitState = false;
                    setVisible(false);
                }
            });
        }
        return Bcancel;
    }

    /**
     * This method initializes CBrating	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getCBrating() {
        if( CBrating == null ) {
            CBrating = new JComboBox();
            for( int x=0; x < RatingStringProvider.ratingStrings.length; x++ ) {
                CBrating.addItem(RatingStringProvider.ratingStrings[x]);
            }
        }
        return CBrating;
    }

    /**
     * This method initializes TFcomment	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getTFcomment() {
        if( TFcomment == null ) {
            TFcomment = new JTextField();
            TFcomment.setColumns(32);
            TFcomment.setDocument(new RestrictSizeDocument(SharedFileXmlFile.MAX_COMMENT_LENGTH));
        }
        return TFcomment;
    }
    
    /**
     * This method initializes TFkeywords	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getTFkeywords() {
        if( TFkeywords == null ) {
            TFkeywords = new JTextField();
            TFkeywords.setColumns(32);
            TFkeywords.setDocument(new RestrictSizeDocument(SharedFileXmlFile.MAX_KEYWORDS_LENGTH));
        }
        return TFkeywords;
    }
    
    /**
     * This document restricts the size of the text to a specific length.
     */
    protected class RestrictSizeDocument extends PlainDocument {
        int maxChars;
        public RestrictSizeDocument(int maxChars) {
            this.maxChars = maxChars;
        }
        @Override
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            
            int availableSpace = maxChars - getLength();
            if( availableSpace > 0 ) {
                if( str.length() > availableSpace ) {
                    // cut some characters from end of string
                    str = str.substring(0, availableSpace);
                }
                super.insertString(offs, str, a);
            }
            // else do not insert more characters
        }
    }

    /**
     * @return  true if user clicked ok, otherwise false
     */
    public boolean startDialog(String fileName, int fileCount, FrostSharedFileItem defaults) {

        String labelText;
        if( fileName != null ) {
            labelText = "Edit properties of file '"+fileName+"':";
        } else {
            labelText = "Edit properties of "+fileCount+" files:";
        }
        
        descLabel.setText(labelText);
        
        if( defaults != null ) {
            if( defaults.getComment() != null ) {
                getTFcomment().setText(defaults.getComment());
            }
            if( defaults.getKeywords() != null ) {
                getTFkeywords().setText(defaults.getKeywords());
            }
            getCBrating().setSelectedIndex(defaults.getRating());
        }
        
        exitState = false;
        setVisible(true);
        // blocks
        
        return exitState;
    }
    
    public String getComment() {
        return comment;
    }
    public String getKeywords() {
        return keywords;
    }
    public int getRating() {
        return rating;
    }
    
}  //  @jve:decl-index=0:visual-constraint="10,10"
