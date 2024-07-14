/*
  SelectRowOnRightClick.java / Frost
  Copyright (C) 2024 Frost Project <jtcfrost.sourceforge.net>

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
package frost.util.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

public class SelectRowOnRightClick extends MouseAdapter {

	private JTable table;

	public SelectRowOnRightClick(JTable table) {
		this.table = table;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		Integer row = table.rowAtPoint(e.getPoint());
		if (row < 0) {
			return;
		}
		if (!table.getSelectionModel().isSelectedIndex(row)) {
			table.getSelectionModel().setSelectionInterval(row, row);
		}
	}
}
