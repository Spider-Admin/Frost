/*
 * Created on 07-ene-2005
 * 
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

	/**
	 * 
	 */
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
	
	/**
	 * 
	 */
	public DownloadStatusPanel(DownloadTicker ticker) {
		super();
		this.ticker = ticker;
		language = Language.getInstance();
		initialize();
	}

	/**
	 * 
	 */
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
	
	/**
	 * 
	 */
	private void refreshLanguage() {
		downloadingLabel.setText(language.getString("DownloadStatusPanel.Downloading"));
		if (count == 1) {
			filesLabel.setText(language.getString("StatusPanel.file"));
		} else {
			filesLabel.setText(language.getString("StatusPanel.files"));
		}
	}
	
	/**
	 * 
	 */
	private void numberChanged() {
		count = ticker.getRunningThreads();
		countLabel.setText("" + count);
		refreshLanguage();
	}
}
