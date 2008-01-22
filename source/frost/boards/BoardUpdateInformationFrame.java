package frost.boards;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import frost.threads.*;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class BoardUpdateInformationFrame extends javax.swing.JFrame implements BoardUpdateThreadListener {

    // FIXME: show tabbed pane, summary tab shows accumulated values for each day (all boards) and all days

    private JComboBox cbBoards;
    private JLabel lBoards;
    private JTextArea taContent;
    private JLabel lDates;
    private JComboBox cbDates;

    private static boolean isShowing = false; // flag, is true if frame is shown
    private final TofTree tofTree;
    private final TofTreeModel tofTreeModel;

    public BoardUpdateInformationFrame(final JFrame parentFrame, final TofTree tofTree) {
        super();
        this.tofTree = tofTree;
        this.tofTreeModel = (TofTreeModel) tofTree.getModel();
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        initGUI();
        setTitle("Board update informations (alpha)");
        setLocationRelativeTo(parentFrame);
    }

    private void initGUI() {
        try {
            final GridBagLayout thisLayout = new GridBagLayout();
            getContentPane().setLayout(thisLayout);
            {
                final ComboBoxModel cbBoardsModel = new DefaultComboBoxModel();
                cbBoards = new JComboBox();
                getContentPane().add(cbBoards, new GridBagConstraints(1, 0, 1, 1, 0.4, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
                cbBoards.setModel(cbBoardsModel);
                cbBoards.addActionListener(new ActionListener() {
                    public void actionPerformed(final ActionEvent evt) {
                        cbBoardsActionPerformed(evt);
                    }
                });
            }
            {
                final ComboBoxModel cbDatesModel = new DefaultComboBoxModel();
                cbDates = new JComboBox();
                getContentPane().add(cbDates, new GridBagConstraints(3, 0, 1, 1, 0.4, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
                cbDates.setModel(cbDatesModel);
                cbDates.addActionListener(new ActionListener() {
                    public void actionPerformed(final ActionEvent evt) {
                        cbDatesActionPerformed(evt);
                    }
                });
            }
            {
                lBoards = new JLabel();
                getContentPane().add(lBoards, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
                lBoards.setText("Board");
            }
            {
                lDates = new JLabel();
                getContentPane().add(lDates, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
                lDates.setText("Date");
            }
            {
                taContent = new JTextArea();
                taContent.setEditable(false);
                getContentPane().add(taContent, new GridBagConstraints(0, 1, 4, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));
            }
            pack();
            setSize(400, 400);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void clearTaContent() {
        taContent.setText("No informations available");
    }

    private void cbBoardsActionPerformed(final ActionEvent evt) {
        final JComboBox cb = (JComboBox)evt.getSource();
        final Board selectedBoard = (Board)cb.getSelectedItem();
        if( selectedBoard == null ) {
            clearTaContent();
            return;
        }

        cbDates.removeAllItems();

        final List<BoardUpdateInformation> l = selectedBoard.getBoardUpdateInformationList();
        for( final BoardUpdateInformation bui : l ) {
            cbDates.addItem(bui);
        }
        if( cbDates.getModel().getSize() > 0 ) {
            cbDates.setSelectedIndex(0);
        } else {
            clearTaContent();
        }
    }

    private void cbDatesActionPerformed(final ActionEvent evt) {
        final JComboBox cb = (JComboBox)evt.getSource();
        final BoardUpdateInformation selectedItem = (BoardUpdateInformation)cb.getSelectedItem();
        if( selectedItem == null ) {
            clearTaContent();
            return;
        }

        taContent.setText( selectedItem.getInfoString() );
    }

    /**
     * Reload dialog after restart.
     */
    private void loadGuiData() {
        // only add boards that have update information
        final Vector<Board> items = new Vector<Board>();
        for( final Board b : tofTreeModel.getAllBoards() ) {
            if( b.hasBoardUpdateInformations() ) {
                items.add(b);
            }
        }
        final ComboBoxModel cbBoardsModel = new DefaultComboBoxModel(items);
        cbBoards.setModel(cbBoardsModel);

        if( cbBoards.getItemCount() > 0 ) {
            // FIXME: by default select board that is current board in board tree!!!
            cbBoards.setSelectedIndex(0);
        }
    }

    public void startDialog() {
        tofTree.getRunningBoardUpdateThreads().addBoardUpdateThreadListener(this);
        setDialogShowing(true);
        loadGuiData();
        setVisible(true);
    }

    private void closeDialog() {
        tofTree.getRunningBoardUpdateThreads().removeBoardUpdateThreadListener(this);
        setDialogShowing(false);
        dispose();
    }

    @Override
    protected void processWindowEvent(final WindowEvent e) {
        if( e.getID() == WindowEvent.WINDOW_CLOSING ) {
            // setDialogShowing( false ); // also done in closeDialog()
            closeDialog();
        }
        super.processWindowEvent(e);
    }

    public static boolean isDialogShowing() {
        return isShowing;
    }

    public static void setDialogShowing(final boolean val) {
        isShowing = val;
    }

    // Implementing the BoardUpdateThreadListener ...

    /**
     * Is called if a Thread is finished.
     */
    public void boardUpdateThreadFinished(final BoardUpdateThread thread) {
   }

   /**
    * Is called if a Thread is started.
    *
    * @see frost.threads.BoardUpdateThreadListener#boardUpdateThreadStarted(frost.threads.BoardUpdateThread)
    */
   public void boardUpdateThreadStarted(final BoardUpdateThread thread) {
   }

   public void boardUpdateInformationChanged(final BoardUpdateThread thread, final BoardUpdateInformation bui) {

       final Board selectedBoard = (Board)cbBoards.getSelectedItem();

       // maybe add new board
       {
           boolean contained = false;
           for(int x=0; x < cbBoards.getItemCount(); x++) {
               if( cbBoards.getItemAt(x) == thread.getTargetBoard() ) {
                   contained = true;
                   break;
               }
           }
           if( !contained ) {
               cbBoards.addItem(thread.getTargetBoard());
               return;
           }
       }

       if( selectedBoard == null ) {
           return;
       }
       if( selectedBoard != thread.getTargetBoard() ) {
           return;
       }

       // maybe add new date
       {
           boolean contained = false;
           for(int x=0; x < cbDates.getItemCount(); x++) {
               if( cbDates.getItemAt(x) == bui ) {
                   contained = true;
                   break;
               }
           }
           if( !contained ) {
               cbDates.addItem(bui);
               return;
           }
       }

       final BoardUpdateInformation selectedBui = (BoardUpdateInformation)cbDates.getSelectedItem();
       if( selectedBui == null ) {
           return;
       }
       if( selectedBui != bui ) {
           return;
       }
       taContent.setText( bui.getInfoString() );
   }
}
