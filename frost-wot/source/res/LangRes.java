/*
  LangRes.java
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
package res;

import java.util.ListResourceBundle;

/**
 * This file contains the default translations for Frost. It is VERY IMPORTANT
 * that this file is kept UPTODATE because other translations are based on
 * this file! Please insert the text you want to be translatable at the proper
 * position in this file. If you changed texts or no longer need certain translations
 * please remove them from this file.
 * @author czornack
 */
public class LangRes extends ListResourceBundle {

    public Object[][] getContents() {
    return contents;
    }
    static final Object[][] contents = {
	
	
	///////////////////////////////////////////////////
	// Unknown translations
	// Have to look where in the GUI these translations are needed
	///////////////////////////////////////////////////
	{"Minimize to System Tray", "Minimize to System Tray"},
	{"Index", "Index"},
	{"From", "From"},
	{"Subject", "Subject"},
	{"Date", "Date"},
	{"Filename", "Filename"},
	{"Key", "Key"},
	{"Select a message to view its content.", "Select a message to view its content."},
	{"Size", "Size"},
	{"Age", "Age"},
	{"Board", "Board"},
	{"Waiting", "Waiting"},
	{"State", "State"},
	{"Source", "Source"},
	{"Last upload", "Last upload"},
	{"Path", "Path"},
	{"Destination", "Destination"},
	{"Frost by Jantho", "Frost by Jantho"},
	{"Reload selected files", "Upload selected files"},
	{"Reload all files", "Reload all files"}, //reload????
	{"Trying", "Trying"},
	{"Done", "Done"},
	{"Failed", "Failed"},
	{"Never", "Never"},
	{"Requested", "Requested"},
	{"Uploading", "Uploading"},
	{"Unknown", "Unkown"},
	{"Show systray icon","Show SysTray icon"},
	{"Display","Display"},
	{"Miscellaneous","Miscellaneous"},
	{"Messages Today","Messages Today"},
	//{"Board information","Board information"},
	//{"Boards","Boards"},
	{"Public board","Public board"},
	{"Secure board","Secure board"},
	{"Generate new keypair","Generate new keypair"},
	{"Private key","Private key"},
	{"Public key","Public key"},
	{"Not available","Not available"},
	{"New Node Name","New Board name"},
	
	
	///////////////////////////////////////////////////
    // Welcome message
	///////////////////////////////////////////////////
    {"Welcome message", "Please read this!\n\nTo use Frost, you first need to select a topic from the board information window. You can openthis window with the i-button above. You can send messages and files to people using the same board. It will probably take some time until the first boards show up (press the update button)."},

	///////////////////////////////////////////////////
	// Main Window Menu
	///////////////////////////////////////////////////
    // Menu File
    {"File", "File"},
    {"Exit", "Exit"},

	// Menu News
    {"News", "News"},
    {"Automatic message update", "Automatic board update"},
    {"Increase Font Size", "Increase Font Size"},
    {"Decrease Font Size", "Decrease Font Size"},
    {"Configure selected board", "Configure selected board"},
    {"Display board information window", "Display board information window"},
	{"Display known boards", "Display known boards"},
	
	// Menu Options
    {"Options", "Options"},
    {"Preferences", "Preferences"},

	// Menu Plugins
	{"Plugins", "Plugins"},
	{"Experimental Freenet Browser", "Experimental Freenet Browser"},
	{"Translate Frost into another language", "Translate Frost into another language"},

	// Menu Language
	{"Language", "Language"},
	{"Default", "Default"},
	{"German", "German"},
	{"English", "English"},
	{"Dutch", "Dutch"},
	{"Japanese", "Japanese"},
	{"French", "French"},
	{"Italian", "Italian"},
	{"Spanish", "Spanish"},
	{"Bulgarian","Bulgarian"},
		
	// Menu Help
    {"Help", "Help"},
    {"About", "About"},

	///////////////////////////////////////////////////
    // Main Window ToolBar
	///////////////////////////////////////////////////
	{"New board", "New board"},
	{"New folder", "New folder"},
	{"Configure board", "Configure board"},
	{"Rename folder", "Rename folder"},
	{"Cut board", "Cut board"},
	{"Paste board", "Paste board"},
	{"Remove board", "Remove board"},
	{"Board Information Window", "Board Information Window"},
	{"Display list of known boards", "Display list of known boards"},
    
	///////////////////////////////////////////////////
	// Main Window Tabbed Pane
	///////////////////////////////////////////////////
	{"Search", "Search"},
	{"Downloads", "Downloads"},
	{"Uploads", "Uploads"},
	{"News", "News"},
	{"Status", "Status"},

	///////////////////////////////////////////////////
    // Main Window News Tab
	///////////////////////////////////////////////////
    // ToolBar in News Tab
	{"Save message", "Save message"},
	{"New message", "New message"},
	{"Reply", "Reply"},
	{"Update", "Update"},
	{"Download attachment(s)", "Download attachment(s)"},
	{"Add Board(s)","Add Board(s)"},
	{"Trust","Trust"},
	{"Set to CHECK", "Set to CHECK"},
	{"Do not trust","Do not trust"},

	// Popup over message table
	{"Mark message unread", "Mark message unread"},
	{"Mark ALL messages read", "Mark ALL messages read"},
	{"Cancel", "Cancel"},	

	// Popup over message
	{"Save message to disk", "Save message to disk"},
	//{"Cancel", "Cancel"}, // Defined above

	// Popup over attachments table
	{"Add selected board", "Add selected board"},	
	{"Download selected attachment", "Download selected attachment"},	
	//{"Cancel", "Cancel"}, // Defined above
	
	///////////////////////////////////////////////////
	// Main Window Search Tab
	///////////////////////////////////////////////////
	// ToolBar in Search tab
	//{"Search", "Search"}, // Defined above
	{"Download selected keys", "Download selected keys"},
	{"all boards", "all boards"},
	
	// Popup over search results table
	//{"Download selected keys", "Download selected keys"}, // Defined above
	{"Download all keys", "Download all keys"},
	{"help user (sets to GOOD)", "help user (sets to GOOD)"},
	{"block user (sets to BAD)", "block user (sets to BAD)"},
	{"set to neutral (CHECK)","set to neutral (CHECK)"},
	//{"Cancel", "Cancel"}, // Defined above
		
	///////////////////////////////////////////////////
    // Main Window Downloads Tab
	///////////////////////////////////////////////////
	// ToolBar in Downloads Tab
	{"Activate downloading", "Activate downloading"},
	{"Show healing information", "Show healing information"},
    
    // Popup over download table
	{"Restart selected downloads", "Restart selected downloads"},
	{"Enable downloads", "Enable downloads"},
	{"Enable selected downloads", "Enable selected downloads"},
	{"Disable selected downloads", "Disable selected downloads"},
	{"Invert enabled state for selected downloads", "Invert enabled state for selected downloads"},
	{"Enable all downloads", "Enable all downloads"},
	{"Disable all downloads", "Disable all downloads"},
	{"Invert enabled state for all downloads", "Invert enabled state for all downloads"},
	{"Remove", "Remove"},
	{"Remove selected downloads", "Remove selected downloads"},
	{"Remove all downloads", "Remove all downloads"},
	{"Remove finished downloads", "Remove finished downloads"},
	//{"Cancel", "Cancel"}, // Defined above
	
	//Download table
	{"Blocks", "Blocks"},
	{"Tries", "Tries"},
        
	///////////////////////////////////////////////////
    // Main Window Uploads Tab
	///////////////////////////////////////////////////
	// ToolBar in Uploads Tab
	{"Browse", "Browse"},

    // FileChooser in Uploads Tab
	{"Select files you want to upload to the", "Select files you want to upload to the"},
	{"board", "board"},

	// Popup over uploads table    
	{"Copy to clipboard", "Copy to clipboard"},
		{"CHK key", "CHK key"},
		{"CHK key + filename", "CHK key + filename"},
	//{"Remove", "Remove"}, // Defined above
		{"Remove selected files", "Remove selected files"},
		{"Remove all files", "Remove all files"},
	{"Start encoding of selected files", "Start encoding of selected files"},
	{"Upload selected files", "Upload selected files"},
	{"Upload all files", "Upload all files"},
	{"Set prefix for selected files", "Set prefix for selected files"},
	{"Set prefix for all files", "Set prefix for all files"},
	{"Restore default filenames for selected files", "Restore default filenames for selected files"},
	{"Restore default filenames for all files", "Restore default filenames for all files"},
	{"Change destination board", "Change destination board"},
	{"Please enter the prefix you want to use for your files.", "Please enter the prefix you want to use for your files."}, 
	//{"Cancel", "Cancel"}, // Defined above    
    
	///////////////////////////////////////////////////
	// Main Window Board Selection Tree
	///////////////////////////////////////////////////
    // Popup over Tree
	{"Refresh", "Refresh"},
	{"Remove", "Remove"},
	{"folder", "folder"},
	{"Folder", "Folder"},
	{"board", "board"},
	{"Board", "Board"},
	{"Cut", "Cut"},
	{"Paste", "Paste"},
	{"Add new board", "Add new board"},
	{"Add new folder", "Add new folder"},
	{"Configure selected board", "Configure selected board"},
	//{"Remove board", "Remove board"}, // Defined above
	//{"Cut board", "Cut board"}, // Defined above
	//{"Cancel", "Cancel"}, // Defined above
    
	///////////////////////////////////////////////////
    // Main Window Status Bar
	///////////////////////////////////////////////////
	{"Up", "Up"},
	{"Down", "Down"},
	{"TOFUP", "TOFUP"},
	{"TOFDO", "TOFDO"},
	{"Results", "Results"},
	{"Files", "Files"},
	{"Selected board", "Selected board"},
      
	///////////////////////////////////////////////////
	// New Message Window
	///////////////////////////////////////////////////
	{"Create message", "Create message"},
	{"Send message", "Send message"},
	{"Add attachment(s)", "Add attachment(s)"},
	{"Sign", "Sign"},
	{"Indexed attachments", "Indexed attachments"},
	{"Should file attachments be added to upload table?", "Should file attachments be added to upload table?"},
	{"Board", "Board"},
	{"From", "From"},
	{"Subject", "Subject"},
	{"Remove", "Remove"},
	{"Do you want to enter a subject?", "Do you want to enter a subject?"},
	{"No subject specified!", "No subject specified!"},
	{"You must enter a subject!", "You must enter a subject!"},
	{"You must enter a sender name!", "You must enter a sender name!"},
	{"No 'From' specified!", "No 'From' specified!"},
	{"Choose file(s) / directory(s) to attach", "Choose file(s) / directory(s) to attach"},
	{"Choose boards to attach", "Choose boards to attach"},

	///////////////////////////////////////////////////
	// About box
	///////////////////////////////////////////////////
	{"About", "About"},
	{"OK", "OK"},

	///////////////////////////////////////////////////
	// Preferences
	///////////////////////////////////////////////////
	// More often used translations
	{"On", "On"},
	{"Off", "Off"},
	//{"OK", "OK"}, // Defined above
	//{"Cancel", "Cancel"}, // Defined above
	
	// Downloads Panel
	{"Downloads", "Downloads"},
	{"Disable downloads", "Disable downloads"},
	{"Download directory", "Download directory"},
	//{"Browse", "Browse"}, // Defined above
	{"Restart failed downloads", "Restart failed downloads"},
	{"Maximum number of retries", "Maximum number of retries"},
	{"Waittime after each try", "Waittime after each try"},
	{"minutes", "minutes"},
	{"Enable requesting of failed download files", "Enable requesting of failed download files"},
	{"Request file after this count of retries", "Request file after this count of retries"},
	{"Number of simultaneous downloads", "Number of simultaneous downloads"},
	{"Number of splitfile threads", "Number of splitfile threads"},
	{"Remove finished downloads every 5 minutes", "Remove finished downloads every 5 minutes"},
	{"Try to download all segments, even if one fails", "Try to download all segments, even if one fails"},
	{"Decode each segment immediately after its download", "Decode each segment immediately after its download"},
	{"Select download directory", "Select download directory"},

	// Uploads Panel
	{"Disable uploads", "Disable uploads"},
	{"Automatic Indexing", "Automatic Indexing"},
	{"Share Downloads","Share Downloads"},
	{"Sign shared files", "Sign shared files"},
	{"Help spread files from people marked GOOD","Help spread files from people marked GOOD"},
	{"Upload HTL", "Upload HTL"},
	{"up htl explanation","(bigger is slower but more reliable)"},
	{"Number of simultaneous uploads", "Number of simultaneous uploads"},
	{"Number of splitfile threads", "Number of splitfile threads"},
	{"splitfile explanation","(bigger is faster but uses more cpu)"},
	{"Upload batch size","Upload batch size"},
	{"batch explanation", "bigger is faster but smaller is spam resistant"},
	{"Index file redundancy","Index file redundancy"},
	{"redundancy explanation", "not working yet"},

	// News (1) Panel
	{"Message upload HTL", "Message upload HTL"},
	{"Message download HTL", "Message download HTL"},
	{"Number of days to display", "Number of days to display"},
	{"Number of days to download backwards", "Number of days to download backwards"},
	{"Message base", "Message base"},
	{"Signature", "Signature"},
		
	// News (2) Panel
	{"Block messages with subject containing (separate by ';' )", "Block messages with subject containing (separate by ';' )"},
	{"Block messages with body containing (separate by ';' )", "Block messages with body containing (separate by ';' )"},
	{"Hide unsigned messages", "Hide unsigned messages"},
	{"Hide messages flagged BAD", "Hide messages flagged BAD"},
	{"Hide messages flagged CHECK", "Hide messages flagged CHECK"},
	{"Hide messages flagged N/A", "Hide messages flagged N/A"},
	{"Do spam detection", "Do spam detection"},
	{"Sample interval", "Sample interval"},
	{"hours", "hours"},
	{"Threshold of blocked messages", "Threshold of blocked messages"},
		
	// News (3) Panel
	{"Automatic update options", "Automatic update options"},
	{"Minimum update interval of a board", "Minimum update interval of a board"},
	//{"minutes", "minutes"}, // Defined above
	{"Number of concurrently updating boards", "Number of concurrently updating boards"},
	{"Show board update visualization", "Show board update visualization"},
	{"Choose background color if updating board is selected", "Choose background color if updating board is selected"},
	{"Choose background color if updating board is not selected", "Choose background color if updating board is not selected"},
	{"Choose updating color of SELECTED boards", "Choose updating color of SELECTED boards"},
	{"Choose updating color of NON-SELECTED boards", "Choose updating color of NON-SELECTED boards"},
	
    // Search Panel
	{"Image Extension", "Image Extension"},
	{"Video Extension", "Video Extension"},
	{"Archive Extension", "Archive Extension"},
	{"Document Extension", "Document Extension"},
	{"Audio Extension", "Audio Extension"},
	{"Executable Extension", "Executable Extension"},
	{"Maximum search results", "Maximum search results"},
	{"Hide files from people marked BAD","Hide files from people marked BAD"},
	{"Hide files from anonymous users","Hide files from anonymous users"},

	// Miscelaneous Panel
	{"Keyfile upload HTL", "Keyfile upload HTL"},
	{"Keyfile download HTL", "Keyfile download HTL"},
	{"list of nodes","Comma-separated list of nodes you have FCP access to"},
	{"Maximum number of keys to store", "Maximum number of keys to store"},
	{"Allow 2 byte characters", "Allow 2 byte characters"},
	{"Use editor for writing messages", "Use editor for writing messages"},
	{"Clean the keypool", "Clean the keypool"},
	{"Automatic saving interval", "Automatic saving interval"}, 	
	{"Disable splashscreen", "Disable splashscreen"}, 	

    // Display Panel
    {"EnableSkins", "Enable Skins"}, 	
	{"MessageBodyFont", "Message Body Font"}, 
	{"MoreSkinsAt", "You can get more skins at"},
	{"Preview","Preview"},
	{"RefreshList","Refresh List"},
	{"NoSkinsFound","No skins found!"},
	{"AvailableSkins","Available Skins"},

	///////////////////////////////////////////////////
	// Board Information Window
	///////////////////////////////////////////////////
	{"BoardInfoFrame.UpdateSelectedBoardButton","Update Selected Board"},
	{"BoardInfoFrame.Update","Update"},
	{"BoardInfoFrame.Update all boards","Update all boards"},
	{"BoardInfoFrame.Close","Close"},
	{"BoardInfoFrame.Board information window","Board information window"},
	{"BoardInfoFrame.Boards","Boards"},
	{"BoardInfoFrame.Messages","Messages"},
	{"BoardInfoFrame.Files","Files"},
	
	// Board information window table
	{"Messages","Messages"},

	///////////////////////////////////////////////////
	// List of known boards window
	///////////////////////////////////////////////////
	{"KnownBoardsFrame.List of known boards","List of known boards"},
	{"KnownBoardsFrame.Close","Close"},
	{"KnownBoardsFrame.Add board","Add board"},
	{"KnownBoardsFrame.Lookup","Lookup"},
	{"KnownBoardsFrame.Add board","Add board"},
	{"KnownBoardsFrame.Remove board","Remove board"}

    };
}

