package frost.components.translate;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import frost.Core;
import frost.SettingsClass;
import frost.util.gui.MiscToolkit;
import frost.util.gui.translation.FrostResourceBundleReader;
import frost.util.gui.translation.Language;

public class TranslationDialog extends JDialog {
	
	private static Logger logger = Logger.getLogger(SettingsClass.class.getName());
	
	private Language frostlanguage = Language.getInstance();
//	private Language language = new Language("de");
	private static final String BUNDLE_NAME = "/i18n/langres"; // base name in jar file
	private TreeMap rootbundle;
	private TreeMap userbundle;
	private boolean bundleChanged = false;
	private String editlocale;
	
	private JPanel jContentPane = null;
	private JLabel jLabel = null;
	private JTabbedPane jTabbedPane = null;
	private JPopupMenu popMenu = null;  //  @jve:decl-index=0:visual-constraint="52,235"
	private JPanel jPanel = null;
	private JPanel jPanel1 = null;
	private MiscToolkit miscToolkit = MiscToolkit.getInstance();
	private JRadioButtonMenuItem languageBulgarianMenuItem = null;
	private JRadioButtonMenuItem languageDefaultMenuItem = null;
	private JRadioButtonMenuItem languageFrenchMenuItem = null;
	private JRadioButtonMenuItem languageEnglishMenuItem = null;
	private JRadioButtonMenuItem languageDutchMenuItem = null;
	private JRadioButtonMenuItem languageGermanMenuItem = null;
	private JRadioButtonMenuItem languageItalianMenuItem = null;
	private JRadioButtonMenuItem languageJapaneseMenuItem = null;
	private JRadioButtonMenuItem languageRussianMenuItem = null;
	private JRadioButtonMenuItem languageSpanishMenuItem = null;
	private JPanel jPanel2 = null;
	private JPanel jPanel3 = null;
	private JCheckBox CB_FilterNone = null;
	private JCheckBox CB_FilterAll = null;
	private JCheckBox CB_FilterMissing = null;
	private JCheckBox CB_CL_Translation = null;
	private JCheckBox jCheckBox5 = null;
	private JCheckBox CB_CL_Frost = null;
	private JSplitPane jSplitPane = null;
	private JPanel jPanel4 = null;
	private JPanel jPanel5 = null;
	private JScrollPane jScrollPane = null;		//  @jve:decl-index=0:visual-constraint=""
	private JList jList1 = null;
	  //  @jve:decl-index=0:visual-constraint=""
	public TranslationDialog() throws HeadlessException {
		super();
		// TODO Auto-generated constructor stub
		initialize();
	}

	public TranslationDialog(Frame arg0) throws HeadlessException {
		super(arg0);
		// TODO Auto-generated constructor stub
		initialize();
	}

	public TranslationDialog(Frame arg0, boolean arg1) throws HeadlessException {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
		initialize();
	}

	public TranslationDialog(Frame arg0, String arg1) throws HeadlessException {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
		initialize();
	}

	public TranslationDialog(Frame arg0, String arg1, boolean arg2)
			throws HeadlessException {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
		initialize();
	}

	public TranslationDialog(Frame arg0, String arg1, boolean arg2,
			GraphicsConfiguration arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
		initialize();
	}

	public TranslationDialog(Dialog arg0) throws HeadlessException {
		super(arg0);
		// TODO Auto-generated constructor stub
		initialize();
	}

	public TranslationDialog(Dialog arg0, boolean arg1)
			throws HeadlessException {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
		initialize();
	}

	public TranslationDialog(Dialog arg0, String arg1) throws HeadlessException {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
		initialize();
	}

	public TranslationDialog(Dialog arg0, String arg1, boolean arg2)
			throws HeadlessException {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
		initialize();
	}

	public TranslationDialog(Dialog arg0, String arg1, boolean arg2,
			GraphicsConfiguration arg3) throws HeadlessException {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
		initialize();
	}
	
	
	
	private class ItemListModel extends AbstractListModel {
		
		
		
		public ItemListModel() {
			super();
		}

