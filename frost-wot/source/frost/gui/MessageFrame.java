/*
MessageFrame.java / Frost
Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import frost.*;
import frost.fcp.FcpInsert;
import frost.gui.model.*;
import frost.gui.objects.FrostBoardObject;
import frost.identities.LocalIdentity;
import frost.messages.*;
import frost.util.gui.*;

public class MessageFrame extends JFrame
{
	private LocalIdentity myId;

	private static Logger logger = Logger.getLogger(MessageFrame.class.getName());
	
    private ResourceBundle languageResource;

    //------------------------------------------------------------------------
    // Class Vars
    //------------------------------------------------------------------------
    FrostBoardObject board;
    String from;
    String subject;
    String lastUsedDirectory;
    String keypool;
    boolean state;
    SettingsClass frostSettings;
    
    private boolean initialized = false;
    
    MFAttachedBoardsTable attBoardsTable;
    MFAttachedFilesTable attFilesTable;
    MFAttachedBoardsTableModel attBoardsTableModel;
    MFAttachedFilesTableModel attFilesTableModel;
    
    JSplitPane attachmentSplitPane;
    JSplitPane boardSplitPane;
    
	JSkinnablePopupMenu attFilesPopupMenu;
	JSkinnablePopupMenu attBoardsPopupMenu;
    
    //------------------------------------------------------------------------
    // Generate objects
    //------------------------------------------------------------------------
    JButton Bsend = new JButton(new ImageIcon(this.getClass().getResource("/data/send.gif")));
    JButton Bcancel = new JButton(new ImageIcon(this.getClass().getResource("/data/remove.gif")));
    JButton BattachFile = new JButton(new ImageIcon(this.getClass().getResource("/data/attachment.gif")));
    JButton BattachBoard= new JButton(new ImageIcon(MainFrame.class.getResource("/data/attachmentBoard.gif")));

    JCheckBox sign = new JCheckBox();
    JCheckBox addAttachedFilesToUploadTable = new JCheckBox();

    JTextField TFboard = new JTextField(); // Board (To)
    private JTextField fromTextField = new JTextField(); // From
    private JTextField subjectTextField = new JTextField(); // Subject

    private AntialiasedTextArea messageTextArea = new AntialiasedTextArea(); // Text
    private ImmutableArea headerArea = null;
    private String oldSender = null;

	private void initialize() throws Exception {
		if (!initialized) {
			//------------------------------------------------------------------------
			// Configure objects
			//------------------------------------------------------------------------

			setIconImage(Toolkit.getDefaultToolkit().createImage(getClass().getResource("/data/newmessage.gif")));
			setTitle(languageResource.getString("Create message"));
			setResizable(true);

			attBoardsTableModel = new MFAttachedBoardsTableModel();
			attBoardsTable = new MFAttachedBoardsTable(attBoardsTableModel);
			JScrollPane attBoardsScroller = new JScrollPane(attBoardsTable);
			attBoardsTable.addMouseListener(new AttBoardsTablePopupMenuMouseListener());

			attFilesTableModel = new MFAttachedFilesTableModel();
			attFilesTable = new MFAttachedFilesTable(attFilesTableModel);
			JScrollPane attFilesScroller = new JScrollPane(attFilesTable);
			attFilesTable.addMouseListener(new AttFilesTablePopupMenuMouseListener());

			MiscToolkit toolkit = MiscToolkit.getInstance();
			toolkit.configureButton(Bsend, "Send message", "/data/send_rollover.gif", languageResource);
			toolkit.configureButton(Bcancel, "Cancel", "/data/remove_rollover.gif", languageResource);
			toolkit.configureButton(
				BattachFile,
				"Add attachment(s)",
				"/data/attachment_rollover.gif",
				languageResource);
			toolkit.configureButton(
				BattachBoard,
				"Add Board(s)",
				"/data/attachmentBoard_rollover.gif",
				languageResource);

			sign.setText(languageResource.getString("Sign"));
			addAttachedFilesToUploadTable.setText(languageResource.getString("Indexed attachments"));

			TFboard.setEditable(false);
			TFboard.setText(board.toString());
			fromTextField.setText(from);

			subjectTextField.setText(subject);
			messageTextArea.setLineWrap(true);
			messageTextArea.setWrapStyleWord(true);

			// check if last msg was signed and set it to remembered state
			if (from.equals(myId.getUniqueName())) {
				fromTextField.setEditable(false);
				sign.setSelected(true);
			}

			addAttachedFilesToUploadTable.setSelected(false);
			addAttachedFilesToUploadTable.setToolTipText(
				languageResource.getString("Should file attachments be added to upload table?"));

			//------------------------------------------------------------------------
			// Actionlistener
			//------------------------------------------------------------------------
			Bsend.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					send_actionPerformed(e);
				}
			});
			Bcancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cancel_actionPerformed(e);
				}
			});
			BattachFile.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					attachFile_actionPerformed(e);
				}
			});
			BattachBoard.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					attachBoards_actionPerformed(e);
				}
			});
			sign.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sign_ActionPerformed(e);
				}
			});
			fromTextField.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					updateHeaderArea();
				}
				public void insertUpdate(DocumentEvent e) {
					updateHeaderArea();
				}
				public void removeUpdate(DocumentEvent e) {
					updateHeaderArea();
				}
			});		
			AbstractDocument doc = (AbstractDocument) fromTextField.getDocument();
			doc.setDocumentFilter(new DocumentFilter() {
                /*
                 * (non-Javadoc)
                 * 
                 * @see javax.swing.text.DocumentFilter#insertString(javax.swing.text.DocumentFilter.FilterBypass,
                 *      int, java.lang.String, javax.swing.text.AttributeSet)
                 */
                public void insertString(DocumentFilter.FilterBypass fb, int offset, String string,
                        AttributeSet attr) throws BadLocationException {
                    
                    if (fromTextField.isEditable()) {
                        string = string.replaceAll("@","");
                    }
                    super.insertString(fb, offset, string, attr);

                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see javax.swing.text.DocumentFilter#replace(javax.swing.text.DocumentFilter.FilterBypass,
                 *      int, int, java.lang.String,
                 *      javax.swing.text.AttributeSet)
                 */
                public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text,
                        AttributeSet attrs) throws BadLocationException {
                    
                    if (fromTextField.isEditable()) {
                        text = text.replaceAll("@","");
                    }
                    super.replace(fb, offset, length, text, attrs);
                    
                }
            });
			
			//------------------------------------------------------------------------
			// Append objects
			//------------------------------------------------------------------------
			JPanel panelMain = new JPanel(new BorderLayout()); // Main Panel
			JPanel panelTextfields = new JPanel(new BorderLayout()); // Textfields
			JPanel panelToolbar = new JPanel(new BorderLayout()); // Toolbar / Textfields
			JPanel panelLabels = new JPanel(new BorderLayout()); // Labels
			JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

			JLabel Lboard = new JLabel(languageResource.getString("Board") + ": ");
			JLabel Lfrom = new JLabel(languageResource.getString("From") + ": ");
			JLabel Lsubject = new JLabel(languageResource.getString("Subject") + ": ");

			JScrollPane textScroller = new JScrollPane(messageTextArea); // Textscrollpane
			textScroller.setMinimumSize(new Dimension(100, 50));

			panelLabels.add(Lboard, BorderLayout.NORTH);
			panelLabels.add(Lfrom, BorderLayout.CENTER);
			panelLabels.add(Lsubject, BorderLayout.SOUTH);

			panelTextfields.add(TFboard, BorderLayout.NORTH);
			panelTextfields.add(fromTextField, BorderLayout.CENTER);
			panelTextfields.add(subjectTextField, BorderLayout.SOUTH);

			panelButtons.add(Bsend);
			panelButtons.add(Bcancel);
			panelButtons.add(BattachFile);
			panelButtons.add(BattachBoard);
			panelButtons.add(sign);
			panelButtons.add(addAttachedFilesToUploadTable);

			JPanel dummyPanel = new JPanel(new BorderLayout());
			dummyPanel.add(panelLabels, BorderLayout.WEST);
			dummyPanel.add(panelTextfields, BorderLayout.CENTER);

			panelToolbar.add(panelButtons, BorderLayout.NORTH);
			panelToolbar.add(dummyPanel, BorderLayout.SOUTH);

			attachmentSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textScroller, attFilesScroller);
			boardSplitPane =
				new JSplitPane(JSplitPane.VERTICAL_SPLIT, attachmentSplitPane, attBoardsScroller);

			boardSplitPane.setResizeWeight(1);
			attachmentSplitPane.setResizeWeight(1);

			panelMain.add(panelToolbar, BorderLayout.NORTH);
			panelMain.add(boardSplitPane, BorderLayout.CENTER);

			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(panelMain, BorderLayout.CENTER);

			initPopupMenu();

			initialized = true;
		}
	}
    
    protected void initPopupMenu()
    {
        attFilesPopupMenu = new JSkinnablePopupMenu();
        attBoardsPopupMenu = new JSkinnablePopupMenu();
        
        JMenuItem removeFiles = new JMenuItem(languageResource.getString("Remove"));
        JMenuItem removeBoards = new JMenuItem(languageResource.getString("Remove"));
        
        removeFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedItemsFromTable(attFilesTable);
            }
        });
        removeBoards.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedItemsFromTable(attBoardsTable);
            }
        });

        attFilesPopupMenu.add( removeFiles );
        attBoardsPopupMenu.add( removeBoards );
    }
    
    /**jButton1 Action Listener (Send)*/
    private void send_actionPerformed(ActionEvent e)
    {
        from = fromTextField.getText().trim();
		fromTextField.setText(from);
        subject = subjectTextField.getText().trim();
        subjectTextField.setText(subject); // if a pbl occurs show the subject we checked
        String text = messageTextArea.getText().trim();

        boolean quit = true;

        if( subject.equals("No subject") )
        {
            int n = JOptionPane.showConfirmDialog( this,
								languageResource.getString("Do you want to enter a subject?"),
								languageResource.getString("No subject specified!"),
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
            if( n == JOptionPane.YES_OPTION )
            {
                return;
            }
        }
        
        if( subject.length() == 0)
        {
            JOptionPane.showMessageDialog( this,
								languageResource.getString("You must enter a subject!"),
								languageResource.getString("No subject specified!"),
            					JOptionPane.ERROR);
            return;                               
        }
        if( from.length() == 0)
        {
            JOptionPane.showMessageDialog( this,
								languageResource.getString("You must enter a sender name!"),
								languageResource.getString("No 'From' specified!"),
            					JOptionPane.ERROR);
            return;                               
        }

        // for convinience set last used user (maybe obsolete now)
        frostSettings.setValue("userName", from);
        
        // create new MessageObject to upload
        MessageObject mo = new MessageObject();
        mo.setBoard(board.getBoardName());
        mo.setFrom(from);
        mo.setSubject(subject);
        mo.setContent(text);
        if( sign.isSelected() )
        {
            mo.setPublicKey(myId.getKey());
        }
        // MessageUploadThread will set date + time !
        
        // attach all files and boards the user chosed
        for(int x=0; x < attFilesTableModel.getRowCount(); x++)
        {
            MFAttachedFile af = (MFAttachedFile)attFilesTableModel.getRow(x);
            File aChosedFile = af.getFile();
            FrostBoardObject boardObj = null;
            
            SharedFileObject sfo;
            if (aChosedFile.length() > FcpInsert.smallestChunk)
            	sfo = new FECRedirectFileObject(aChosedFile,boardObj);
            else 
            	sfo= new SharedFileObject(aChosedFile, boardObj);
			if( addAttachedFilesToUploadTable.isSelected() )
			{
						sfo.setOwner(sign.isSelected() ?
											Mixed.makeFilename(myId.getUniqueName()) :
											"Anonymous");
			}
			
			
            FileAttachment fa = new FileAttachment(sfo);
            mo.getAttachmentList().add(fa);
        }
        for(int x=0; x < attBoardsTableModel.getRowCount(); x++)
        {
            MFAttachedBoard ab = (MFAttachedBoard)attBoardsTableModel.getRow(x);
            FrostBoardObject aChosedBoard = ab.getBoardObject();
            BoardAttachment ba = new BoardAttachment(aChosedBoard);
            mo.getAttachmentList().add(ba);
        }

        // start upload thread which also saves the file, uploads attachments+signs if choosed
        MainFrame.getInstance().getRunningBoardUpdateThreads().startMessageUpload(
                                              board,
                                              mo,
                                              null);

        frostSettings.setValue("lastUsedDirectory", lastUsedDirectory);
        frostSettings.save();
        
        state = true; // exit state
        
        setVisible(false);        
        dispose();
    }
    
	/**
	 * @param e
	 */
	private void sign_ActionPerformed(ActionEvent e) {
		String sender;
		if (sign.isSelected()) {
			sender = myId.getUniqueName();
			fromTextField.setEditable(false);
		} else {
			sender = "Anonymous";
			fromTextField.setEditable(true);
		}
		fromTextField.setText(sender);
	}

	/**
	 * 
	 */
	private void updateHeaderArea() {
		headerArea.setEnabled(false);
		String sender = fromTextField.getText();
		try {
			messageTextArea.getDocument().remove(headerArea.getStartPos() + 6, oldSender.length());
			messageTextArea.getDocument().insertString(headerArea.getStartPos() + 6, sender, null);
			oldSender = sender;
			headerArea.setEnabled(true);
		} catch (BadLocationException exception) {
			logger.log(Level.SEVERE, "Error while updating the message header", exception);
		}
	}

	/**jButton2 Action Listener (Cancel)*/
    private void cancel_actionPerformed(ActionEvent e)
    {
        state = false;
        dispose();
    }

    /**jButton3 Action Listener (Add attachment(s))*/
    private void attachFile_actionPerformed(ActionEvent e)
    {
        final JFileChooser fc = new JFileChooser(lastUsedDirectory);
        fc.setDialogTitle(languageResource.getString("Choose file(s) / directory(s) to attach"));
        fc.setFileHidingEnabled(false);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);

        int returnVal = fc.showOpenDialog(MessageFrame.this);
        if( returnVal == JFileChooser.APPROVE_OPTION )
        {
            File[] selectedFiles = fc.getSelectedFiles();
            for( int i = 0; i < selectedFiles.length; i++ )
            {
                // for convinience remember last used directory
                lastUsedDirectory = selectedFiles[i].getPath();
                
                // collect all choosed files + files in all choosed directories
                ArrayList allFiles = FileAccess.getAllEntries(selectedFiles[i], "");
                for (int j = 0; j < allFiles.size(); j++) 
                {
                    File aFile = (File)allFiles.get(j);
                    if (aFile.isFile() && aFile.length() > 0) 
                    {
                        MFAttachedFile af = new MFAttachedFile( aFile );
                        attFilesTableModel.addRow( af );
                    }
                }
            }
        }
        else
        {
            logger.fine("Open command cancelled by user.");
        }
        
        updateAttachmentSplitPanes();
    }

	private void attachBoards_actionPerformed(ActionEvent e) {
		Vector allBoards = MainFrame.getInstance().getTofTree().getAllBoards();
		if (allBoards.size() == 0)
			return;
		Collections.sort(allBoards);

		AttachBoardsChooser chooser = new AttachBoardsChooser(allBoards);
		chooser.setLocationRelativeTo(this);
		Vector chosedBoards = chooser.runDialog();
		if (chosedBoards == null || chosedBoards.size() == 0) // nothing chosed or cancelled
			{
			return;
		}

		for (int i = 0; i < chosedBoards.size(); i++) {
			FrostBoardObject board = (FrostBoardObject) chosedBoards.get(i);

			String privKey = board.getPrivateKey();

			if (privKey != null) {
				int answer =
					JOptionPane.showConfirmDialog(
						this,
						"You have the private key to "
							+ board.toString()
							+ ".  Are you sure you want it attached?\n "
							+ "If you choose NO, only the public key will be attached.",
						"Include private board key?",
						JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.NO_OPTION) {
					privKey = null; // dont provide privkey
				}
			}
			// build a new board because maybe privKey should'nt be uploaded
			FrostBoardObject aNewBoard =
				new FrostBoardObject(board.getBoardName(), board.getPublicKey(), privKey, board.getDescription());
			MFAttachedBoard ab = new MFAttachedBoard(aNewBoard);
			attBoardsTableModel.addRow(ab);
		}
		updateAttachmentSplitPanes();
	}

    protected void updateAttachmentSplitPanes()
    {
        if( attBoardsTableModel.getRowCount() == 0 &&
            attFilesTableModel.getRowCount() == 0 )
        {
            resetSplitPanes();
            return;
        }
        
        // Attachment available
        if( attFilesTableModel.getRowCount() > 0 ) 
        {
            if( attBoardsTableModel.getRowCount() == 0 )
            {
                boardSplitPane.setDividerSize(0);
                boardSplitPane.setDividerLocation(1.0);
            }
            attachmentSplitPane.setDividerLocation(0.8);
            attachmentSplitPane.setDividerSize(3);
        }
        // Board Available
        if( attBoardsTableModel.getRowCount() > 0 ) 
        {
            //only a board, no attachments.
            if(attFilesTableModel.getRowCount() == 0 ) 
            {
                attachmentSplitPane.setDividerSize(0);
                attachmentSplitPane.setDividerLocation(1.0);
            }
            boardSplitPane.setDividerLocation(0.8);
            boardSplitPane.setDividerSize(3);
        }
    }
    
	public void composeNewMessage(FrostBoardObject newBoard, String newFrom, String newSubject, String newText) {
		composeMessage(newBoard, newFrom, newSubject, newText, false);
	}
	
	private void composeMessage(
		FrostBoardObject newBoard,
		String newFrom,
		String newSubject,
		String newText,
		boolean isReply) {
			
		headerArea.setEnabled(false);	
		board = newBoard;
		from = newFrom;
		subject = newSubject;
		String text = newText;

		String date = DateFun.getExtendedDate() + " - " + DateFun.getFullExtendedTime() + "GMT";

		if (isReply) {
			text += "\n\n";
		}
		int headerAreaStart = text.length();//Beginning of non-modifiable area
		text += "----- " + from + " ----- " + date + " -----\n\n";
		int headerAreaEnd = text.length() - 2; //End of non-modifiable area
		oldSender = from;
		
		int caretPos = text.length();

		File signatureFile = new File("signature.txt");
		if (signatureFile.isFile()) {
			String signature = FileAccess.readFile("signature.txt", "UTF-8").trim();
			if (signature.length() > 0) {
				text += "\n-- \n";
				text += signature;
			}
		}

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			initialize();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception thrown in composeMessage(...)", e);
		}

		messageTextArea.setText(text);
		headerArea.setStartPos(headerAreaStart);
		headerArea.setEndPos(headerAreaEnd);
		headerArea.setEnabled(true);
		setVisible(true);

		// reset the splitpanes (java bug)        
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				resetSplitPanes();
			}
		});
		new Thread() {
			public void run() {
				Mixed.wait(10);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						resetSplitPanes();
					}
				});
			} 
		}
		.start();

		// Properly positions the caret (AKA cursor)
		messageTextArea.requestFocusInWindow();
		messageTextArea.getCaret().setDot(caretPos);
		messageTextArea.getCaret().setVisible(true);
	}
    
	public void composeReply(FrostBoardObject newBoard, String newFrom, String newSubject, String newText) {
			composeMessage(newBoard, newFrom, newSubject, newText, true);
	}
    
    protected void resetSplitPanes() 
    {
        // initially hide the attachment tables
        attachmentSplitPane.setDividerSize(0);
        attachmentSplitPane.setDividerLocation(1.0);
        boardSplitPane.setDividerSize(0);
        boardSplitPane.setDividerLocation(1.0);
    }
    
    protected void removeSelectedItemsFromTable( JTable tbl )
    {
        SortedTableModel m = (SortedTableModel)tbl.getModel();
        int[] sel = tbl.getSelectedRows();
        for(int x=sel.length-1; x>=0; x--)
        {
            m.removeRow(sel[x]);
        }
        updateAttachmentSplitPanes();
    }

    protected void processWindowEvent(WindowEvent e)
    {
        if( e.getID() == WindowEvent.WINDOW_CLOSING )
        {
            dispose();
        }
        super.processWindowEvent(e);
    }
    
	/**Constructor*/
	public MessageFrame(
		SettingsClass newSettings,
		Component parentComponent,
		ResourceBundle languageResource,
		LocalIdentity newMyId) {
		super();
		this.languageResource = languageResource;
		myId = newMyId;
		state = false;
		frostSettings = newSettings;
		lastUsedDirectory = frostSettings.getValue("lastUsedDirectory");
		keypool = frostSettings.getValue("keypool.dir");

		String fontName = frostSettings.getValue(SettingsClass.MESSAGE_BODY_FONT_NAME);
		int fontStyle = frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_STYLE);
		int fontSize = frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE);
		Font tofFont = new Font(fontName, fontStyle, fontSize);
		if (!tofFont.getFamily().equals(fontName)) {
			logger.severe("The selected font was not found in your system\n" + 
						  "That selection will be changed to \"Monospaced\".");
			frostSettings.setValue(SettingsClass.MESSAGE_BODY_FONT_NAME, "Monospaced");
			tofFont = new Font("Monospaced", fontStyle, fontSize);
		}
		messageTextArea.setFont(tofFont);
		messageTextArea.setAntiAliasEnabled(frostSettings.getBoolValue("messageBodyAA"));
		ImmutableAreasDocument messageDocument = new ImmutableAreasDocument();
		headerArea = new ImmutableArea(messageDocument);
		messageDocument.addImmutableArea(headerArea);	//So that the user can't modify the header of the message
		messageTextArea.setDocument(messageDocument);

		setSize(600, 460);
		setLocationRelativeTo(parentComponent);
	}

