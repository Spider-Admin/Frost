package res;

import java.util.ListResourceBundle;
public class LangRes extends ListResourceBundle {

    public Object[][] getContents() {
    return contents;
    }
    static final Object[][] contents = {

    // Welcome message
    {"Welcome message", "Please read this!\n\nTo use Frost, you first need to select a topic from the board information window. You can openthis window with the i-button above. You can send messages and files to people using the same board. It will probably take some time until the first boards show up (press the update button)."},

    // Tabbed pane
    {"Search", "Search"},
    {"Downloads", "Downloads"},
    {"Uploads", "Uploads"},
    {"News", "News"},
    {"Status", "Status"},

    //About box
    {"Version 20030126", "Version 20030126"},
    {"Open Source Project", "Open Source Project"},
    // Menus
    {"File", "File"},
    {"Exit", "Exit"},

    {"News", "News"},
    {"Automatic message update", "Automatic board update"},
    {"Increase Font Size", "Increase Font Size"},
    {"Decrease Font Size", "Decrease Font Size"},
    {"Configure selected board", "Configure selected board"},
    {"Display board information window", "Display board information window"},

    {"Options", "Options"},
    {"Preferences", "Preferences"},

    {"Help", "Help"},
    {"About", "About"},

    // Buttons
    {"Download", "Download"},
    {"Browse...", "Browse..."},
    {"New message", "New message"},
    {"Reply", "Reply"},
    {"Update", "Update"},
    {"No spam mode", "No spam mode"},
    {"New board", "New board"},
    {"Rename folder", "Rename folder"},
    {"Remove board", "Remove board"},
    {"Cut board", "Cut board"},
    {"Copy board", "Copy board"},
    {"Paste board", "Paste board"},
    {"Board Information Window", "Board Information Window"},
    {"Configure board", "Configure board"},
    {"Save message", "Save message"},
    {"Search all boards", "Search all boards"},
    {"Minimize to System Tray", "Minimize to System Tray"},

    // Check Boxes
    {"Automatic update", "Automatic update"},
    {"Activate downloading", "Activate downloading"},
    {"Activate uploading", "Activate uploading"},

    // Popup Menues
    {"Download selected keys", "Download selected keys"},
    {"Download all keys", "Download all keys"},
    {"Copy as attachment to clipboard", "Copy as attachment to clipboard"},
    {"help user (sets to GOOD)","help user (sets to GOOD)"}, //at the moment these two neither set GOOD
    {"block user (sets to BAD)","block user (sets to BAD)"}, //nor BAD.  how lame
    {"Cancel", "Cancel"},

    {"Remove selected downloads", "Remove selected downloads"},
    {"Remove all downloads", "Remove all downloads"},
    {"Retry selected downloads", "Retry selected downloads"},
    {"Move selected downloads up", "Move selected downloads up"},
    {"Move selected downloads down", "Move selected downloads down"},

    {"Remove selected files", "Remove selected files"},
    {"Remove all files", "Remove all files"},
    {"Move selected files up", "Move selected files up"},
    {"Move selected files down", "Move selected files down"},
    {"Reload selected files", "Upload selected files"},
    {"Reload all files", "Upload all files"},
    {"Set prefix for selected files", "Set prefix for selected files"},
    {"Set prefix for all files", "Set prefix for all files"},
    {"Restore default filenames for selected files", "Restore default filenames for selected files"},
    {"Restore default filenames for all files", "Restore default filenames for all files"},
    {"Change destination board", "Change destination board"},
    {"Add files to board", "Add files to board"},

    {"Save message to disk", "Save message to disk"},
    {"Download attachment(s)", "Download attachment(s)"},
    {"Add Board(s)","Add Board(s)"},

    {"Add new board / folder", "Add new board / folder"},
    {"Remove selected board / folder", "Remove selected board / folder"},
    {"Copy selected board / folder", "Copy selected board / folder"},
    {"Cut selected board / folder", "Cut selected board / folder"},
    {"Paste board / folder", "Paste board / folder"},
    {"Configure selected board", "Configure selected board"},
    {"Cancel", "Cancel"},

    // Preferences
    {"Options", "Options"},
    {"OK", "OK"},
    {"(On)", "(On)"},
    {"(Off)", "(Off)"},
    {"Select download directory.", "Select download directory."},

    {"Download directory:", "Download directory:"},
    {"Minimum HTL:", "Minimum HTL:"},
    {"Maximum HTL:", "Maximum HTL:"},

    {"Disable uploads", "Disable uploads"},
    {"Disable downloads", "Disable downloads"},
    {"Show only signed messages", "Show only signed messages"},
    //{"Show only GOOD messages", "Show only GOOD messages"},
    {"Hide messages flagged BAD", "Hide messages flagged BAD"},
    {"Hide messages flagged CHECK", "Hide messages flagged CHECK"},
    {"Block message body containing:", "Block message body containing:"},
    {"Block message from/subject containing:", "Block message from/subject containing:"},
    {"Do spam detection", "Do spam detection"},
    {"Sample interval (hours)", "Sample interval (hours)"},
    {"Threshold of blocked messages", "Threshold of blocked messages"},
    {"Insert request if HTL tops:", "Insert request if HTL tops:"},
    {"Clean the keypool", "Clean the keypool"},

    {"Automatic update options", "Automatic update options"},
    {"Minimum update interval of a board (minutes) :", "Minimum update interval of a board (minutes) :"},
    {"Number of concurrently updating boards:", "Number of concurrently updating boards:"},

    {"Number of simultaneous downloads:", "Number of simultaneous downloads:"},
    {"Number of splitfile threads:", "Number of splitfile threads:"},
    {"Remove finished downloads every 5 minutes.", "Remove finished downloads every 5 minutes."},

    {"Upload HTL:", "Upload HTL:"},
    {"up htl explanation","( bigger is slower but more reliable )"},
    {"Sign shared files", "Sign shared files"},
    {"Hide files from people marked BAD","Hide files from people marked BAD"},
    {"Hide files from anonymous users","Hide files from anonymous users"},
    {"Upload batch size","Upload batch size"},
    {"Index file redundancy","Index file redundancy"},
    {"batch explanation", "( bigger is faster but smaller is spam resistant )"},
    {"redundancy explanation", "not working yet"},
    {"Help spread files from people marked GOOD","Help spread files from people marked GOOD"},
    {"Number of simultaneous uploads:", "Number of simultaneous uploads:"},
    {"Number of splitfile threads:", "Number of splitfile threads:"},
    {"splitfile explanation","( bigger is faster but uses more cpu )"},

    {"Message upload HTL:", "Message upload HTL:"},
    {"Message download HTL:", "Message download HTL:"},
    {"Number of days to display:", "Number of days to display:"},
    {"Number of days to download backwards:", "Number of days to download backwards:"},
    {"Signature", "Signature"},
    {"Message base:", "Message base:"},
    {"Block messages: ", "Block messages: "},

    {"Keyfile upload HTL:", "Keyfile upload HTL:"},
    {"Keyfile download HTL:", "Keyfile download HTL:"},
    {"Node address:", "Node address:"},
    {"Node port:", "Node port:"},
    {"list of nodes","Comma-separated list of nodes you have FCP access to:"},
    {"Maximum number of keys to store:", "Maximum number of keys to store:"},
    {"Allow 2 byte characters", "Allow 2 byte characters"},

    {"Image Extension:", "Image Extension:"},
    {"Video Extension:", "Video Extension:"},
    {"Archive Extension:", "Archive Extension:"},
    {"Audio Extension:", "Audio Extension:"},
    {"Document Extension:", "Document Extension:"},
    {"Executable Extension:", "Executable Extension:"},
    {"Use editor for writing messages: ", "Use editor for writing messages: "},
    
    //Preferences. Display panel
    {"EnableSkins", "Enable Skins"}, 	
	{"MessageBodyFont", "Message Body Font"}, 

    // Preferences / Tabbed Pane
    {"Downloads", "Downloads"},
    {"Uploads", "Uploads"},
    {"Miscellaneous", "Miscellaneous"},
    {"Skins", "Skins"},
    {"News", "News"},
	{"Display", "Display"},

    // Search table
    {"Filename", "Filename"},
    {"Size", "Size"},
    {"Age", "Age"},
    {"Key", "Key"},
    {"Board", "Board"},

    // Download table
    {"State", "Status"},
    {"HTL", "HTL"},
    {"Trying", "Trying"},
    {"Waiting", "Waiting"},
    {"Done", "Done"},
    {"Failed", "Failed"},
    {"Source", "Source"},

    // Upload table
    {"Last upload", "Last upload"},
    {"Path", "Path"},
    {"Never", "Never"},
    {"Uploading", "Uploading"},
    {"Destination", "Destination"},
    {"Unknown", "Unknown"},
    {"Requested", "Requested"},

    // News table
    {"Index", "Index"},
    {"From", "From"},
    {"Subject", "Subject"},
    {"Date", "Date"},

    // About box
    {"About", "About"},
    {"Copyright (c) 2001 Jan-Thomas Czornack", "Copyright (c) 2001 Jan-Thomas Czornack"},

    // Board information frame
    {"Board information", "Board information"},
    {"BoardInfoFrame.UpdateSelectedBoardButton", "Update selected Board"},
    {"Add board", "Add board"},
    {"Board", "Board"},
    {"State", "State"},
    {"Messages", "Messages"},
    {"New messages", "New messages"},
    {"Files", "Files"},
    {"Update", "Update"},
    {"Boards: ", "Boards: "},
    {"   Messages: ", "   Messages: "},
    {"   Files: ", "   Files: "},
    {"Add more boards","Add more boards"},

    // Board settings frame
    {"Generate new keypair", "Generate new keypair"},
    {"Public board", "Public board"},
    {"Secure board", "Secure board"},
    {"Board settings for ", "Board settings for "},
    {"Private key :", "Private key :"},
    {"Public key :", "Public key :"},
    {"Not available", "Not available"},

    // Message frame
    {"Send message", "Send message"},
    {"Add attachment(s)", "Add attachment(s)"},
    {"Board: ", "Board: "},
    {"From: ", "From: "},
    {"Subject: ", "Subject: "},
    {"Create message", "Create message"},
    {"Choose file(s) / directory(s) to attach", "Choose file(s) / directory(s) to attach"},
    {"Do you want to enter a subject?", "Do you want to enter a subject?"},
    {"No subject specified!", "No subject specified!"},
    {"Yes", "Yes"},
    {"No", "No"},
    {"Add public key to this message", "Add public key to this message"},

    // Status bar
    {"Up: ", "Up: "},
    {"   Down: ", "   Down: "},
    {"   TOFUP: ", "   TOFUP: "},
    {"   TOFDO: ", "   TOFDO: "},
    {"   Results: ", "   Results: "},
    {"   Files: ", "   Files: "},
    {"   Selected board: ", "   Selected board: "},

    // New node dialog window
    {"New Node Name","New board name"},
    {"New Folder","New Folder"},

	//Skin chooser
	{"AvailableSkins", "Available Skins"},
	{"Preview", "Preview"},
	{"RefreshList", "Refresh List"},
	{"NoSkinsFound", "No Skins Found"},

    // Other
    {"Frost by Jantho", "Frost by Jantho"},
    {"Select a message to view its content.", "Select a message to view its content."},
    {"Choose file(s) and/or directories to upload", "Choose file(s) and/or directories to upload"},
    {"Please enter the prefix you want to use for your files.", "Please enter the prefix you want to use for your files."},
    {"Retry", "Retry"},
    {"I was not able to upload your message.", "I was not able to upload your message."},
    {"Upload of message failed", "Upload of message failed"},
    {"Invalid key.  Key must begin with one of", "Invalid key.  Key must begin with one of"},
    {"Invalid key", "Invalid key"},
    {"Name","Name"},
    {"Trust","Trust"},
    {"Do not trust","Do not trust"},

    {"Do you want to add this board to the public boardlist?\n\n","Do you want to add this board to the public boardlist?\n\n"},
    {"ATTENTION: If you choose yes EVERY Frost user will\n","ATTENTION: If you choose yes EVERY Frost user will\n"},
    {"know about this board AND he will be able to read and\n","know about this board AND he will be able to read and\n"},
    {"write messages on this board.","write messages on this board."},
    {" announcement."," announcement."},
    };
}

