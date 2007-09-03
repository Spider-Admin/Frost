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
package frostplugins.HelloWorld;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import frost.pluginmanager.PluginRespinator;
import frost.plugins.FrostPlugin;

/**
 * @author saces
 * 
 */
public class HelloWorld extends JPanel implements FrostPlugin {

	private static final long serialVersionUID = 1L;

	private PluginRespinator m_pr;
	private JLabel jLabel1;
	private JButton jButton1;
	private JButton jButton2;
	private JScrollPane jScrollPane1;
	private JTextArea jTextArea1;

	public HelloWorld() {
		super();
	}

	private void initGUI() {
		try {

			GridBagLayout thisLayout = new GridBagLayout();
			thisLayout.rowWeights = new double[] { 0.1, 0.1, 0.1 };
			thisLayout.rowHeights = new int[] { 7, 7, 7 };
			thisLayout.columnWeights = new double[] { 0.1, 1.0 };
			thisLayout.columnWidths = new int[] { 7, 7 };
			this.setLayout(thisLayout);
			this.setPreferredSize(new java.awt.Dimension(332, 173));

			jLabel1 = new JLabel();
			GridLayout jLabel1Layout = new GridLayout(1, 1);
			jLabel1Layout.setHgap(5);
			jLabel1Layout.setVgap(5);
			jLabel1Layout.setColumns(1);
			jLabel1.setLayout(jLabel1Layout);
			this.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0), 0, 0));
			jLabel1.setText("Hello, World!");

			// START >> jButton1
			jButton1 = new JButton();
			this.add(jButton1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0), 0, 0));
			jButton1.setText("plugins.txt");
			jButton1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					loadText("plugins.txt");
				}
			});
			// END << jButton1
			// START >> jButton2
			jButton2 = new JButton();
			this.add(jButton2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0), 0, 0));
			jButton2.setText("HelloWorld.java");
			jButton2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					loadText("HelloWorld.java");
				}
			});
			// END << jButton2
			// START >> jScrollPane1
			jScrollPane1 = new JScrollPane();
			this.add(jScrollPane1, new GridBagConstraints(1, 0, 1, 3, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 5, 5, 5), 0, 0));
			// START >> jTextArea1
			jTextArea1 = new JTextArea();
			jScrollPane1.setViewportView(jTextArea1);
			jTextArea1.setTabSize(4);
			jTextArea1.setText("Text?\n\tPress the button!");
			jTextArea1.setEditable(false);
			jTextArea1.setFont(new Font("monospaced", Font.PLAIN, 12));
			// END << jTextArea1
			// END << jScrollPane1

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String copyFromResource(String resource) throws IOException {
		InputStream input = HelloWorld.class.getResourceAsStream(resource);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] data = new byte[4096];
		int bytesRead;
		while ((bytesRead = input.read(data)) != -1) {
			output.write(data, 0, bytesRead);
		}
		input.close();
		output.close();

		return new String(output.toString());
	}

	protected void loadText(String string) {
		String text = null;

		try {
			text = copyFromResource(string);
		} catch (IOException e) {

			text = e.getMessage();
		}

		jTextArea1.setText(text);
		jTextArea1.setCaretPosition(0);
	}

	public boolean canStopPlugin() {
		return true;
	}

	public JPanel getPluginPanel() {
		return this;
	}

	public void startPlugin(PluginRespinator pr) {
		m_pr = pr;
		initGUI();
	}

	public void stopPlugin() {

	}
}
