/*
 HelloWorldPanel.java / Frost
 Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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
 
package frostplugins.SimpleSiteTool;
import java.awt.GridLayout;
import javax.swing.JLabel;

import javax.swing.JPanel;

import frost.pluginmanager.PluginRespinator;
import frost.plugins.FrostPlugin;

/**
 * @author saces
 *
 */
public class SimpleSiteTool extends JPanel implements FrostPlugin {

	private static final long serialVersionUID = 1L;
	
	private JLabel jLabel1;

	public SimpleSiteTool() {
		super();
	}
	
	private void initGUI() {
		try {
			{
				GridLayout thisLayout = new GridLayout(1, 1);
				thisLayout.setHgap(5);
				thisLayout.setVgap(5);
				thisLayout.setColumns(1);
				this.setLayout(thisLayout);
				this.setPreferredSize(new java.awt.Dimension(332, 173));
				{
					jLabel1 = new JLabel();
					GridLayout jLabel1Layout = new GridLayout(1, 1);
					jLabel1Layout.setHgap(5);
					jLabel1Layout.setVgap(5);
					jLabel1Layout.setColumns(1);
					jLabel1.setLayout(jLabel1Layout);
					this.add(jLabel1);
					jLabel1.setText("Hello, World!");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean canStopPlugin() {
		return true;
	}

	public JPanel getPluginPanel() {
		return this;
	}

	public void startPlugin(PluginRespinator pr) {
		initGUI();
	}

	public void stopPlugin() {
	}

}