/*************************************************
 ************************************************* 
 **  INTERNAL CLASSES  ***************************
 ************************************************* 
 *************************************************/

    private class MFAttachedBoard implements TableMember
    {
        FrostBoardObject aBoard;
        public MFAttachedBoard(FrostBoardObject ab)
        {
            aBoard = ab;
        }
        public FrostBoardObject getBoardObject()
        {
            return aBoard;
        }
        public int compareTo( TableMember anOther, int tableColumIndex )
        {
            Comparable c1 = (Comparable)getValueAt(tableColumIndex);
            Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
            return c1.compareTo( c2 );
        }

		public Object getValueAt(int column) {
			switch (column) {
				case 0 :
					return aBoard.getBoardName();
				case 1 :
					return (aBoard.getPublicKey() == null) ? "N/A" : aBoard.getPublicKey();
				case 2 :
					return (aBoard.getPrivateKey() == null) ? "N/A" : aBoard.getPrivateKey();
				case 3 :
					return (aBoard.getDescription() == null) ? "N/A" : aBoard.getDescription();
			}
			return "*ERR*";
		}
    }
    
    private class MFAttachedFile implements TableMember
    {
        File aFile;
        public MFAttachedFile(File af)
        {
            aFile = af;
        }
        public File getFile()
        {
            return aFile;
        }
        public int compareTo( TableMember anOther, int tableColumIndex )
        {
            Comparable c1 = (Comparable)getValueAt(tableColumIndex);
            Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
            return c1.compareTo( c2 );
        }
        public Object getValueAt(int column)
        {
            switch(column)
            {
                case 0: return aFile.getName();
                case 1: return ""+aFile.length();
            }
            return "*ERR*";
        }
    }

    private class MFAttachedBoardsTable extends SortedTable
    {
        public MFAttachedBoardsTable(MFAttachedBoardsTableModel m)
        {
            super(m);

            // set column sizes
            int[] widths = {250, 80, 80};
            for (int i = 0; i < widths.length; i++)
            {
                getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }

            // default for sort: sort by name ascending ?
            sortedColumnIndex = 0;
            sortedColumnAscending = true;
            resortTable();
        }
    }
    private class MFAttachedFilesTable extends SortedTable
    {
        public MFAttachedFilesTable(MFAttachedFilesTableModel m)
        {
            super(m);

            // set column sizes
            int[] widths = {250, 80};
            for (int i = 0; i < widths.length; i++)
            {
                getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }

            // default for sort: sort by name ascending ?
            sortedColumnIndex = 0;
            sortedColumnAscending = true;
            resortTable();
        }
    }
    
    private class MFAttachedBoardsTableModel extends SortedTableModel
    {
        protected final String columnNames[] = {
            "Boardname",
            "public key",
            "Private key", 
            "Description"
        };
        protected final Class columnClasses[] = {
            String.class, 
            String.class,
            String.class,
			String.class
        };

        public MFAttachedBoardsTableModel()
        {
            super();
        }

        public boolean isCellEditable(int row, int col)
        {
            return false;
        }

        public String getColumnName(int column)
        {
            if( column >= 0 && column < columnNames.length )
                return columnNames[column];
            return null;
        }
        public int getColumnCount()
        {
            return columnNames.length;
        }
        public Class getColumnClass(int column)
        {
            if( column >= 0 && column < columnClasses.length )
                return columnClasses[column];
            return null;
        }
        public void setValueAt(Object aValue, int row, int column) {}
    }
    
    private class MFAttachedFilesTableModel extends SortedTableModel
    {
        protected final String columnNames[] = {
            "Filename",
            "Size"
        };
        protected final Class columnClasses[] = {
            String.class,
            String.class
        };

        public MFAttachedFilesTableModel()
        {
            super();
        }

        public boolean isCellEditable(int row, int col)
        {
            return false;
        }

        public String getColumnName(int column)
        {
            if( column >= 0 && column < columnNames.length )
                return columnNames[column];
            return null;
        }
        public int getColumnCount()
        {
            return columnNames.length;
        }
        public Class getColumnClass(int column)
        {
            if( column >= 0 && column < columnClasses.length )
                return columnClasses[column];
            return null;
        }
        public void setValueAt(Object aValue, int row, int column) {}
    }

    private class AttachBoardsChooser extends JDialog
    {
        Vector boards;
        JButton Bok;
        JButton Bcancel;
        JList Lboards;
        boolean okPressed = false;

        public AttachBoardsChooser(Vector boards)
        {
            super();
            setTitle(languageResource.getString("Choose boards to attach"));
            setModal(true);
            this.boards = boards;
            initGui();
        }
        private void initGui()
        {
            Bok = new JButton("OK");
            Bok.addActionListener( new ActionListener() {
                   public void actionPerformed(ActionEvent e) {
                       	okPressed = true;
                       	setVisible(false);
                   } });
            Bcancel = new JButton("Cancel");
            Bcancel.addActionListener( new ActionListener() {
                   public void actionPerformed(ActionEvent e) {
                       	okPressed = false;
						setVisible(false);
                   } });
            JPanel buttonsPanel = new JPanel( new FlowLayout(FlowLayout.RIGHT, 8, 8) );
            buttonsPanel.add( Bok );
            buttonsPanel.add( Bcancel );

            Lboards = new JList( boards );
            Lboards.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            JScrollPane listScroller = new JScrollPane( Lboards );
            listScroller.setBorder( new CompoundBorder( new EmptyBorder(5,5,5,5),
                                                        new CompoundBorder( new EtchedBorder(),
                                                                            new EmptyBorder(5,5,5,5) )
                                                      ) );
            getContentPane().add(listScroller, BorderLayout.CENTER);
            getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
            setSize(300, 400);
        }
        public Vector runDialog()
        {
            setVisible(true);
            if( okPressed == false )
                return null;

            Object[] sels = Lboards.getSelectedValues();
            Vector chosed = new Vector( Arrays.asList( sels ) );
            return chosed;
        }
    }
    
    class BuddyComparator implements Comparator
    {
        // compare buddies in lowercase
        public int compare(Object o1, Object o2)
        {
            String s1 = (String)o1;
            String s2 = (String)o2;
            return s1.toLowerCase().compareTo( s2.toLowerCase() );
        }
    }
    
    class AttFilesTablePopupMenuMouseListener implements MouseListener
    {
        public void mouseReleased(MouseEvent event) {
            maybeShowPopup(event);
        }
        public void mousePressed(MouseEvent event) {
            maybeShowPopup(event);
        }
        public void mouseClicked(MouseEvent event) {}
        public void mouseEntered(MouseEvent event) {}
        public void mouseExited(MouseEvent event) {}
        protected void maybeShowPopup(MouseEvent e) {
            if( e.isPopupTrigger() ) {
                attFilesPopupMenu.show(attFilesTable, e.getX(), e.getY());
            }
        }
    }
    class AttBoardsTablePopupMenuMouseListener implements MouseListener
    {
        public void mouseReleased(MouseEvent event) {
            maybeShowPopup(event);
        }
        public void mousePressed(MouseEvent event) {
            maybeShowPopup(event);
        }
        public void mouseClicked(MouseEvent event) {}
        public void mouseEntered(MouseEvent event) {}
        public void mouseExited(MouseEvent event) {}
        protected void maybeShowPopup(MouseEvent e) {
            if( e.isPopupTrigger() ) {
                attBoardsPopupMenu.show(attBoardsTable, e.getX(), e.getY());
            }
        }
    }
    
}
