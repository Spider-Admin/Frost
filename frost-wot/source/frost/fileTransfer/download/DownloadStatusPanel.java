/*
  DownloadStatusPanel.java / Frost

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
package frost.fileTransfer.download;

import java.awt.FlowLayout;

import javax.swing.*;

import frost.util.gui.translation.*;

/**
 * @author $Author$
 * @version $Revision$
 */
public class DownloadStatusPanel extends JPanel {

	private class Listener implements DownloadTickerListener, LanguageListener {
		/* (non-Javadoc)
		 * @see frost.fileTransfer.download.DownloadTickerListener#threadCountChanged()
		 */
		public void threadCountChanged() {
			numberChanged();
		}

		/* (non-Javadoc)
		 * @see frost.util.gui.translation.LanguageListener#languageChanged(frost.util.gui.translation.LanguageEvent)
		 */
		public void languageChanged(LanguageEvent event) {
			refreshLanguage();			
		}
	}
	
	private DownloadTicker ticker;
	private Language language;
	
	private JLabel downloadingLabel = new JLabel();
	private JLabel countLabel = new JLabel();
	private JLabel filesLabel = new JLabel();
	
	int count = 0;
	
	private Listener listener = new Listener();
	
	public DownloadStatusPanel(DownloadTicker ticker) {
		super();
		this.ticker = ticker;
		language = Language.getInstance();
		initialize();
	}

	private void initialize() {
		refreshLanguage();
		setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		
		// Init count
		count = ticker.getRunningThreads();
		countLabel.setText("" + count);
		
		// Add components
		add(downloadingLabel);
		add(countLabel);
		add(filesLabel);
		
		// Add listeners
		ticker.addDownloadTickerListener(listener);
		language.addLanguageListener(listener);
	}
	
	private void refreshLanguage() {
		downloadingLabel.setText(language.getString("MainFrame.statusBar.downloading")+":");
		if (count == 1) {
			filesLabel.setText(language.getString("MainFrame.statusBar.file"));
		} else {
			filesLabel.setText(language.getString("MainFrame.statusBar.files"));
		}
	}
	
	private void numberChanged() {
		count = ticker.getRunningThreads();
		countLabel.setText("" + count);
		refreshLanguage();
	}
}
