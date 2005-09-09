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
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import frost.*;
import frost.boards.*;
import frost.fcp.*;
import frost.gui.model.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.messages.*;
import frost.storage.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class MessageFrame extends JFrame
{
    private class AttachBoardsChooser extends JDialog
    {
		private class AttachBoardsCellRenderer extends DefaultListCellRenderer {

			/* (non-Javadoc)
			 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
			 */
			public Component getListCellRendererComponent(JList list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value != null) {
					Board board = (Board) value;
					setText(board.getName());
				}	
				return this;
			}
		}
    	
        JButton Bcancel;
        List boards;
        JButton Bok;
        JList Lboards;
        boolean okPressed = false;

        /**
         * @param boards
         */
        public AttachBoardsChooser(List boards)
        {	
            super();
            setTitle(language.getString("Choose boards to attach"));
            setModal(true);
            this.boards = boards;
            initGui();
        }
        
        /**
         * 
         */
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

            ListModel boardsModel = new AbstractListModel() {
				public int getSize() {
					return boards.size();
				}
				public Object getElementAt(int index) {
					return boards.get(index);
				}
            };
            Lboards = new JList(boardsModel);
            Lboards.setCellRenderer(new AttachBoardsCellRenderer());
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
        
        /**
         * @return
         */
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
    
    class BuddyComparator implements Comparator {
        /** 
         * compare buddies in lowercase
         */
        public int compare(Object o1, Object o2) {
            String s1 = o1.toString();
            String s2 = o2.toString();
            return s1.toLowerCase().compareTo( s2.toLowerCase() );
        }
    }
    
	/**
	 * 
	 */
	private class Listener implements MouseListener, LanguageListener {

		/**
		 * @param e
		 */
		protected void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				if (e.getSource() == boardsTable) {
					attBoardsPopupMenu.show(boardsTable, e.getX(), e.getY());
				}
				if (e.getSource() == filesTable) {
					attFilesPopupMenu.show(filesTable, e.getX(), e.getY());
				}
				if (e.getSource() == messageTextArea) {
					getMessageBodyPopupMenu().show(messageTextArea, e.getX(), e.getY());
				}
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent event) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent event) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent event) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent event) {
			maybeShowPopup(event);
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent event) {
			maybeShowPopup(event);
		}

		/* (non-Javadoc)
		 * @see frost.util.gui.translation.LanguageListener#languageChanged(frost.util.gui.translation.LanguageEvent)
		 */
		public void languageChanged(LanguageEvent event) {
			refreshLanguage();					
		}
	}
    
	/**
	 * 
	 */
	private class MessageBodyPopupMenu 
		extends JSkinnablePopupMenu 
		implements ActionListener, ClipboardOwner {
		
		private Clipboard clipboard;

		private JTextComponent sourceTextComponent;

		private JMenuItem cutItem = new JMenuItem();
		private JMenuItem copyItem = new JMenuItem();
		private JMenuItem pasteItem = new JMenuItem();
		private JMenuItem cancelItem = new JMenuItem();

		/**
		 * @param sourceTextComponent
		 */
		public MessageBodyPopupMenu(JTextComponent sourceTextComponent) {
			super();
			this.sourceTextComponent = sourceTextComponent;
			initialize();
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == cutItem) {
				cutSelectedText();
			}
			if (e.getSource() == copyItem) {
				copySelectedText();
			}
			if (e.getSource() == pasteItem) {
				pasteText();
			}
		}
		
		/**
		 * 
		 */
		private void copySelectedText() {
			StringSelection selection = new StringSelection(sourceTextComponent.getSelectedText());
			clipboard.setContents(selection, this);
		}
		
		/**
		 * 
		 */
		private void cutSelectedText() {
			StringSelection selection = new StringSelection(sourceTextComponent.getSelectedText());
			clipboard.setContents(selection, this);
			
			int start = sourceTextComponent.getSelectionStart();
			int end = sourceTextComponent.getSelectionEnd();
			try {
				sourceTextComponent.getDocument().remove(start, end - start);
			} catch (BadLocationException ble) {
				logger.log(Level.SEVERE, "Problem while cutting text.", ble);
			}
		}
		
		/**
		 * 
		 */
		private void pasteText() {
			Transferable clipboardContent = clipboard.getContents(this);
			try {
				String text = (String) clipboardContent.getTransferData(DataFlavor.stringFlavor);
				
				Caret caret = sourceTextComponent.getCaret();
				int p0 = Math.min(caret.getDot(), caret.getMark());
                int p1 = Math.max(caret.getDot(), caret.getMark());
				
				Document document = sourceTextComponent.getDocument();
				
				if (document instanceof PlainDocument) {
					((PlainDocument) document).replace(p0, p1 - p0, text, null);
				} else {
					if (p0 != p1) {
						document.remove(p0, p1 - p0);
                    }
					document.insertString(p0, text, null);
				}
			} catch (IOException ioe) {
				logger.log(Level.SEVERE, "Problem while pasting text.", ioe);
			} catch (UnsupportedFlavorException ufe) {
				logger.log(Level.SEVERE, "Problem while pasting text.", ufe);
			} catch (BadLocationException ble) {
				logger.log(Level.SEVERE, "Problem while pasting text.", ble);
			}
		}

		/**
		 *  
		 */
		private void initialize() {
			refreshLanguage();
			
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			clipboard = toolkit.getSystemClipboard();
			
			cutItem.addActionListener(this);
			copyItem.addActionListener(this);
			pasteItem.addActionListener(this);

			add(cutItem);
			add(copyItem);
			add(pasteItem);
			addSeparator();
			add(cancelItem);
		}

		/**
		 *  
		 */
		private void refreshLanguage() {
			cutItem.setText(language.getString("Cut"));
			copyItem.setText(language.getString("Copy"));
			pasteItem.setText(language.getString("Paste"));
			cancelItem.setText(language.getString("Cancel"));
		}
		
		/* (non-Javadoc)
		 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
		 */
		public void lostOwnership(Clipboard clipboard, Transferable contents) {
			// Nothing here
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
		 */
		public void show(Component invoker, int x, int y) {
			if (sourceTextComponent.getSelectedText() != null) {
				cutItem.setEnabled(true);
				copyItem.setEnabled(true);
			} else {
				cutItem.setEnabled(false);
				copyItem.setEnabled(false);
			}
			Transferable clipboardContent = clipboard.getContents(this);
			if ((clipboardContent != null) &&
					(clipboardContent.isDataFlavorSupported(DataFlavor.stringFlavor))) {
				pasteItem.setEnabled(true);
			} else {
				pasteItem.setEnabled(false);
			}
			super.show(invoker, x, y);
		}
	}

    /**
     * 
     */
    private class MFAttachedBoard implements TableMember
    {
        Board aBoard;
        
        /**
         * @param ab
         */
        public MFAttachedBoard(Board ab)
        {
            aBoard = ab;
        }
        
        /* (non-Javadoc)
         * @see frost.gui.model.TableMember#compareTo(frost.gui.model.TableMember, int)
         */
        public int compareTo( TableMember anOther, int tableColumIndex )
        {
            Comparable c1 = (Comparable)getValueAt(tableColumIndex);
            Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
            return c1.compareTo( c2 );
        }
        
        /**
         * @return
         */
        public Board getBoardObject()
        {
            return aBoard;
        }

		/* (non-Javadoc)
		 * @see frost.gui.model.TableMember#getValueAt(int)
		 */
		public Object getValueAt(int column) {
			switch (column) {
				case 0 :
					return aBoard.getName();
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

    /**
     * 
     */
    private class MFAttachedBoardsTable extends SortedTable
    {
        /**
         * @param m
         */
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
    
    /**
     * 
     */
    private class MFAttachedBoardsTableModel extends SortedTableModel
    {
        protected final Class columnClasses[] = {
            String.class, 
            String.class,
            String.class,
			String.class
        };
        protected final String columnNames[] = {
            "Boardname",
            "public key",
            "Private key", 
            "Description"
        };

        /**
         * 
         */
        public MFAttachedBoardsTableModel()
        {
            super();
        }
        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnClass(int)
         */
        public Class getColumnClass(int column)
        {
            if( column >= 0 && column < columnClasses.length )
                return columnClasses[column];
            return null;
        }
        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount()
        {
            return columnNames.length;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnName(int)
         */
        public String getColumnName(int column)
        {
            if( column >= 0 && column < columnNames.length )
                return columnNames[column];
            return null;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#isCellEditable(int, int)
         */
        public boolean isCellEditable(int row, int col)
        {
            return false;
        }
        
        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
         */
        public void setValueAt(Object aValue, int row, int column) {}
    }
    
    /**
     * 
     */
    private class MFAttachedFile implements TableMember
    {
        File aFile;
        
        /**
         * @param af
         */
        public MFAttachedFile(File af)
        {
            aFile = af;
        }
        
        /* (non-Javadoc)
         * @see frost.gui.model.TableMember#compareTo(frost.gui.model.TableMember, int)
         */
        public int compareTo( TableMember anOther, int tableColumIndex )
        {
            Comparable c1 = (Comparable)getValueAt(tableColumIndex);
            Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
            return c1.compareTo( c2 );
        }
        
        /**
         * @return
         */
        public File getFile()
        {
            return aFile;
        }
        
        /* (non-Javadoc)
         * @see frost.gui.model.TableMember#getValueAt(int)
         */
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
    
    /**
     * 
     */
    private class MFAttachedFilesTable extends SortedTable
    {
        /**
         * @param m
         */
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
    
    /**
     * 
     */
    private class MFAttachedFilesTableModel extends SortedTableModel
    {
        protected final Class columnClasses[] = {
            String.class,
            String.class
        };
        
        protected final String columnNames[] = {
            "Filename",
            "Size"
        };

        /**
         * 
         */
        public MFAttachedFilesTableModel()
        {
            super();
        }
        
        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnClass(int)
         */
        public Class getColumnClass(int column)
        {
            if( column >= 0 && column < columnClasses.length )
                return columnClasses[column];
            return null;
        }
        
        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount()
        {
            return columnNames.length;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnName(int)
         */
        public String getColumnName(int column)
        {
            if( column >= 0 && column < columnNames.length )
                return columnNames[column];
            return null;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#isCellEditable(int, int)
         */
        public boolean isCellEditable(int row, int col)
        {
            return false;
        }
        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
         */
        public void setValueAt(Object aValue, int row, int column) {}
    }

	private LocalIdentity myId;

	private static Logger logger = Logger.getLogger(MessageFrame.class.getName());
	
    private Language language;

    private Listener listener = new Listener();

    private boolean initialized = false;
    
    private Board board;
    private String from;
    private String subject;
    private String lastUsedDirectory;
    private String keypool;
    private boolean state;
    private SettingsClass frostSettings;
        
    private MFAttachedBoardsTable boardsTable;
    private MFAttachedFilesTable filesTable;
    private MFAttachedBoardsTableModel boardsTableModel;
    private MFAttachedFilesTableModel filesTableModel;
    
	private JSplitPane messageSplitPane = null;
	private JSplitPane attachmentsSplitPane = null;
	private JScrollPane filesTableScrollPane;
	private JScrollPane boardsTableScrollPane;
    
	private JSkinnablePopupMenu attFilesPopupMenu;
	private JSkinnablePopupMenu attBoardsPopupMenu;
	private MessageBodyPopupMenu messageBodyPopupMenu;
    
	private JButton Bsend = new JButton(new ImageIcon(this.getClass().getResource("/data/send.gif")));
	private JButton Bcancel = new JButton(new ImageIcon(this.getClass().getResource("/data/remove.gif")));
	private JButton BattachFile = new JButton(new ImageIcon(this.getClass().getResource("/data/attachment.gif")));
	private JButton BattachBoard= new JButton(new ImageIcon(MainFrame.class.getResource("/data/attachmentBoard.gif")));

	private JCheckBox sign = new JCheckBox();
    JCheckBox encrypt = new JCheckBox("Encrypt for");
    JComboBox buddies;
    private JCheckBox addAttachedFilesToUploadTable = new JCheckBox();

    private JLabel Lboard = new JLabel();
    private JLabel Lfrom = new JLabel();
    private JLabel Lsubject = new JLabel();    
    private JTextField TFboard = new JTextField(); // Board (To)
    private JTextField fromTextField = new JTextField(); // From
    private JTextField subjectTextField = new JTextField(); // Subject

    private AntialiasedTextArea messageTextArea = new AntialiasedTextArea(); // Text
    private ImmutableArea headerArea = null;
    private String oldSender = null;
    private String signature = null;
    
    private TofTree tofTree;
    
	/**
	 * @param newSettings
	 * @param parentWindow
	 * @param newMyId
	 */
	public MessageFrame(SettingsClass newSettings, Window parentWindow, LocalIdentity newMyId) {
		super();
		this.language = Language.getInstance();
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
			logger.severe("The selected font was not found in your system\n"
					+ "That selection will be changed to \"Monospaced\".");
			frostSettings.setValue(SettingsClass.MESSAGE_BODY_FONT_NAME, "Monospaced");
			tofFont = new Font("Monospaced", fontStyle, fontSize);
		}
		messageTextArea.setFont(tofFont);
		messageTextArea.setAntiAliasEnabled(frostSettings.getBoolValue("messageBodyAA"));
		ImmutableAreasDocument messageDocument = new ImmutableAreasDocument();
		headerArea = new ImmutableArea(messageDocument);
		messageDocument.addImmutableArea(headerArea); //So that the user can't
													  // modify the header of
													  // the message
		messageTextArea.setDocument(messageDocument);

		setSize((int) (parentWindow.getWidth() * 0.75), 
				(int) (parentWindow.getHeight() * 0.75));
		setLocationRelativeTo(parentWindow);
	}

	/**
	 * @param e
	 */
	private void attachBoards_actionPerformed(ActionEvent e) {
		Vector allBoards = MainFrame.getInstance().getTofTreeModel().getAllBoards();
		if (allBoards.size() == 0)
			return;
		Collections.sort(allBoards);

		AttachBoardsChooser chooser = new AttachBoardsChooser(allBoards);
		chooser.setLocationRelativeTo(this);
		Vector chosenBoards = chooser.runDialog();
		if (chosenBoards == null || chosenBoards.size() == 0) // nothing chosed or cancelled
			{
			return;
		}

		for (int i = 0; i < chosenBoards.size(); i++) {
			Board board = (Board) chosenBoards.get(i);

			String privKey = board.getPrivateKey();

			if (privKey != null) {
				int answer =
					JOptionPane.showConfirmDialog(
						this,
						language.getString("MessageFrame.ConfirmBody1") + 
							board.getName() +
							language.getString("MessageFrame.ConfirmBody2"),
						language.getString("MessageFrame.ConfirmTitle"),
						JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.NO_OPTION) {
					privKey = null; // don't provide privkey
				}
			}
			// build a new board because maybe privKey shouldn't be uploaded
			Board aNewBoard =
				new Board(board.getName(), board.getPublicKey(), privKey, board.getDescription());
			MFAttachedBoard ab = new MFAttachedBoard(aNewBoard);
			boardsTableModel.addRow(ab);
		}
		positionDividers();
	}

    /**
     * jButton3 Action Listener (Add attachment(s))
     * @param e
     */
    private void attachFile_actionPerformed(ActionEvent e)
    {
        final JFileChooser fc = new JFileChooser(lastUsedDirectory);
        fc.setDialogTitle(language.getString("Choose file(s) / directory(s) to attach"));
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
                        filesTableModel.addRow( af );
                    }
                }
            }
        }
        else
        {
            logger.fine("Open command cancelled by user.");
        }
        
        positionDividers();
    }

	/**
	 * jButton2 Action Listener (Cancel)
     * @param e
     */
    private void cancel_actionPerformed(ActionEvent e)
    {
        state = false;
        dispose();
    }
	
	/**
	 * @param newBoard
	 * @param newFrom
	 * @param newSubject
	 * @param newText
	 * @param isReply
	 */
	private void composeMessage(
		Board newBoard,
		String newFrom,
		String newSubject,
		String newText,
		boolean isReply,
        String recipient) { // if given compose encrypted reply
			
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
			signature = FileAccess.readFile("signature.txt", "UTF-8").trim();
            if( signature != null ) {
                signature = signature.trim();
                if( signature.length() > 0 ) {
                    signature = "\n-- \n" + signature;
                } else {
                    signature = null;
                }
            }
		}

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			initialize();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception thrown in composeMessage(...)", e);
		}

        // maybe prepare to reply to an encrypted message
        if( recipient != null && recipient.length() > 0 ) {
            sign.setSelected(true);
            encrypt.setSelected(true);
            buddies.removeAllItems();
            buddies.addItem(recipient);
            buddies.setSelectedItem(recipient);
        }
        
        // set sig if msg is marked as signed
        if( sign.isSelected() && signature != null ) {
            text += signature;
        }

		messageTextArea.setText(text);
		headerArea.setStartPos(headerAreaStart);
		headerArea.setEndPos(headerAreaEnd);
		headerArea.setEnabled(true);
		setVisible(true);

		// reset the splitpanes       
		positionDividers();

		// Properly positions the caret (AKA cursor)
		messageTextArea.requestFocusInWindow();
		messageTextArea.getCaret().setDot(caretPos);
		messageTextArea.getCaret().setVisible(true);
	}
    
	/**
	 * @param newBoard
	 * @param newFrom
	 * @param newSubject
	 * @param newText
	 */
	public void composeNewMessage(Board newBoard, String newFrom, String newSubject, String newText) {
		composeMessage(newBoard, newFrom, newSubject, newText, false, null);
	}
    
	/**
	 * @param newBoard
	 * @param newFrom
	 * @param newSubject
	 * @param newText
	 */
	public void composeReply(Board newBoard, String newFrom, String newSubject, String newText) {
		composeMessage(newBoard, newFrom, newSubject, newText, true, null);
	}

    public void composeEncryptedReply(Board newBoard, String newFrom, String newSubject, String newText,
            String recipient) {
        composeMessage(newBoard, newFrom, newSubject, newText, true, recipient);
    }

	/* (non-Javadoc)
	 * @see java.awt.Window#dispose()
	 */
	public void dispose() {
		if (initialized) {
			language.removeLanguageListener(listener);
			initialized = false;
		}
		super.dispose();
	}
	
	/**
	 * @return
	 */
	private MessageBodyPopupMenu getMessageBodyPopupMenu() {
		if (messageBodyPopupMenu == null) {
			messageBodyPopupMenu = new MessageBodyPopupMenu(messageTextArea);
		}
		return messageBodyPopupMenu;
	}

	/**
	 * @throws Exception
	 */
	private void initialize() throws Exception {
		if (!initialized) {
			refreshLanguage();
			language.addLanguageListener(listener);

			ImageIcon frameIcon = new ImageIcon(getClass().getResource("/data/newmessage.gif"));
			setIconImage(frameIcon.getImage());
			setResizable(true);

			boardsTableModel = new MFAttachedBoardsTableModel();
			boardsTable = new MFAttachedBoardsTable(boardsTableModel);
			boardsTableScrollPane = new JScrollPane(boardsTable);
			boardsTable.addMouseListener(listener);

			filesTableModel = new MFAttachedFilesTableModel();
			filesTable = new MFAttachedFilesTable(filesTableModel);
			filesTableScrollPane = new JScrollPane(filesTable);
			filesTable.addMouseListener(listener);

            List budList = Core.getInstance().getIdentities().getAllIdentitiesWithState(FrostIdentities.FRIEND);
            Vector budVec = new Vector(budList);
            if( budVec.size() > 0 ) {
                Collections.sort( budVec, new BuddyComparator() );
                buddies = new JComboBox(budVec);
                buddies.setSelectedItem(budVec.get(0));
            } else {
                buddies = new JComboBox();
            }
            
			MiscToolkit toolkit = MiscToolkit.getInstance();
			toolkit.configureButton(Bsend, "Send message", "/data/send_rollover.gif", language);
			toolkit.configureButton(Bcancel, "Cancel", "/data/remove_rollover.gif", language);
			toolkit.configureButton(
				BattachFile,
				"Add attachment(s)",
				"/data/attachment_rollover.gif",
				language);
			toolkit.configureButton(
				BattachBoard,
				"Add Board(s)",
				"/data/attachmentBoard_rollover.gif",
				language);

			TFboard.setEditable(false);
			TFboard.setText(board.getName());
			fromTextField.setText(from);

			new TextComponentClipboardMenu(TFboard, language);
			new TextComponentClipboardMenu(fromTextField, language);
			new TextComponentClipboardMenu(subjectTextField, language);
			subjectTextField.setText(subject);
			messageTextArea.setLineWrap(true);
			messageTextArea.setWrapStyleWord(true);
			messageTextArea.addMouseListener(listener);

			// check if last msg was signed and set it to remembered state
			if (from.equals(myId.getUniqueName())) {
				fromTextField.setEditable(false);
				sign.setSelected(true);
			}
            
            if( sign.isSelected() && buddies.getItemCount() > 0 ) {
                encrypt.setEnabled(true);
            } else {
                encrypt.setEnabled(false);
            }
            encrypt.setSelected(false);
            buddies.setEnabled(false);
// TODO:
			addAttachedFilesToUploadTable.setSelected(false);

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
            encrypt.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    encrypt_ActionPerformed(e);
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

			JScrollPane bodyScrollPane = new JScrollPane(messageTextArea); // Textscrollpane
			bodyScrollPane.setMinimumSize(new Dimension(100, 50));

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
            panelButtons.add(encrypt);
            panelButtons.add(buddies);
			panelButtons.add(addAttachedFilesToUploadTable);

			JPanel dummyPanel = new JPanel(new BorderLayout());
			dummyPanel.add(panelLabels, BorderLayout.WEST);
			dummyPanel.add(panelTextfields, BorderLayout.CENTER);

			panelToolbar.add(panelButtons, BorderLayout.NORTH);
			panelToolbar.add(dummyPanel, BorderLayout.SOUTH);

			//Put everything together
			attachmentsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, filesTableScrollPane,
                    boardsTableScrollPane);
            attachmentsSplitPane.setResizeWeight(0.5);
            attachmentsSplitPane.setDividerSize(3);
            attachmentsSplitPane.setDividerLocation(0.5);

            messageSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, bodyScrollPane,
                    attachmentsSplitPane);
            messageSplitPane.setDividerSize(0);
            messageSplitPane.setDividerLocation(1.0);
            messageSplitPane.setResizeWeight(1.0);

			panelMain.add(panelToolbar, BorderLayout.NORTH);
			panelMain.add(messageSplitPane, BorderLayout.CENTER);

			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(panelMain, BorderLayout.CENTER);

			initPopupMenu();

			initialized = true;
		}
	}
    
    /**
     * 
     */
    protected void initPopupMenu()
    {
        attFilesPopupMenu = new JSkinnablePopupMenu();
        attBoardsPopupMenu = new JSkinnablePopupMenu();
        
        JMenuItem removeFiles = new JMenuItem(language.getString("Remove"));
        JMenuItem removeBoards = new JMenuItem(language.getString("Remove"));
        
        removeFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedItemsFromTable(filesTable);
            }
        });
        removeBoards.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedItemsFromTable(boardsTable);
            }
        });

        attFilesPopupMenu.add( removeFiles );
        attBoardsPopupMenu.add( removeBoards );
    }

	/**
	 * 
     */
    private void positionDividers() {
        int attachedFiles = filesTableModel.getRowCount();
        int attachedBoards = boardsTableModel.getRowCount();
        if (attachedFiles == 0 && attachedBoards == 0) {
            // Neither files nor boards
            messageSplitPane.setBottomComponent(null);
            messageSplitPane.setDividerSize(0);
            return;
        }
        messageSplitPane.setDividerSize(3);
        messageSplitPane.setDividerLocation(0.75);
        if (attachedFiles != 0 && attachedBoards == 0) {
            //Only files
            messageSplitPane.setBottomComponent(filesTableScrollPane);
            return;
        }
        if (attachedFiles == 0 && attachedBoards != 0) {
            //Only boards
            messageSplitPane.setBottomComponent(boardsTableScrollPane);
            return;
        }
        if (attachedFiles != 0 && attachedBoards != 0) {
            //Both files and boards
            messageSplitPane.setBottomComponent(attachmentsSplitPane);
            attachmentsSplitPane.setTopComponent(filesTableScrollPane);
            attachmentsSplitPane.setBottomComponent(boardsTableScrollPane);
        }
    }

    protected void processWindowEvent(WindowEvent e) {
        if( e.getID() == WindowEvent.WINDOW_CLOSING ) {
            dispose();
        }
        super.processWindowEvent(e);
    }
    
	private void refreshLanguage() {
		setTitle(language.getString("Create message"));
		
		Bsend.setToolTipText(language.getString("Send message"));
		Bcancel.setToolTipText(language.getString("Cancel"));
		BattachFile.setToolTipText(language.getString("Add attachment(s)"));
		BattachBoard.setToolTipText(language.getString("Add Board(s)"));
		
		sign.setText(language.getString("Sign"));
		addAttachedFilesToUploadTable.setText(language.getString("Indexed attachments"));
		
		addAttachedFilesToUploadTable.setToolTipText(
				language.getString("Should file attachments be added to upload table?"));
		
		Lboard.setText(language.getString("Board") + ": ");
		Lfrom.setText(language.getString("From") + ": ");
		Lsubject.setText(language.getString("Subject") + ": ");
	}
        
    /**
     * @param tbl
     */
    protected void removeSelectedItemsFromTable( JTable tbl )
    {
        SortedTableModel m = (SortedTableModel)tbl.getModel();
        int[] sel = tbl.getSelectedRows();
        for(int x=sel.length-1; x>=0; x--)
        {
            m.removeRow(sel[x]);
        }
        positionDividers();
    }
    
    /**
     * jButton1 Action Listener (Send)
     * @param e
     */
    private void send_actionPerformed(ActionEvent e)
    {
        from = fromTextField.getText().trim();
		fromTextField.setText(from);
        subject = subjectTextField.getText().trim();
        subjectTextField.setText(subject); // if a pbl occurs show the subject we checked
        String text = messageTextArea.getText().trim();

        if( subject.equals("No subject") ) {
            int n = JOptionPane.showConfirmDialog( this,
            					language.getString("Do you want to enter a subject?"),
								language.getString("No subject specified!"),
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
            if( n == JOptionPane.YES_OPTION ) {
                return;
            }
        }
        
        if( subject.length() == 0) {
            JOptionPane.showMessageDialog( this,
            					language.getString("You must enter a subject!"),
								language.getString("No subject specified!"),
            					JOptionPane.ERROR);
            return;                               
        }
        if( from.length() == 0) {
            JOptionPane.showMessageDialog( this,
            					language.getString("You must enter a sender name!"),
								language.getString("No 'From' specified!"),
            					JOptionPane.ERROR);
            return;                               
        }

        // for convinience set last used user (maybe obsolete now)
        frostSettings.setValue("userName", from);
        
        // create new MessageObject to upload
        MessageObject mo = new MessageObject();
        mo.setBoard(board.getName());
        mo.setFrom(from);
        mo.setSubject(subject);
        mo.setContent(text);
        
        if( sign.isSelected() ) {
            mo.setPublicKey(myId.getKey());
        }
        
        // MessageUploadThread will set date + time !
        
        // attach all files and boards the user chosed
        for(int x=0; x < filesTableModel.getRowCount(); x++) {
            MFAttachedFile af = (MFAttachedFile)filesTableModel.getRow(x);
            File aChosedFile = af.getFile();
            Board boardObj = null;
            
            SharedFileObject sfo;
            if (aChosedFile.length() > FcpInsert.smallestChunk) {
            	sfo = new FECRedirectFileObject(aChosedFile,boardObj);
            } else { 
            	sfo= new SharedFileObject(aChosedFile, boardObj);
            }
			if( addAttachedFilesToUploadTable.isSelected() ) {
				sfo.setOwner(sign.isSelected() ?
									Mixed.makeFilename(myId.getUniqueName()) :
									"Anonymous");
			}
            FileAttachment fa = new FileAttachment(sfo);
            mo.addAttachment(fa);
        }
        for(int x=0; x < boardsTableModel.getRowCount(); x++) {
            MFAttachedBoard ab = (MFAttachedBoard)boardsTableModel.getRow(x);
            Board aChosedBoard = ab.getBoardObject();
            BoardAttachment ba = new BoardAttachment(aChosedBoard);
            mo.addAttachment(ba);
        }

        Identity recipient = null;
        if( encrypt.isEnabled() && encrypt.isSelected() ) {
            recipient = (Identity)buddies.getSelectedItem();
            if( recipient == null ) {
                JOptionPane.showMessageDialog( this,
                        "Can't encrypt, no recipient choosed!",
                        "ERROR",
                        JOptionPane.ERROR);
                return;                               
            }
            mo.setRecipient(recipient.getUniqueName());
        }
        // start upload thread which also saves the file, uploads attachments and signs if choosed
        tofTree.getRunningBoardUpdateThreads().startMessageUpload(
                                              board,
                                              mo,
                                              null,
                                              recipient);

        frostSettings.setValue("lastUsedDirectory", lastUsedDirectory);
        try {
        	frostSettings.save();
		} catch (StorageException se) {
			logger.log(Level.SEVERE, "Error while saving the settings.", se);
		}
        
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
            if( buddies.getItemCount() > 0 ) {
                encrypt.setEnabled(true);
            }
            // add signature if not existing
            String txt = messageTextArea.getText();
            if (signature != null && !messageTextArea.getText().endsWith(signature)) {
                try {
                    messageTextArea.getDocument().insertString(txt.length(), signature, null);
                } catch (BadLocationException e1) {
                    logger.log(Level.SEVERE, "Error while updating the signature ", e1);
                }
            }
		} else {
			sender = "Anonymous";
			fromTextField.setEditable(true);
            encrypt.setEnabled(false);
            // remove signature if existing
            if (signature != null && messageTextArea.getText().endsWith(signature)) {
                try {
                    messageTextArea.getDocument().remove(messageTextArea.getText().length()-signature.length(), 
                            signature.length());
                } catch (BadLocationException e1) {
                    logger.log(Level.SEVERE, "Error while updating the signature ", e1);
                }
            }
		}
		fromTextField.setText(sender);
	}

    private void encrypt_ActionPerformed(ActionEvent e) {
        if( encrypt.isSelected() ) {
            buddies.setEnabled(true);
        } else {
            buddies.setEnabled(false);
        }
    }

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
	
	/**
	 * @param tofTree
	 */
	public void setTofTree(TofTree tofTree) {
		this.tofTree = tofTree;
	}
}
