package res;

import java.util.*;

public class LangResMapping {

    public static void main(String[] args) {
        buildMap();
        LangRes lr = new LangRes();
        
        for(Iterator i=mapping.keySet().iterator(); i.hasNext(); ) {
            String k1 = (String)i.next();
            k1 = (String)mapping.get(k1);

            try {
                if( lr.getString(k1) == null ) {
                    System.out.println("Key in mapping, but not in LangRes: '"+k1+"'");
                }
            } catch (RuntimeException e) {
                System.out.println("Key in mapping, but not in LangRes: '"+k1+"'");
            }
        }
    }
    
    private static Map mapping = null;
    
    public static String getOldForNew(String newStr) {
        if(mapping == null ) {
            buildMap();
        }
        return (String)mapping.get(newStr);
    }
    
    private static void buildMap() {
        mapping = new HashMap();
        for(int x=0; x < contents.length; x++) {
            String oldStr = (String)contents[x][0];
            String newStr = (String)contents[x][1];
            mapping.put(newStr, oldStr);
        }
    }
    
static final Object[][] contents = {

///////////////////////////////////////////////////
// Common
///////////////////////////////////////////////////
{"Exit",   "Common.exit"},
{"Ok",     "Common.ok"},
{"Cancel", "Common.cancel"},
{"Browse", "Common.browse"},
{"Cut",    "Common.cut"},
{"Copy",   "Common.copy"},
{"Paste",  "Common.paste"},

// Copy keys to clipboard submenu
{"Copy to clipboard",           "Common.copyToClipBoard"},
{"Copy keys only",              "Common.copyToClipBoard.copyKeysOnly"},
{"Copy keys with filenames",    "Common.copyToClipBoard.copyKeysWithFilenames"},
{"Copy extended info",          "Common.copyToClipBoard.copyExtendedInfo"},
{"Key not available yet",       "Common.copyToClipBoard.extendedInfo.keyNotAvailableYet"},
{"clipboard.File:",             "Common.copyToClipBoard.extendedInfo.file"},
{"clipboard.Key:",              "Common.copyToClipBoard.extendedInfo.key"},
{"clipboard.Bytes:",            "Common.copyToClipBoard.extendedInfo.bytes"},

///////////////////////////////////////////////////
// Splash messages
///////////////////////////////////////////////////
{"Initializing Mainframe",       "Splashscreen.message.1"},
{"Hypercube fluctuating!",       "Splashscreen.message.2"},
{"Sending IP address to NSA",    "Splashscreen.message.3"},
{"Wasting more time",            "Splashscreen.message.4"},
{"Reaching ridiculous speed...", "Splashscreen.message.5"},

///////////////////////////////////////////////////
// First Startup Dialog
///////////////////////////////////////////////////
{"Frost first startup",                                                     "FirstStartupDialog.title"},
{"Please choose the version of Freenet you want to use",                    "FirstStartupDialog.freenetVersion.label"},
{"You can create a new identity or import an existing identities.xml file", "FirstStartupDialog.identity.label"},
{"Create new identity",                                                     "FirstStartupDialog.identity.createNew"},
{"Import existing identities.xml file",                                     "FirstStartupDialog.identity.import"},


///////////////////////////////////////////////////
// Default message texts
///////////////////////////////////////////////////
{"Welcome message",                       "MessagePane.defaultText.welcomeMessage"},
{"Select a board to view its content.",   "MessagePane.defaultText.noBoardSelected"},
{"Select a message to view its content.", "MessagePane.defaultText.noMessageSelected"},

///////////////////////////////////////////////////
// Main Window Menu
///////////////////////////////////////////////////
// Menu File
{"File", "MainFrame.menu.file"},

// Menu News
{"News",                             "MainFrame.menu.news"},
{"Automatic message update",         "MainFrame.menu.news.automaticBoardUpdate"},
{"Configure selected board",         "MainFrame.menu.news.configureBoard"},
{"Display board information window", "MainFrame.menu.news.displayBoardInformationWindow"},
{"Display known boards",             "MainFrame.menu.news.displayKnownBoards"},
{"Search messages",                  "MainFrame.menu.news.searchMessages"},

// Menu Options
{"Options",     "MainFrame.menu.options"},
{"Preferences", "MainFrame.menu.options.preferences"},

// Menu Plugins
//{"Plugins", "Plugins"},
//{"Experimental Freenet Browser", "Experimental Freenet Browser"},
//{"Translate Frost into another language", "Translate Frost into another language"},

// Menu Language
{"Language",  "MainFrame.menu.language"},
{"Default",   "MainFrame.menu.language.default"},
{"German",    "MainFrame.menu.language.german"},
{"English",   "MainFrame.menu.language.english"},
{"Dutch",     "MainFrame.menu.language.dutch"},
{"Japanese",  "MainFrame.menu.language.japanese"},
{"French",    "MainFrame.menu.language.french"},
{"Italian",   "MainFrame.menu.language.italian"},
{"Spanish",   "MainFrame.menu.language.spanish"},
{"Bulgarian", "MainFrame.menu.language.bulgarian"},
{"Russian",   "MainFrame.menu.language.russian"},

// Menu Help
{"Help",                "MainFrame.menu.help"},
{"Show memory monitor", "MainFrame.menu.help.showMemoryMonitor"},
{"Help",                "MainFrame.menu.help.help"},
{"About",               "MainFrame.menu.help.aboutFrost"},

///////////////////////////////////////////////////
// Main Window ToolBar
///////////////////////////////////////////////////
{"New board",                    "MainFrame.toolbar.tooltip.newBoard"},
{"New folder",                   "MainFrame.toolbar.tooltip.newFolder"},
{"Configure board",              "MainFrame.toolbar.tooltip.configureBoard"},
{"Rename folder",                "MainFrame.toolbar.tooltip.renameFolder"},
{"Cut board",                    "MainFrame.toolbar.tooltip.cutBoard"},
{"Paste board",                  "MainFrame.toolbar.tooltip.pasteBoard"},
{"Remove board",                 "MainFrame.toolbar.tooltip.removeBoard"},
{"Board Information Window",     "MainFrame.toolbar.tooltip.boardInformationWindow"},
{"Display list of known boards", "MainFrame.toolbar.tooltip.displayListOfKnownBoards"},
{"Search messages",              "MainFrame.toolbar.tooltip.searchMessages"},
{"Minimize to System Tray",      "MainFrame.toolbar.tooltip.minimizeToSystemTray"},

///////////////////////////////////////////////////
// Main Window Tabbed Pane
///////////////////////////////////////////////////
{"News",      "MainFrame.tabbedPane.news"},
{"Downloads", "MainFrame.tabbedPane.downloads"},
{"Search",    "MainFrame.tabbedPane.search"},
{"Uploads",   "MainFrame.tabbedPane.uploads"},

///////////////////////////////////////////////////
// Main Window News Tab
///////////////////////////////////////////////////
// ToolBar in News Tab
{"Next unread message", "MessagePane.toolbar.tooltip.nextUnreadMessage"},
{"Save message",        "MessagePane.toolbar.tooltip.saveMessage"},
{"New message",         "MessagePane.toolbar.tooltip.newMessage"},
{"Reply",               "MessagePane.toolbar.tooltip.reply"},
{"Update",              "MessagePane.toolbar.tooltip.update"},
{"Trust",               "MessagePane.toolbar.tooltip.setToGood"},
{"Set to CHECK",        "MessagePane.toolbar.tooltip.setToCheck"},
{"Set to OBSERVE",      "MessagePane.toolbar.tooltip.setToObserve"},
{"Do not trust",        "MessagePane.toolbar.tooltip.setToBad"},

//Popup over BOARD attachments table
{"Add Board(s)",                         "MessagePane.boardAttachmentTable.popupmenu.addBoards"},
{"Add Board(s) to folder",               "MessagePane.boardAttachmentTable.popupmenu.addBoardsToFolder"},
{"Add board(s) to list of known boards", "MessagePane.boardAttachmentTable.popupmenu.addBoardsToKnownBoards"},

//Popup over FILE attachments table
{"Download attachment(s)",       "MessagePane.fileAttachmentTable.popupmenu.downloadAttachments"},
{"Download selected attachment", "MessagePane.fileAttachmentTable.popupmenu.downloadSelectedAttachment"},

// Popup over message table
{"Mark message unread",      "MessagePane.messageTable.popupmenu.markMessageUnread"},
{"Mark ALL messages read",   "MessagePane.messageTable.popupmenu.markAllMessagesRead"},
{"help user (sets to GOOD)", "MessagePane.messageTable.popupmenu.setToGood"},
{"block user (sets to BAD)", "MessagePane.messageTable.popupmenu.setToBad"},
{"set to neutral (CHECK)",   "MessagePane.messageTable.popupmenu.setToCheck"},
{"observe user (OBSERVE)",   "MessagePane.messageTable.popupmenu.setToObserve"},
{"Delete message",           "MessagePane.messageTable.popupmenu.deleteMessage"},
{"Undelete message",         "MessagePane.messageTable.popupmenu.undeleteMessage"},

// message table columns
{"Index",         "MessagePane.messageTable.index"},
{"From",          "MessagePane.messageTable.from"},
{"Subject",       "MessagePane.messageTable.subject"},
{"Sig",           "MessagePane.messageTable.sig"},
{"Date",          "MessagePane.messageTable.date"},

// Popup over message (main frame)
{"Copy",                 "MessagePane.messageText.popupmenu.copy"},
{"Save message to disk", "MessagePane.messageText.popupmenu.saveMessageToDisk"},

{"Save message to disk", "MessagePane.messageText.saveDialog.title"},

// board states
{"read access",  "Board.boardState.readAccess"},
{"write access", "Board.boardState.writeAccess"},
{"public board", "Board.boardState.publicBoard"},
{"(INVALID, no public key, but private!)", "Board.boardState.invalid"},

//AttachedBoardTableModel
{"Board Name",     "MessagePane.boardAttachmentTable.boardName"},
{"Access rights",  "MessagePane.boardAttachmentTable.accessRights"},
{"Description",    "MessagePane.boardAttachmentTable.description"},

//AttachedFilesTableModel
{"Filename", "MessagePane.fileAttachmentTable.filename"},
{"Size",     "MessagePane.fileAttachmentTable.size"},
{"Key",      "MessagePane.fileAttachmentTable.key"},

///////////////////////////////////////////////////
// Main Window Search Tab
///////////////////////////////////////////////////
// ToolBar in Search tab
{"Search",                 "SearchPane.toolbar.tooltip.search"}, 
{"Download selected keys", "SearchPane.toolbar.tooltip.downloadSelectedKeys"},
{"all boards",             "SearchPane.toolbar.searchAllBoards"},
{"Results",                "SearchPane.toolbar.results"},

// Popup over search results table
{"Download all keys",        "SearchPane.resultTable.popupmenu.downloadAllKeys"},
{"Download selected keys",   "SearchPane.resultTable.popupmenu.downloadSelectedKeys"},
{"help user (sets to GOOD)", "SearchPane.resultTable.popupmenu.setToGood"},
{"block user (sets to BAD)", "SearchPane.resultTable.popupmenu.setToBad"},

//SearchComboBox
{"All files",     "SearchPane.fileTypes.allFiles"},
{"Audio",         "SearchPane.fileTypes.audio"},
{"Video",         "SearchPane.fileTypes.video"},
{"Images",        "SearchPane.fileTypes.images"},
{"Documents",     "SearchPane.fileTypes.documents"},
{"Executables",   "SearchPane.fileTypes.executables"},
{"Archives",      "SearchPane.fileTypes.archives"},

///////////////////////////////////////////////////
// Main Window Downloads Tab
///////////////////////////////////////////////////
// ToolBar in Downloads Tab
{"Activate downloading", "DownloadPane.toolbar.tooltip.activateDownloading"},
{"Pause downloading",    "DownloadPane.toolbar.tooltip.pauseDownloading"},
{"Waiting",              "DownloadPane.toolbar.waiting"},

// Popup over download table
{"Restart selected downloads",             "DownloadPane.fileTable.popupmenu.restartSelectedDownloads"},
{"Enable downloads",                       "DownloadPane.fileTable.popupmenu.enableDownloads"},
{"Enable selected downloads",              "DownloadPane.fileTable.popupmenu.enableDownloads.enableSelectedDownloads"},
{"Disable selected downloads",             "DownloadPane.fileTable.popupmenu.enableDownloads.disableSelectedDownloads"},
{"Invert enabled state for selected downloads", "DownloadPane.fileTable.popupmenu.enableDownloads.invertEnabledStateForSelectedDownloads"},
{"Enable all downloads",                   "DownloadPane.fileTable.popupmenu.enableDownloads.enableAllDownloads"},
{"Disable all downloads",                  "DownloadPane.fileTable.popupmenu.enableDownloads.disableAllDownloads"},
{"Invert enabled state for all downloads", "DownloadPane.fileTable.popupmenu.enableDownloads.invertEnabledStateForAllDownloads"},
{"Remove",                                 "DownloadPane.fileTable.popupmenu.remove"},
{"Remove selected downloads",              "DownloadPane.fileTable.popupmenu.remove.removeSelectedDownloads"},
{"Remove all downloads",                   "DownloadPane.fileTable.popupmenu.remove.removeAllDownloads"},
{"Remove finished downloads",              "DownloadPane.fileTable.popupmenu.remove.removeFinishedDownloads"},

///////////////////////////////////////////////////
// Main Window Uploads Tab
///////////////////////////////////////////////////
// ToolBar in Uploads Tab
{"Browse", "UploadPane.toolbar.tooltip.browse"},

// FileChooser in Uploads Tab
{"Select files you want to upload to board ''{0}''",     "UploadPane.filechooser.title"},

// Popup over uploads table
{"Remove",                                       "UploadPane.fileTable.popupmenu.remove"},
{"Remove selected files",                        "UploadPane.fileTable.popupmenu.remove.removeSelectedFiles"},
{"Remove all files",                             "UploadPane.fileTable.popupmenu.remove.removeAllFiles"},
{"Start encoding of selected files",             "UploadPane.fileTable.popupmenu.startEncodingOfSelectedFiles"},
{"Upload selected files",                        "UploadPane.fileTable.popupmenu.uploadSelectedFiles"},
{"Upload all files",                             "UploadPane.fileTable.popupmenu.uploadAllFiles"},
{"Set prefix for selected files",                "UploadPane.fileTable.popupmenu.setPrefixForSelectedFiles"},
{"Set prefix for all files",                     "UploadPane.fileTable.popupmenu.setPrefixForAllFiles"},
{"Restore default filenames for selected files", "UploadPane.fileTable.popupmenu.restoreDefaultFilenamesForSelectedFiles"},
{"Restore default filenames for all files",      "UploadPane.fileTable.popupmenu.restoreDefaultFilenamesForAllFiles"},
{"Change destination board",                     "UploadPane.fileTable.popupmenu.changeDestinationBoard"},
{"Please enter the prefix you want to use for your files.", "UploadPane.fileTable.popupmenu.prefixInputLabel"},

///////////////////////////////////////////////////
// Main Window Board Selection Tree
///////////////////////////////////////////////////
// Popup over Tree
{"Refresh",                   "BoardTree.popupmenu.refresh"},
{"Remove",                    "BoardTree.popupmenu.remove"},
{"folder",                    "BoardTree.popupmenu.folder"},
{"Folder",                    "BoardTree.popupmenu.Folder"},
{"board",                     "BoardTree.popupmenu.board"},
{"Board",                     "BoardTree.popupmenu.Board"},
{"Cut",                       "BoardTree.popupmenu.cut"},
{"Paste",                     "BoardTree.popupmenu.paste"},
{"Add new board",             "BoardTree.popupmenu.addNewBoard"},
{"Add new folder",            "BoardTree.popupmenu.addNewFolder"},
{"Sort folder",               "BoardTree.popupmenu.sortFolder"},
{"Mark ALL messages read",    "BoardTree.popupmenu.markAllMessagesRead"},
{"Rename folder",             "BoardTree.popupmenu.renameFolder"},
{"Configure selected board",  "BoardTree.popupmenu.configureSelectedBoard"},
{"Configure selected folder", "BoardTree.popupmenu.configureSelectedFolder"},

///////////////////////////////////////////////////
// Main Window Status Bar
///////////////////////////////////////////////////
//{"Up", "Up"},
//{"Down", "Down"},
{"UploadStatusPanel.Uploading",     "MainFrame.statusBar.uploading"},
{"DownloadStatusPanel.Downloading", "MainFrame.statusBar.downloading"},
{"TOFUP",                           "MainFrame.statusBar.TOFUP"},
{"TOFDO",                           "MainFrame.statusBar.TOFDO"},
//{"Results", "Results"},
{"Selected board",                  "MainFrame.statusBar.selectedBoard"},
{"StatusPanel.file",                "MainFrame.statusBar.file"},
{"StatusPanel.files",               "MainFrame.statusBar.files"},

///////////////////////////////////////////////////
// New Message Window
///////////////////////////////////////////////////
{"Create message",                                     "MessageFrame.createMessage.title"},
{"Send message",                                       "MessageFrame.toolbar.tooltip.sendMessage"},
{"Cancel",                                             "MessageFrame.toolbar.tooltip.cancelMessage"},
{"Add attachment(s)",                                  "MessageFrame.toolbar.tooltip.addFileAttachments"},
{"Add Board(s)",                                       "MessageFrame.toolbar.tooltip.addBoardAttachments"},
{"Sign",                                               "MessageFrame.toolbar.sign"},
{"Encrypt for",                                        "MessageFrame.toolbar.encryptFor"},
{"Indexed attachments",                                "MessageFrame.toolbar.indexedAttachments"},
{"Should file attachments be added to upload table?",  "MessageFrame.toolbar.tooltip.indexedAttachments"},
{"Board",                                              "MessageFrame.board"},
{"From",                                               "MessageFrame.from"},
{"Subject",                                            "MessageFrame.subject"},
{"No subject specified!",                              "MessageFrame.defaultSubjectWarning.title"},
{"Do you want to enter a subject?",                    "MessageFrame.defaultSubjectWarning.text"},
{"No subject specified!",                              "MessageFrame.noSubjectError.title"},
{"You must enter a subject!",                          "MessageFrame.noSubjectError.text"},
{"No 'From' specified!",                               "MessageFrame.noSenderError.title"},
{"You must enter a sender name!",                      "MessageFrame.noSenderError.text"},

{"Remove",                                             "MessageFrame.attachmentTables.popupmenu.remove"},
{"Boardname",                                          "MessageFrame.boardAttachmentTable.boardname"},
{"Public key",                                         "MessageFrame.boardAttachmentTable.publicKey"},
{"Private key",                                        "MessageFrame.boardAttachmentTable.privateKey"},
{"Description",                                        "MessageFrame.boardAttachmentTable.description"},

{"Filename",                                           "MessageFrame.fileAttachmentTable.filename"},
{"Size",                                               "MessageFrame.fileAttachmentTable.size"},

{"Choose file(s) / directory(s) to attach",            "MessageFrame.fileChooser.title"},

{"Encrypt error",                                      "MessageFrame.encryptErrorNoRecipient.title"},
{"Can't encrypt the message, no recipient choosed!",   "MessageFrame.encryptErrorNoRecipient.body"},

// Attach private board warning
{"MessageFrame.ConfirmTitle", "MessageFrame.attachBoard.sendPrivateKeyConfirmationDialog.title"},
{"You have the private key to board ''{0}''. Are you sure you want it attached?\nIf you choose NO, only the public key will be attached.", 
    "MessageFrame.attachBoard.sendPrivateKeyConfirmationDialog.body"},                              

///////////////////////////////////////////////////
/// Message Window
///////////////////////////////////////////////////
{"Board",   "MessageWindow.board"},
{"Date",    "MessageWindow.date"},
{"From",    "MessageWindow.from"},
{"Subject", "MessageWindow.subject"},

///////////////////////////////////////////////////
// BoardsChooser
///////////////////////////////////////////////////
{"Choose boards", "BoardsChooser.title"},

///////////////////////////////////////////////////
// TargetFolderChooser
///////////////////////////////////////////////////
{"Choose a target folder" , "TargetFolderChooser.title"},

///////////////////////////////////////////////////
// About box
///////////////////////////////////////////////////
{"About",                             "AboutBox.title"},
{"Version",                           "AboutBox.label.version"},
{"Open Source Project (GPL license)", "AboutBox.label.openSourceProject"},
{"Development:",                      "AboutBox.text.development"},
{"Windows Installer:",                "AboutBox.text.windowsInstaller"},
{"System Tray Executables:",          "AboutBox.text.systemTrayExecutables"},
{"Translation Support:",              "AboutBox.text.translationSupport"},
{"Splash Screen Logo:",               "AboutBox.text.splashScreenLogo"},
{"Misc code contributions:",          "AboutBox.text.miscCodeContributions"},

{"More",                              "DialogWithDetails.button.more"},
{"Less",                              "DialogWithDetails.button.less"},

///////////////////////////////////////////////////
// Preferences
///////////////////////////////////////////////////
{"Options",     "Options.title"},
// More often used translations
{"On",       "Options.common.on"},
{"Off",      "Options.common.off"},
{"minutes",  "Options.common.minutes"},
{"hours",    "Options.common.hours"},

// Downloads Panel
{"Downloads",                                           "Options.downloads"},
{"Disable downloads",                                   "Options.downloads.disableDownloads"},
{"Download directory",                                  "Options.downloads.downloadDirectory"},
{"Restart failed downloads",                            "Options.downloads.restartFailedDownloads"},
{"Maximum number of retries",                           "Options.downloads.maximumNumberOfRetries"},
{"Waittime after each try",                             "Options.downloads.waittimeAfterEachTry"},
{"Enable requesting of failed download files",          "Options.downloads.enableRequestingOfFailedDownloadFiles"},
{"Request file after this count of retries",            "Options.downloads.requestFileAfterThisCountOfRetries"},
{"Number of simultaneous downloads",                    "Options.downloads.numberOfSimultaneousDownloads"},
{"Number of splitfile threads",                         "Options.downloads.numberOfSplitfileThreads"},
{"Remove finished downloads every 5 minutes",           "Options.downloads.removeFinishedDownloadsEvery5Minutes"},
{"Try to download all segments, even if one fails",     "Options.downloads.tryToDownloadAllSegments"},
{"Decode each segment immediately after its download",  "Options.downloads.decodeEachSegmentImmediately"},
{"Select download directory",                           "Options.downloads.filechooser.title"},

// Uploads Panel
{"Uploads",                                   "Options.uploads"},
{"Disable uploads",                           "Options.uploads.disableUploads"},
{"Restart failed uploads",                    "Options.uploads.restartFailedUploads"},
{"Waittime after each try",                   "Options.uploads.waittimeAfterEachTry"},
{"Maximum number of retries",                 "Options.uploads.maximumNumberOfRetries"},
{"Automatic Indexing",                        "Options.uploads.automaticIndexing"},
{"Share Downloads",                           "Options.uploads.shareDownloads"},
{"Sign shared files",                         "Options.uploads.signSharedFiles"},
{"Help spread files from people marked GOOD", "Options.uploads.helpSpreadFilesFromPeopleMarkedGood"},
{"Upload HTL",                                "Options.uploads.uploadHtl"},
{"up htl explanation",                        "Options.uploads.uploadHtlExplanation"},
{"Number of simultaneous uploads",            "Options.uploads.numberOfSimultaneousUploads"},
{"Number of splitfile threads",               "Options.uploads.numberOfSplitfileThreads"},
{"splitfile explanation",                     "Options.uploads.numberOfSplitfileThreadsExplanation"},
{"Upload batch size",                         "Options.uploads.uploadBatchSize"},
{"batch explanation",                         "Options.uploads.uploadBatchSizeExplanation"},

// common for News panels
{"News",                                  "Options.news"},

// News (1) Panel
{"Message upload HTL",                    "Options.news.1.messageUploadHtl"},
{"Message download HTL",                  "Options.news.1.messageDownloadHtl"},
{"Number of days to display",             "Options.news.1.numberOfDaysToDisplay"},
{"Number of days to download backwards",  "Options.news.1.numberOfDaysToDownloadBackwards"},
{"Message base",                          "Options.news.1.messageBase"},
{"Signature",                             "Options.news.1.signature"},

// News (2) Panel
{"Block messages with subject containing (separate by ';' )",    "Options.news.2.blockMessagesWithSubject"},
{"Block messages with body containing (separate by ';' )",       "Options.news.2.blockMessagesWithBody"},
{"Block messages with these attached boards (separate by ';' )", "Options.news.2.blockMessagesWithTheseBoards"},

{"Hide messages with trust states",                                    "Options.news.2.hideMessagesWithTrustStates"},
{"Don't add boards to known boards list from users with trust states", "Options.news.2.dontAddBoardsFromTrustStates"},
{"Bad",             "Options.news.2.trustState.bad"},
{"Check",           "Options.news.2.trustState.check"},
{"Observe",         "Options.news.2.trustState.observe"},
{"None (unsigned)", "Options.news.2.trustState.none"},

{"Do spam detection",             "Options.news.2.doSpamDetection"},
{"Sample interval",               "Options.news.2.sampleInterval"},
{"Threshold of blocked messages", "Options.news.2.thresholdOfBlockedMessages"},

// News (3) Panel
{"Automatic update options",                           "Options.news.3.automaticUpdateOptions"},
{"Automatic message update",                           "Options.news.3.automaticBoardUpdate"},
{"Minimum update interval of a board",                 "Options.news.3.minimumUpdateInterval"},
{"Number of concurrently updating boards",             "Options.news.3.numberOfConcurrentlyUpdatingBoards"},
{"Show board update visualization",                    "Options.news.3.showBoardUpdateVisualization"},
{"Background color if updating board is selected",     "Options.news.3.backgroundColorIfUpdatingBoardIsSelected"},
{"Background color if updating board is not selected", "Options.news.3.backgroundColorIfUpdatingBoardIsNotSelected"},
{"Choose",                                             "Options.news.3.choose"},
{"Color",                                              "Options.news.3.color"},
{"Choose updating color of SELECTED boards",           "Options.news.3.colorChooserDialog.title.chooseUpdatingColorOfSelectedBoards"},
{"Choose updating color of NON-SELECTED boards",       "Options.news.3.colorChooserDialog.title.chooseUpdatingColorOfUnselectedBoards"},
{"Silently retry failed messages",                     "Options.news.3.silentlyRetryFailedMessages"},
{"Show deleted messages",                              "Options.news.3.showDeletedMessages"},
{"Receive duplicate messages",                         "Options.news.3.receiveDuplicateMessages"},

// Expiration panel
{"Expiration",                              "Options.expiration"},
{"Number of days before a message expires", "Options.expiration.numberOfDaysBeforeMessageExpires"},
{"Keep expired messages in keypool",        "Options.expiration.keepExpiredMessages"},
{"Archive expired messages",                "Options.expiration.archiveExpiredMessages"},
{"Archive folder",                          "Options.expiration.archiveFolder"},
{"Delete expired messages from keypool",    "Options.expiration.deleteExpiredMessages"},
{"Select archive directory",                "Options.expiration.fileChooser.title.selectArchiveDirectory"},

// Search Panel
{"Search",                             "Options.search"},
{"Image Extension",                    "Options.search.imageExtension"},
{"Video Extension",                    "Options.search.videoExtension"},
{"Archive Extension",                  "Options.search.archiveExtension"},
{"Document Extension",                 "Options.search.documentExtension"},
{"Audio Extension",                    "Options.search.audioExtension"},
{"Executable Extension",               "Options.search.executableExtension"},
{"Maximum search results",             "Options.search.maximumSearchResults"},
{"Hide files from people marked BAD",  "Options.search.hideFilesFromPeopleMarkedBad"},
{"Hide files from anonymous users",    "Options.search.hideFilesFromAnonymousUsers"},

//Display Panel
{"Display",                          "Options.display"},
{"EnableSkins",                      "Options.display.enableSkins"},
{"MoreSkinsAt",                      "Options.display.youCanGetMoreSkinsAt"},
{"Preview",                          "Options.display.preview"},
{"RefreshList",                      "Options.display.refreshList"},
{"NoSkinsFound",                     "Options.display.noSkinsFound"},
{"AvailableSkins",                   "Options.display.availableSkins"},
{"Plain",                            "Options.display.fontChooser.plain"},
{"Italic",                           "Options.display.fontChooser.italic"},
{"Bold",                             "Options.display.fontChooser.bold"},
{"Bold Italic",                      "Options.display.fontChooser.boldItalic"},
{"Sample",                           "Options.display.fontChooser.sample"},
{"Choose a Font",                    "Options.display.fontChooser.title"},
{"Sample",                           "Options.display.fontChooser.sample"},
{"Fonts",                            "Options.display.fonts"},
{"Message Body",                     "Options.display.messageBody"},
{"Message List",                     "Options.display.messageList"},
{"File List",                        "Options.display.fileList"},
{"Choose",                           "Options.display.choose"},
{"EnableMessageBodyAA",              "Options.display.enableAntialiasingForMessageBody"},
{"EnableMsgTableMultilineSelect",    "Options.display.enableMultilineSelectionsInMessageTable"},
{"ShowBoardDescTooltips",            "Options.display.showTooltipWithBoardDescriptionInBoardTree"},

// Miscellaneous Panel
{"Miscellaneous",                   "Options.miscellaneous"},
{"Keyfile upload HTL",              "Options.miscellaneous.keyfileUploadHtl"},
{"Keyfile download HTL",            "Options.miscellaneous.keyfileDownloadHtl"},
{"list of nodes",                   "Options.miscellaneous.listOfFcpNodes"},
{"list of nodes 2",                 "Options.miscellaneous.listOfFcpNodesExplanation"},
{"Allow 2 byte characters",         "Options.miscellaneous.allow2ByteCharacters"},
{"Use editor for writing messages", "Options.miscellaneous.useEditorForWritingMessages"},
{"Clean the keypool",               "Options.miscellaneous.cleanTheKeypool"},
{"Automatic saving interval",       "Options.miscellaneous.automaticSavingInterval"},
{"Disable splashscreen",            "Options.miscellaneous.disableSplashscreen"},
{"Show systray icon",               "Options.miscellaneous.showSysTrayIcon"},
{"Enable logging",                  "Options.miscellaneous.enableLogging"},
{"Logging level",                   "Options.miscellaneous.loggingLevel"},
{"Log file size limit (in KB)",     "Options.miscellaneous.logFileSizeLimit"},
{"Very high",                       "Options.miscellaneous.logLevel.veryHigh"},
{"High",                            "Options.miscellaneous.logLevel.high"},
{"Medium",                          "Options.miscellaneous.logLevel.medium"},
{"Low",                             "Options.miscellaneous.logLevel.low"},
{"Very low",                        "Options.miscellaneous.logLevel.veryLow"},

///////////////////////////////////////////////////
// Board Information Window
///////////////////////////////////////////////////
{"BoardInfoFrame.UpdateSelectedBoardButton", "BoardInfoFrame.button.updateSelectedBoard"},
{"BoardInfoFrame.Update",                    "BoardInfoFrame.button.update"},
{"BoardInfoFrame.Update all boards",         "BoardInfoFrame.button.updateAllBoards"},
{"BoardInfoFrame.Close",                     "BoardInfoFrame.button.close"},
{"BoardInfoFrame.Board information window",  "BoardInfoFrame.title"},
{"BoardInfoFrame.Boards",                    "BoardInfoFrame.label.boards"},
{"BoardInfoFrame.Messages",                  "BoardInfoFrame.label.messages"},
{"BoardInfoFrame.Files",                     "BoardInfoFrame.label.files"},

// Board information window table
{"Messages",         "BoardInfoFrame.table.messages"},
{"Files",            "BoardInfoFrame.table.files"},
{"Board",            "BoardInfoFrame.table.board"},
{"State",            "BoardInfoFrame.table.state"},
{"Messages Today",   "BoardInfoFrame.table.messagesToday"},

///////////////////////////////////////////////////
// List of known boards window
///////////////////////////////////////////////////
{"KnownBoardsFrame.List of known boards", "KnownBoardsFrame.title"},
{"KnownBoardsFrame.Lookup",               "KnownBoardsFrame.label.lookup"},
{"KnownBoardsFrame.Filter",               "KnownBoardsFrame.label.filter"},
{"KnownBoardsFrame.Close",                "KnownBoardsFrame.button.close"},
{"Add Board(s)",                          "KnownBoardsFrame.button.addBoards"},
{"Add Board(s) to folder",                "KnownBoardsFrame.button.addBoardsToFolder"},
{"Remove board",                          "KnownBoardsFrame.button.removeBoard"},

{"KnownBoardsTableModel.Boardname",       "KnownBoardsFrame.table.boardName"},
{"Private key",                           "KnownBoardsFrame.table.privateKey"},
{"Public key",                            "KnownBoardsFrame.table.publicKey"},
{"Description",                           "KnownBoardsFrame.table.description"},

///////////////////////////////////////////////////
// Core start messages
///////////////////////////////////////////////////

{"Core.init.NodeNotRunningBody",                      "Core.init.NodeNotRunningBody"},
{"Core.init.NodeNotRunningTitle",                     "Core.init.NodeNotRunningTitle"},
{"Core.loadIdentities.ConnectionNotEstablishedBody",  "Core.loadIdentities.ConnectionNotEstablishedBody"},
{"Core.loadIdentities.ConnectionNotEstablishedTitle", "Core.loadIdentities.ConnectionNotEstablishedTitle"},
{"Core.init.TestnetWarningTitle",                     "Core.init.TestnetWarningTitle"},
{"Core.init.TestnetWarningBody",                      "Core.init.TestnetWarningBody"},
{"Core.init.UnsupportedFreenetVersionTitle",          "Core.init.UnsupportedFreenetVersionTitle"},
{"Core.init.UnsupportedFreenetVersionBody",           "Core.init.UnsupportedFreenetVersionBody"},

{"Core.loadIdentities.ChooseName",                    "Core.loadIdentities.ChooseName"},
{"Core.loadIdentities.InvalidNameBody",               "Core.loadIdentities.InvalidNameBody"},
{"Core.loadIdentities.InvalidNameTitle",              "Core.loadIdentities.InvalidNameTitle"},

///////////////////////////////////////////////////
// Board Settings Dialog
///////////////////////////////////////////////////
{"Settings for board",                 "BoardSettings.title.boardSettings"},
{"Settings for all boards in folder",  "BoardSettings.title.folderSettings"},
{"Public board",                       "BoardSettings.label.publicBoard"},
{"Secure board",                       "BoardSettings.label.secureBoard"},
{"Private key",                        "BoardSettings.label.privateKey"},
{"Public key",                         "BoardSettings.label.publicKey"},
{"Generate new keypair",               "BoardSettings.button.generateNewKeypair"},
{"Not available",                      "BoardSettings.text.keyNotAvailable"},

{"Override default settings",          "BoardSettings.label.overrideDefaultSettings"},
{"Use default",                        "BoardSettings.label.useDefault"},
{"Set to",                             "BoardSettings.label.setTo"},
{"Yes",                                "BoardSettings.label.yes"},
{"No",                                 "BoardSettings.label.no"},
{"Enable automatic board update",      "BoardSettings.label.enableAutomaticBoardUpdate"},
{"Maximum message display (days)",     "BoardSettings.label.maximumMessageDisplay"},
{"Hide unsigned messages",             "BoardSettings.label.hideUnsignedMessages"},
{"Hide messages flagged BAD",          "BoardSettings.label.hideBadMessages"},
{"Hide messages flagged CHECK",        "BoardSettings.label.hideCheckMessages"},
{"Hide messages flagged OBSERVE",      "BoardSettings.label.hideObserveMessages"},
{"Warning",                            "BoardSettings.generateKeyPairErrorDialog.title"},

{"BoardSettingsFrame.description",     "BoardSettings.label.description"},
{"BoardSettingsFrame.confirmTitle",    "BoardSettings.looseKeysWarningDialog.title"},
{"BoardSettingsFrame.confirmBody",     "BoardSettings.looseKeysWarningDialog.body"},

//  Uploads underway warning when exiting
{"UploadsUnderway.title", "MainFrame.runningUploadsWarning.title"},
{"UploadsUnderway.body",  "MainFrame.runningUploadsWarning.body"},

///////////////////////////////////////////////////
/// TofTree
///////////////////////////////////////////////////
{"New Folder Name",                        "BoardTree.newFolderDialog.title"},
{"Please enter a name for the new folder", "BoardTree.newFolderDialog.body"},
{"newfolder",                              "BoardTree.newFolderDialog.defaultName"},

{"Duplicate board name",                                                 "BoardTree.duplicateNewBoardNameError.title"},
{"You already have a board with name ''{0}''!\nPlease choose a new name.", "BoardTree.duplicateNewBoardNameError.body"},

{"Overwrite existing board",                                             "BoardTree.overWriteBoardConfirmation.title"},
{"You already have a board with name ''{0}''!\nDo you really want to overwrite it?\n(This will not delete messages)", 
    "BoardTree.overWriteBoardConfirmation.body"},

{"Remove folder ''{0}''",     "BoardTree.removeFolderConfirmation.title"},
{"Do you really want to delete folder ''{0}'' ?\nNOTE: Removing it will also remove all boards/folders inside this folder!!!",
        "BoardTree.removeFolderConfirmation.body"},    

{"Remove board ''{0}''",                         "BoardTree.removeBoardConfirmation.title"},
{"Do you really want to delete board ''{0}'' ?", "BoardTree.removeBoardConfirmation.body"},    
        
///////////////////////////////////////////////////
/// SearchTableFormat
///////////////////////////////////////////////////
{"Filename",   "SearchPane.resultTable.filename"},
{"Size",       "SearchPane.resultTable.size"},
{"Age",        "SearchPane.resultTable.age"},
{"From",       "SearchPane.resultTable.from"},
{"Board",      "SearchPane.resultTable.board"},

// states
{"FrostSearchItemObject.Offline",   "SearchPane.resultTable.states.offline"},
{"FrostSearchItemObject.Anonymous", "SearchPane.resultTable.states.anonymous"},
{"SearchTableFormat.Uploading",     "SearchPane.resultTable.states.uploading"},
{"SearchTableFormat.Downloading",   "SearchPane.resultTable.states.downloading"},
{"SearchTableFormat.Downloaded",    "SearchPane.resultTable.states.downloaded"},

///////////////////////////////////////////////////
/// DownloadTableFormat
///////////////////////////////////////////////////
{"DownloadTableFormat.Enabled",     "DownloadPane.fileTable.enabled"},
{"Filename",                        "DownloadPane.fileTable.filename"}, 
{"Size",                            "DownloadPane.fileTable.size"},
{"Age",                             "DownloadPane.fileTable.age"},
{"State",                           "DownloadPane.fileTable.state"},
{"Blocks",                          "DownloadPane.fileTable.blocks"},
{"Tries",                           "DownloadPane.fileTable.tries"},
{"Source",                          "DownloadPane.fileTable.source"},
{"From",                            "DownloadPane.fileTable.from"},
{"Key",                             "DownloadPane.fileTable.key"},

{"Waiting",                         "DownloadPane.fileTable.states.waiting"},
{"Trying",                          "DownloadPane.fileTable.states.trying"},
{"Failed",                          "DownloadPane.fileTable.states.failed"},
{"Done",                            "DownloadPane.fileTable.states.done"},
{"Requesting",                      "DownloadPane.fileTable.states.requesting"},
{"Requested",                       "DownloadPane.fileTable.states.requested"},
{"Decoding segment",                "DownloadPane.fileTable.states.decodingSegment"},

{"FrostSearchItemObject.Offline",   "DownloadPane.fileTable.states.offline"},
{"FrostSearchItemObject.Anonymous", "DownloadPane.fileTable.states.anonymous"},
{"Unknown",                         "DownloadPane.fileTable.states.unknown"},

{"Invalid key",                     "DownloadPane.invalidKeyDialog.title"},
{"Invalid key. Key must begin with one of", "DownloadPane.invalidKeyDialog.body"},

///////////////////////////////////////////////////
/// UploadTableFormat
///////////////////////////////////////////////////
{"DownloadTableFormat.Enabled",    "UploadPane.fileTable.enabled"},
{"Filename",                       "UploadPane.fileTable.filename"},
{"Size",                           "UploadPane.fileTable.size"},
{"Last upload",                    "UploadPane.fileTable.lastUpload"},
{"Path",                           "UploadPane.fileTable.path"},
{"Tries",                          "UploadPane.fileTable.tries"},
{"Destination",                    "UploadPane.fileTable.destination"},
{"Key",                            "UploadPane.fileTable.key"},

{"Never",             "UploadTableFormat.state.never"},
{"Requested",         "UploadTableFormat.state.requested"},
{"Uploading",         "UploadTableFormat.state.uploading"},
{"Encode requested",  "UploadTableFormat.state.encodeRequested"},
{"Encoding file",     "UploadTableFormat.state.encodingFile"},
{"Waiting",           "UploadTableFormat.state.waiting"},
{"Unknown",           "UploadTableFormat.state.unknown"},

///////////////////////////////////////////////////
/// NewBoardDialog
///////////////////////////////////////////////////
{"NewBoardDialog.title",       "NewBoardDialog.title"},
{"NewBoardDialog.details",     "NewBoardDialog.details"},
{"NewBoardDialog.name",        "NewBoardDialog.name"},
{"NewBoardDialog.description", "NewBoardDialog.description"},
{"NewBoardDialog.add",         "NewBoardDialog.add"},


///////////////////////////////////////////////////
//  Frost startup error messages
///////////////////////////////////////////////////
{"Frost.lockFileFound", "Frost.lockFileFound"},

///////////////////////////////////////////////////
//  Message upload failed dialog
///////////////////////////////////////////////////
{"Upload of message failed",                   "MessageUploadFailedDialog.title"},
{"Frost was not able to upload your message.", "MessageUploadFailedDialog.body"},
{"Retry",                                      "MessageUploadFailedDialog.option.retry"},
{"Retry on next startup",                      "MessageUploadFailedDialog.option.retryOnNextStartup"},
{"Discard message",                            "MessageUploadFailedDialog.option.discardMessage"},

///////////////////////////////////////////////////
//  Saver popup
///////////////////////////////////////////////////
{"Saver.AutoTask.title",   "Saver.AutoTask.title"},
{"Saver.AutoTask.message", "Saver.AutoTask.message"},

///////////////////////////////////////////////////
/// AltEdit support
///////////////////////////////////////////////////
{"Error",                                                         "AltEdit.errorDialogs.title"},
{"The message file returned by the alternate editor is invalid.", "AltEdit.errorDialog.invalidReturnedMessageFile"},
{"Could not start alternate editor using command",                "AltEdit.errorDialog.couldNotStartEditorUsingCommand"},
{"Could not create message file for alternate editor",            "AltEdit.errorDialog.couldNotCreateMessageFile"},
{"Configured alternate editor line must contain a '%f' as placeholder for the filename.", "AltEdit.errorDialog.missingPlaceholder"},
{"No alternate editor configured.",                               "AltEdit.errorDialog.noAlternateEditorConfigured"},
{">>> This is a Frost alternate editor message file.                <<<", "AltEdit.textFileMessage.1"},
{">>> You can edit the subject and add text at the end of the file. <<<", "AltEdit.textFileMessage.2"},
{">>> Don't change or delete the marker lines!                      <<<", "AltEdit.textFileMessage.3"},
{"*--- Subject line (changeable) ---*",                                   "AltEdit.markerLine.subject"}, // marker line
{"*--- Enter your text after this line ---*",                             "AltEdit.markerLine.text"}, // marker line

///////////////////////////////////////////////////
/// Search messages dialog
///////////////////////////////////////////////////
{"Search messages",              "SearchMessages.title"},

{"Search",                       "SearchMessages.search"},
{"Sender",                       "SearchMessages.search.sender"},
{"Subject",                      "SearchMessages.search.subject"},
{"Content",                      "SearchMessages.search.content"},
{"Search private messages only", "SearchMessages.search.searchPrivateMessagesOnly"},

{"Boards",                       "SearchMessages.boards"},
{"Search in displayed boards",   "SearchMessages.boards.searchInDisplayedBoards"},
{"Search following boards",      "SearchMessages.boards.searchFollowingBoards"},
{"Choose boards",                "SearchMessages.boards.chooseBoards"},

{"Date",                           "SearchMessages.date"},
{"Search in messages that would be displayed", "SearchMessages.date.searchInMessagesThatWouldBeDisplayed"},
{"Search all dates",               "SearchMessages.date.searchAllDates"},
{"Search between dates",           "SearchMessages.date.searchBetweenDates"},
{"to",                             "SearchMessages.date.to"}, // startDate 'to' endDate
{"Search number of days backward", "SearchMessages.date.searchNumberOfDaysBackward"},

{"Trust state",          "SearchMessages.trustState"},
{"Search in messages that would be displayed",              "SearchMessages.trustState.searchInMessagesThatWouldBeDisplayed"},
{"Search all messages, no matter which trust state is set", "SearchMessages.trustState.searchAllMessages"},
{"Search only in messages with following trust state",      "SearchMessages.trustState.searchOnlyInMessagesWithFollowingTrustState"},
{"Good",                 "SearchMessages.trustState.good"},
{"Observe",              "SearchMessages.trustState.observe"},
{"Check",                "SearchMessages.trustState.check"},
{"Bad",                  "SearchMessages.trustState.bad"},
{"None (unsigned)",      "SearchMessages.trustState.none"},
{"Tampered",             "SearchMessages.trustState.tampered"},

{"Archive",                       "SearchMessages.archive"},
{"Search in keypool and archive", "SearchMessages.archive.searchInKeypoolAndArchive"},
{"Search only in keypool",        "SearchMessages.archive.searchOnlyInKeypool"},
{"Search only in archive",        "SearchMessages.archive.searchOnlyInArchive"},

{"Attachments",                            "SearchMessages.attachments"},
{"Message must contain board attachments", "SearchMessages.attachments.messageMustContainBoardAttachments"},
{"Message must contain file attachments",  "SearchMessages.attachments.messageMustContainFileAttachments"},

{"Search result",  "SearchMessages.label.searchResult"},
{"Results",        "SearchMessages.label.results"},

{"Help",           "SearchMessages.button.help"},
{"Search",         "SearchMessages.button.search"},
{"Stop search",    "SearchMessages.button.stopSearch"},
{"Close",          "SearchMessages.button.close"},
{"Open message",   "SearchMessages.button.openMessage"},

// error messages
{"Error",                                             "SearchMessages.errorDialogs.title"},
{"Please stop the search before closing the window.", "SearchMessages.errorDialog.stopSearchBeforeClose"},
{"No trust state is selected.",                       "SearchMessages.errorDialogs.noTrustStateSelected"},
{"Invalid start date or end date specified.",         "SearchMessages.errorDialogs.invalidStartOrEndDate"},
{"Start date is after end date.",                     "SearchMessages.errorDialogs.startDateIsAfterEndDate"},
{"No boards to search into were chosed.",             "SearchMessages.errorDialogs.noBoardsChosed"},
{"There are no boards that could be choosed.",        "SearchMessages.errorDialogs.noBoardsToChoose"},

// search messages table model
{"Index",         "SearchMessages.resultTable.index"},
{"From",          "SearchMessages.resultTable.from"},
{"Board",         "SearchMessages.resultTable.board"},
{"Subject",       "SearchMessages.resultTable.subject"},
{"Sig",           "SearchMessages.resultTable.sig"},
{"Date",          "SearchMessages.resultTable.date"},

};
}
