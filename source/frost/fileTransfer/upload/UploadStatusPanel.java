/*
 * Created on 04-ene-2005
 * 
 */
package frost.fileTransfer.upload;

import java.awt.FlowLayout;

import javax.swing.*;

import frost.util.gui.translation.*;

/**
 * @author $Author$
 * @version $Revision$
 */
public class UploadStatusPanel extends JPanel {

	/**
	 * 
	 */
	private class Listener implements UploadTickerListener, LanguageListener {
		/* (non-Javadoc)
		 * @see frost.fileTransfer.upload.UploadTickerListener#uploadingCountChanged()
		 */
		public void uploadingCountChanged() {
			numberChanged();
		}

		/* (non-Javadoc)
		 * @see frost.fileTransfer.upload.UploadTickerListener#generatingCountChanged()
		 */
		public void generatingCountChanged() {
			numberChanged();			
		}

		/* (non-Javadoc)
		 * @see frost.util.gui.translation.LanguageListener#languageChanged(frost.util.gui.translation.LanguageEvent)
		 */
		public void languageChanged(LanguageEvent event) {
			refreshLanguage();			
		}
	}
	
	private UploadTicker ticker;
	private Language language;
	
	private JLabel uploadingLabel = new JLabel();
	private JLabel countLabel = new JLabel();
	private JLabel filesLabel = new JLabel();
	
	int count = 0;
	
	private Listener listener = new Listener();
	
	/**
	 * 
	 */
	public UploadStatusPanel(UploadTicker ticker) {
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
		count = ticker.getRunningUploadingThreads();
		countLabel.setText("" + count);
		
		// Add components
		add(uploadingLabel);
		add(countLabel);
		add(filesLabel);
		
		// Add listeners
		ticker.addUploadTickerListener(listener);
		language.addLanguageListener(listener);
	}
	
	/**
	 * 
	 */
	private void refreshLanguage() {
		uploadingLabel.setText(language.getString("Uploading") + ":");
		if (count == 1) {
			filesLabel.setText(language.getString("File").toLowerCase());
		} else {
			filesLabel.setText(language.getString("Files").toLowerCase());
		}
	}
	
	/**
	 * 
	 */
	private void numberChanged() {
		count = ticker.getRunningUploadingThreads();
		countLabel.setText("" + count);
		refreshLanguage();
	}
}
