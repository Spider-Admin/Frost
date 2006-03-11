/*
  BoardsChooser.java / Frost
  Copyright (C) 2006  Jan-Thomas Czornack <jantho@users.sourceforge.net>
  This file is contributed by Stefan Majewski <feuerblume@users.sourceforge.net>

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
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;

import frost.gui.objects.*;
import frost.util.gui.translation.*;

public class BoardsChooser extends JDialog {

    private Language language = Language.getInstance();

    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    
    protected class BoardsCellRenderer implements ListCellRenderer {

       public Component getListCellRendererComponent(
                     JList list, Object value, int index,
                     boolean isSelected, boolean cellHasFocus)
       {
          BoardListEntry e = (BoardListEntry)value;
          
          JCheckBox checkbox = e.checkBox;
          
          checkbox.setBackground(isSelected ? Lboards.getSelectionBackground() : Lboards.getBackground());
          checkbox.setForeground(isSelected ? Lboards.getSelectionForeground() : Lboards.getForeground());
          checkbox.setEnabled(isEnabled());
          checkbox.setFont(getFont());
          checkbox.setFocusPainted(false);
          checkbox.setBorderPainted(true);
          checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);

          return checkbox;
       }
    }
    
    JButton Bcancel;
    List boardList;
    JButton Bok;
    JList Lboards;
    boolean okPressed = false;

    public BoardsChooser(List boards, List preselectedBoards) {
        super();
        setTitle(language.getString("Choose boards"));
        setModal(true);
        
        // fill given board into our list as BoardListEntries
        boardList = new ArrayList();
        for(Iterator i=boards.iterator(); i.hasNext(); ) {
            Board b = (Board)i.next();
            BoardListEntry e = new BoardListEntry();
            e.board = b;
            e.checkBox = new JCheckBox(b.getName());
            e.checkBox.setSelected(false);
            
            if( preselectedBoards != null ) {
                // check if this board should be selected
                for(Iterator j=preselectedBoards.iterator(); j.hasNext(); ) {
                    Board sb = (Board)j.next();
                    if( b.getName().equals(sb.getName()) ) {
                        e.checkBox.setSelected(true);
                    }
                }
            }
            
            boardList.add(e);
        }
        
        initGui();
    }
    
    public BoardsChooser(List boards) {
        this(boards, null);
    }
    
    private void initGui() {
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
                return boardList.size();
            }
            public Object getElementAt(int index) {
                return boardList.get(index);
            }
        };
        Lboards = new JList(boardsModel);
        Lboards.setCellRenderer(new BoardsCellRenderer());
        Lboards.addMouseListener(new MouseAdapter() {
           public void mousePressed(MouseEvent e) {
              int index = Lboards.locationToIndex(e.getPoint());
              if (index != -1) {
                  BoardListEntry ent = (BoardListEntry)Lboards.getModel().getElementAt(index);
                  JCheckBox checkbox = ent.checkBox;
                  checkbox.setSelected(!checkbox.isSelected());
                  repaint();
              }
           }
        } );
        Lboards.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroller = new JScrollPane( Lboards );
        listScroller.setBorder( new CompoundBorder( new EmptyBorder(5,5,5,5),
                                                    new CompoundBorder( new EtchedBorder(),
                                                                        new EmptyBorder(5,5,5,5) )
                                                  ) );
        getContentPane().add(listScroller, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
        setSize(300, 400);
    }
    
    public List runDialog()
    {
        setVisible(true);
        if( okPressed == false )
            return null;

        ArrayList chosed = new ArrayList();
        for(Iterator i=boardList.iterator(); i.hasNext(); ) {
            BoardListEntry e = (BoardListEntry)i.next();
            if( e.checkBox.isSelected() ) {
                chosed.add(e.board);
            }
        }
        return chosed;
    }

    private class BoardListEntry {
        Board board = null;
        JCheckBox checkBox = null;
    }
}