		/* (non-Javadoc)
		 * @see javax.swing.ListModel#getSize()
		 */
		public int getSize() {
			// TODO Auto-generated method stub
			//return 6;
			return rootbundle.size();
		}

		/* (non-Javadoc)
		 * @see javax.swing.ListModel#getElementAt(int)
		 */
		public Object getElementAt(int arg0) {
			// TODO Auto-generated method stub
			return rootbundle.keySet().toArray()[arg0];
			
			//return "null";
		}

		/* (non-Javadoc)
		 * @see javax.swing.ListModel#addListDataListener(javax.swing.event.ListDataListener)
		 */
		//public void addListDataListener(ListDataListener arg0) {
			// TODO Auto-generated method stub

		//}

		/* (non-Javadoc)
		 * @see javax.swing.ListModel#removeListDataListener(javax.swing.event.ListDataListener)
		 */
		//public void removeListDataListener(ListDataListener arg0) {
			// TODO Auto-generated method stub

		//}

	}
	
	private ItemListModel itemListModel = null;
	private JTextArea jTextArea1 = null;
	private JTextArea jTextArea2 = null;
	private JTextArea jTextArea3 = null;
	private JScrollPane jScrollPane1 = null;
	private JScrollPane jScrollPane2 = null;
	private JScrollPane jScrollPane3 = null;
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		String resource = BUNDLE_NAME+".properties";
		rootbundle = new TreeMap(FrostResourceBundleReader.loadBundle(resource));
		userbundle = new TreeMap();
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		this.setBounds(new java.awt.Rectangle(0,0,470,210));
		this.setTitle("Frost übelsetzen");
		this.setContentPane(getJContentPane());
	}
	
	private String getUserBundleItem(String key) {
		String result;
		result = (String)userbundle.get(key);
		if (result == null) {
			result = "";
		}
		return result;
	}
	
	private void LoadBundle(String l) {
		// TODO Userbundle von file laden
		String ulfdir = Core.frostSettings.getValue(SettingsClass.TRANSLATION_USERDIR);
		File ulf = new File(ulfdir + "langres_" + l + ".properties", "UTF-8");
		if (!ulf.exists()) {
			return;
		}
		editlocale = l;
	}
	
	private void SaveBundle() {
		// TODO zum testerln:
		editlocale = "de";
		System.out.println("SaveBundle() called");
		if (!bundleChanged) { 
			System.out.println("SaveBundle: Bundel not changed");
			return; 
		}
		String ulfdir = Core.frostSettings.getValue(SettingsClass.TRANSLATION_USERDIR);
		File ulf = new File(ulfdir + "langres_" + editlocale + ".properties");
		try {
			ulf.createNewFile();
		} catch (IOException exception) {
			
		}
		// userlocaledir + "langres_" + localeName + ".properties");
		Core.frostSettings.getValue(SettingsClass.TRANSLATION_USERDIR);
		
		
		PrintWriter bundleWriter = null;
        try {
            bundleWriter = new PrintWriter(new FileWriter(ulf));
        } catch (IOException exception) {
            try {
                //Perhaps the problem is that the ulf dir doesn't exist? In that case, we create it and try again
                File ulfDir = new File(ulfdir);
                if (!ulfDir.isDirectory()) {
                    ulfDir.mkdirs(); // if the ulf dir doesn't exist, we create it
                }
                bundleWriter = new PrintWriter(new FileWriter(ulf));
            } catch (IOException exception2) {
                logger.log(Level.SEVERE, "Exception thrown in writeUserBundle()", exception2);
                return;
            }
        }
        
        Iterator i = userbundle.keySet().iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            String val = userbundle.get(key).toString();
            bundleWriter.println(key + "=" + val);
        }
        
        try {
            bundleWriter.close();
            logger.info("Wrote configuration");
            return;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown in writeUserBundle", e);
        }
		//userbundle.toString();
		bundleChanged = false;
	}
	 
	

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel = new JLabel();
			jLabel.setText("Hint: rechte Maustaste für Sprachmenü");
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
			jContentPane.add(jLabel, java.awt.BorderLayout.SOUTH);
			jContentPane.add(getJTabbedPane(), java.awt.BorderLayout.CENTER);
			jContentPane.addMouseListener(new java.awt.event.MouseListener() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
				}
				public void mousePressed(java.awt.event.MouseEvent e) {
					if (e.isPopupTrigger()) {
						//getPopMenu().show(e.getComponent(), e.getX(), e.getY());
					}
				}
				public void mouseReleased(java.awt.event.MouseEvent e) {
					if (e.isPopupTrigger()) {
						//getPopMenu().show(e.getComponent(), e.getX(), e.getY());
					}
				}
				public void mouseEntered(java.awt.event.MouseEvent e) {
				}
				public void mouseExited(java.awt.event.MouseEvent e) {
				}
			});
		}
		return jContentPane;
	}

	/**
	 * This method initializes jTabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			jTabbedPane.addTab("Einstellungen", null, getJPanel(), null);
			jTabbedPane.addTab("Bearbeiten", null, getJPanel1(), null);
			jTabbedPane.addMouseListener(new java.awt.event.MouseListener() {
				public void mouseReleased(java.awt.event.MouseEvent e) {
					if (e.isPopupTrigger()) {
						//getPopMenu().show(e.getComponent(), e.getX(), e.getY());
					}
				}
				public void mouseClicked(java.awt.event.MouseEvent e) {
				}
				public void mousePressed(java.awt.event.MouseEvent e) {
					if (e.isPopupTrigger()) {
						//getPopMenu().show(e.getComponent(), e.getX(), e.getY());
					}
				}
				public void mouseEntered(java.awt.event.MouseEvent e) {
				}
				public void mouseExited(java.awt.event.MouseEvent e) {
				}
			});
			jTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					System.out.println("bla  stateChanged()"); // TODO Auto-generated Event stub stateChanged()
				}
			});
		}
		return jTabbedPane;
	}

	/**
	 * This method initializes jPopupMenu	
	 * 	
	 * @return javax.swing.JPopupMenu	
	 */
	private JPopupMenu getPopMenu() {
		if (popMenu == null) {
			popMenu = new JPopupMenu();
			popMenu.add(getLanguageDefaultMenuItem());
			popMenu.addSeparator();
			popMenu.add(getLanguageBulgarianMenuItem());
			popMenu.add(getLanguageDutchMenuItem());
			popMenu.add(getLanguageEnglishMenuItem());
			popMenu.add(getLanguageFrenchMenuItem());
			popMenu.add(getLanguageGermanMenuItem());
			popMenu.add(getLanguageItalianMenuItem());
			popMenu.add(getLanguageJapaneseMenuItem());
			popMenu.add(getLanguageSpanishMenuItem());
			popMenu.add(getLanguageRussianMenuItem());
			ButtonGroup languageMenuButtonGroup = new ButtonGroup();
            languageDefaultMenuItem.setSelected(true);
            languageMenuButtonGroup.add(languageDefaultMenuItem);
            languageMenuButtonGroup.add(languageBulgarianMenuItem);
            languageMenuButtonGroup.add(languageDutchMenuItem);
            languageMenuButtonGroup.add(languageEnglishMenuItem);
            languageMenuButtonGroup.add(languageFrenchMenuItem);
            languageMenuButtonGroup.add(languageGermanMenuItem);
            languageMenuButtonGroup.add(languageItalianMenuItem);
            languageMenuButtonGroup.add(languageJapaneseMenuItem);
            languageMenuButtonGroup.add(languageRussianMenuItem);
            languageMenuButtonGroup.add(languageSpanishMenuItem);
		}
		return popMenu;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints1.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridheight = 1;
			gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints.gridy = 0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
			jPanel.add(getJPanel2(), gridBagConstraints);
			jPanel.add(getJPanel3(), gridBagConstraints1);
			jPanel.addMouseListener(new java.awt.event.MouseListener() {   
				public void mouseReleased(java.awt.event.MouseEvent e) {    
					//System.out.println("mouseReleased()"); // TODO Auto-generated Event stub mouseReleased()
					if (e.isPopupTrigger()) {
						getPopMenu().show(e.getComponent(), e.getX(), e.getY());
					}
				}
				public void mousePressed(java.awt.event.MouseEvent e) {
					//System.out.println("mousePressed()"); // TODO Auto-generated Event stub mousePressed()
					if (e.isPopupTrigger()) {
						getPopMenu().show(e.getComponent(), e.getX(), e.getY());
					}
				}
				public void mouseClicked(java.awt.event.MouseEvent e) {
				}
			
				public void mouseEntered(java.awt.event.MouseEvent e) {
				}
				public void mouseExited(java.awt.event.MouseEvent e) {
				}
			});
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setLayout(new CardLayout());
			jPanel1.add(getJSplitPane(), getJSplitPane().getName());
			jPanel1.addComponentListener(new java.awt.event.ComponentListener() {
				public void componentShown(java.awt.event.ComponentEvent e) {
					System.out.println(" JP componentShown()");
				}
				public void componentResized(java.awt.event.ComponentEvent e) {
				}
				public void componentMoved(java.awt.event.ComponentEvent e) {
				}
				public void componentHidden(java.awt.event.ComponentEvent e) {
					//System.out.println(" JP componentHidden()");
					SaveBundle();
				}
			});
		}
		return jPanel1;
	}

	/**
	 * This method initializes jRadioButtonMenuItem	
	 * 	
	 * @return javax.swing.JRadioButtonMenuItem	
	 */
	private JRadioButtonMenuItem getLanguageBulgarianMenuItem() {
		if (languageBulgarianMenuItem == null) {
			languageBulgarianMenuItem = new JRadioButtonMenuItem();
			languageBulgarianMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_bg.png", 16, 16));
			languageBulgarianMenuItem.setText("Bulgarian");
			languageBulgarianMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setLanguage("bg");
				}
			});
		}
		return languageBulgarianMenuItem;
	}

	/**
	 * This method initializes jRadioButtonMenuItem1	
	 * 	
	 * @return javax.swing.JRadioButtonMenuItem	
	 */
	private JRadioButtonMenuItem getLanguageDefaultMenuItem() {
		if (languageDefaultMenuItem == null) {
			languageDefaultMenuItem = new JRadioButtonMenuItem();
			languageDefaultMenuItem.setSelected(true);
			languageDefaultMenuItem.setText("Default");
			languageDefaultMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setLanguage(null);
				}
			});
		}
		return languageDefaultMenuItem;
	}

	/**
	 * This method initializes jRadioButtonMenuItem2	
	 * 	
	 * @return javax.swing.JRadioButtonMenuItem	
	 */
	private JRadioButtonMenuItem getLanguageFrenchMenuItem() {
		if (languageFrenchMenuItem == null) {
			languageFrenchMenuItem = new JRadioButtonMenuItem();
			languageFrenchMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_fr.png", 16, 16));
			languageFrenchMenuItem.setText("French");
			languageFrenchMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setLanguage("fr");
				}
			});
		}
		return languageFrenchMenuItem;
	}

	/**
	 * This method initializes jRadioButtonMenuItem3	
	 * 	
	 * @return javax.swing.JRadioButtonMenuItem	
	 */
	private JRadioButtonMenuItem getLanguageEnglishMenuItem() {
		if (languageEnglishMenuItem == null) {
			languageEnglishMenuItem = new JRadioButtonMenuItem();
			languageEnglishMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_en.png", 16, 16));
			languageEnglishMenuItem.setText("English");
			languageEnglishMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setLanguage("en");
				}
			});
		}
		return languageEnglishMenuItem;
	}

	/**
	 * This method initializes jRadioButtonMenuItem4	
	 * 	
	 * @return javax.swing.JRadioButtonMenuItem	
	 */
	private JRadioButtonMenuItem getLanguageDutchMenuItem() {
		if (languageDutchMenuItem == null) {
			languageDutchMenuItem = new JRadioButtonMenuItem();
			languageDutchMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_nl.png", 16, 16));
			languageDutchMenuItem.setText("Dutch");
			languageDutchMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setLanguage("nl");
				}
			});
		}
		return languageDutchMenuItem;
	}

	/**
	 * This method initializes jRadioButtonMenuItem	
	 * 	
	 * @return javax.swing.JRadioButtonMenuItem	
	 */
	private JRadioButtonMenuItem getLanguageGermanMenuItem() {
		if (languageGermanMenuItem == null) {
			languageGermanMenuItem = new JRadioButtonMenuItem();
			languageGermanMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_de.png", 16, 16));
			languageGermanMenuItem.setText("German");
			languageGermanMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setLanguage("de");
				}
			});
		}
		return languageGermanMenuItem;
	}

	/**
	 * This method initializes jRadioButtonMenuItem1	
	 * 	
	 * @return javax.swing.JRadioButtonMenuItem	
	 */
	private JRadioButtonMenuItem getLanguageItalianMenuItem() {
		if (languageItalianMenuItem == null) {
			languageItalianMenuItem = new JRadioButtonMenuItem();
			languageItalianMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_it.png", 16, 16));
			languageItalianMenuItem.setText("Italian");
			languageItalianMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setLanguage("it");
				}
			});
		}
		return languageItalianMenuItem;
	}

	/**
	 * This method initializes jRadioButtonMenuItem	
	 * 	
	 * @return javax.swing.JRadioButtonMenuItem	
	 */
	private JRadioButtonMenuItem getLanguageJapaneseMenuItem() {
		if (languageJapaneseMenuItem == null) {
			languageJapaneseMenuItem = new JRadioButtonMenuItem();
			languageJapaneseMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_jp.png", 16, 16));
			languageJapaneseMenuItem.setText("Japanese");
			languageJapaneseMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setLanguage("jp");
				}
			});
		}
		return languageJapaneseMenuItem;
	}

	/**
	 * This method initializes jRadioButtonMenuItem1	
	 * 	
	 * @return javax.swing.JRadioButtonMenuItem	
	 */
	private JRadioButtonMenuItem getLanguageRussianMenuItem() {
		if (languageRussianMenuItem == null) {
			languageRussianMenuItem = new JRadioButtonMenuItem();
			languageRussianMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_ru.png", 16, 16));
			languageRussianMenuItem.setText("Russian");
			languageRussianMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setLanguage("ru");
				}
			});
		}
		return languageRussianMenuItem;
	}

	/**
	 * This method initializes jRadioButtonMenuItem	
	 * 	
	 * @return javax.swing.JRadioButtonMenuItem	
	 */
	private JRadioButtonMenuItem getLanguageSpanishMenuItem() {
		if (languageSpanishMenuItem == null) {
			languageSpanishMenuItem = new JRadioButtonMenuItem();
			languageSpanishMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_es.png", 16, 16));
			languageSpanishMenuItem.setText("Spanish");
			languageSpanishMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setLanguage("es");
				}
			});
		}
		return languageSpanishMenuItem;
	}

	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.setLayout(new BoxLayout(getJPanel2(), BoxLayout.Y_AXIS));
			jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Sprache ändern...", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12), new java.awt.Color(51,51,51)));
			jPanel2.add(getCB_CL_Translation(), null);
			jPanel2.add(getJCheckBox5(), null);
			jPanel2.add(getCB_CL_Frost(), null);
		}
		return jPanel2;
	}

	/**
	 * This method initializes jPanel3	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			jPanel3 = new JPanel();
			jPanel3.setLayout(new BoxLayout(getJPanel3(), BoxLayout.Y_AXIS));
			jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Filter", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
			jPanel3.add(getCB_FilterNone(), null);
			jPanel3.add(getCB_FilterAll(), null);
			jPanel3.add(getCB_FilterMissing(), null);
		}
		return jPanel3;
	}

	/**
	 * This method initializes jCheckBox1	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getCB_FilterNone() {
		if (CB_FilterNone == null) {
			CB_FilterNone = new JCheckBox();
			CB_FilterNone.setText("Kein Filter");
			CB_FilterNone.setEnabled(false);
			CB_FilterNone.setSelected(true);
			CB_FilterNone.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if (CB_FilterNone.isSelected()) {
						getCB_FilterAll().setEnabled(false);
						getCB_FilterMissing().setEnabled(false);
					} else {
						getCB_FilterAll().setEnabled(true);
						getCB_FilterMissing().setEnabled(true);
					}
					
				//	jPanel3.add(getCB_FilterNone(), null);
				//	jPanel3.add(getCB_FilterAll(), null);
				//	jPanel3.add(getCB_FilterMissing(), null);
				}
			});
		}
		return CB_FilterNone;
	}

	/**
	 * This method initializes jCheckBox2	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getCB_FilterAll() {
		if (CB_FilterAll == null) {
			CB_FilterAll = new JCheckBox();
			CB_FilterAll.setText("Alle");
			CB_FilterAll.setEnabled(false);
			CB_FilterAll.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					System.out.println("itemStateChanged()"); // TODO Auto-generated Event stub itemStateChanged()
				}
			});
		}
		return CB_FilterAll;
	}

	/**
	 * This method initializes jCheckBox3	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getCB_FilterMissing() {
		if (CB_FilterMissing == null) {
			CB_FilterMissing = new JCheckBox();
			CB_FilterMissing.setText("Fehlende");
			CB_FilterMissing.setEnabled(false);
			CB_FilterMissing.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					System.out.println("itemStateChanged()"); // TODO Auto-generated Event stub itemStateChanged()
				}
			});
		}
		return CB_FilterMissing;
	}

	/**
	 * This method initializes jCheckBox4	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getCB_CL_Translation() {
		if (CB_CL_Translation == null) {
			CB_CL_Translation = new JCheckBox();
			CB_CL_Translation.setText("zu übersetzende Sprache");
			CB_CL_Translation.setSelected(true);
		}
		return CB_CL_Translation;
	}

	/**
	 * This method initializes jCheckBox5	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBox5() {
		if (jCheckBox5 == null) {
			jCheckBox5 = new JCheckBox();
			jCheckBox5.setText("Übersetzungsdialog");
			jCheckBox5.setEnabled(false);
			jCheckBox5.setSelected(false);
		}
		return jCheckBox5;
	}

	/**
	 * This method initializes jCheckBox6	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getCB_CL_Frost() {
		if (CB_CL_Frost == null) {
			CB_CL_Frost = new JCheckBox();
			CB_CL_Frost.setText("Frost");
		}
		return CB_CL_Frost;
	}

	/**
     * Setter for thelanguage resource bundle
     * @param newLanguageResource
     */
    private void setLanguage(String newLang) {
    	if( newLang == null ) {
    		newLang = Core.frostSettings.getValue("locale");
    	}	
    	System.out.println("Tuwas mit Sprache:" + newLang);
    	if (getCB_CL_Frost().isSelected()) {
    		frostlanguage.changeLanguage(newLang);
    	}
    	if (getCB_CL_Translation().isSelected()) {
    		frostlanguage.changeLanguage(newLang);
    	}
     /*   if( newLocaleName == null ) {
            frostSettings.setValue("locale", "default");
        } else {
            frostSettings.setValue("locale", newLocaleName);
        }
        language.changeLanguage(newLocaleName);
        translateMainMenu();
        translateButtons(); */
    }

	/**
	 * This method initializes jSplitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setName("jSplitPane");
			jSplitPane.setDividerLocation(110);
			jSplitPane.setRightComponent(getJPanel5());
			jSplitPane.setLeftComponent(getJPanel4());
		}
		return jSplitPane;
	}

	/**
	 * This method initializes jPanel4	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel4() {
		if (jPanel4 == null) {
			jPanel4 = new JPanel();
			jPanel4.setLayout(new CardLayout());
			jPanel4.add(getJScrollPane(), getJScrollPane().getName());
		}
		return jPanel4;
	}

	/**
	 * This method initializes jPanel5	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel5() {
		if (jPanel5 == null) {
			jPanel5 = new JPanel();
			jPanel5.setLayout(new BoxLayout(getJPanel5(), BoxLayout.Y_AXIS));
			jPanel5.add(getJScrollPane1(), null);
			jPanel5.add(getJScrollPane2(), null);
			jPanel5.add(getJScrollPane3(), null);
		}
		return jPanel5;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setName("jScrollPane");
			jScrollPane.setViewportView(getJList1());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jList1	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getJList1() {
		if (jList1 == null) {
			jList1 = new JList();
			jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
			jList1.setModel(getItemListModel());
			jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
				public void valueChanged(javax.swing.event.ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()) {
						//System.out.println("Adjusting2");
						getJTextArea2().setText((String)rootbundle.get(jList1.getSelectedValue()));
						getJTextArea1().setText(frostlanguage.getString((String)jList1.getSelectedValue()));
						getJTextArea3().setText((String)userbundle.get((String)jList1.getSelectedValue()));
						//String tmpstr = rootbundle.get(jList1.getSelectedValue());
					}
					//System.out.println("selected:" + jList1.getSelectedValue());
					
					//System.out.println("valueChanged()"); // TODO Auto-generated Event stub valueChanged()
				}
			});
		}
		return jList1;
	}

	/**
	 * This method initializes itemListModel	
	 * 	
	 * @return frost.components.translate.ItemListModel	
	 */
	private ItemListModel getItemListModel() {
		if (itemListModel == null) {
			itemListModel = new ItemListModel();
		}
		return itemListModel;
	}

	/**
	 * This method initializes jTextArea1	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getJTextArea1() {
		if (jTextArea1 == null) {
			jTextArea1 = new JTextArea();
			jTextArea1.setEditable(false);
		}
		return jTextArea1;
	}

	/**
	 * This method initializes jTextArea2	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getJTextArea2() {
		if (jTextArea2 == null) {
			jTextArea2 = new JTextArea();
			jTextArea2.setEditable(false);
			jTextArea2.setLineWrap(false);
		}
		return jTextArea2;
	}

	/**
	 * This method initializes jTextArea3	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getJTextArea3() {
		if (jTextArea3 == null) {
			jTextArea3 = new JTextArea();
			jTextArea3.setLineWrap(false);
			jTextArea3.addFocusListener(new java.awt.event.FocusListener() {
				public void focusLost(java.awt.event.FocusEvent e) {
					//userbundle.put(jList1.getSelectedValue(), jTextArea3.getText());
					//System.out.println("focusLost()"); // TODO Auto-generated Event stub focusLost()
				}
				public void focusGained(java.awt.event.FocusEvent e) {
					//System.out.println("focusGained()");
				}
			});
			jTextArea3.addKeyListener(new java.awt.event.KeyListener() {
				public void keyTyped(java.awt.event.KeyEvent e) {
					//System.out.println("keyTyped()"); // TODO Auto-generated Event stub keyTyped()
					userbundle.put(jList1.getSelectedValue(), jTextArea3.getText() + e.getKeyChar());
					bundleChanged = true;
				}
				public void keyPressed(java.awt.event.KeyEvent e) {
				}
				public void keyReleased(java.awt.event.KeyEvent e) {
				}
			});
		}
		return jTextArea3;
	}

	/**
	 * This method initializes jScrollPane1	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Original", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
			jScrollPane1.setViewportView(getJTextArea2());
		}
		return jScrollPane1;
	}

	/**
	 * This method initializes jScrollPane2	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane2() {
		if (jScrollPane2 == null) {
			jScrollPane2 = new JScrollPane();
			jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Übelsetzung (eingemontiert)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
			jScrollPane2.setViewportView(getJTextArea1());
		}
		return jScrollPane2;
	}

	/**
	 * This method initializes jScrollPane3	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane3() {
		if (jScrollPane3 == null) {
			jScrollPane3 = new JScrollPane();
			jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Benützeldefünürte Übelsetzeung", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
			jScrollPane3.setViewportView(getJTextArea3());
		}
		return jScrollPane3;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
