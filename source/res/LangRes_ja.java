package res;

import java.util.*;
public class LangRes_ja extends ListResourceBundle {																			 
	
    public Object[][] getContents() { 
	return contents;
    }
    static final Object[][] contents = {
	// Tabbed pane
	{"Search", "\u691c\u7d22"},
	{"Downloads", "\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9"},
	{"Uploads", "\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9"},
	{"News", "\u30cb\u30e5\u30fc\u30b9"},

	// Menues
	{"File", "\u30d5\u30a1\u30a4\u30eb"},
	{"Exit", "\u7d42\u4e86"},
	{"News", "\u30cb\u30e5\u30fc\u30b9"},
	{"Force update of selected board", "\u677f\u306e\u5f37\u5236\u66f4\u65b0"},
	{"Options", "\u30aa\u30d7\u30b7\u30e7\u30f3"},
	{"Preferences", "\u8a2d\u5b9a"},
	{"Help", "\u30d8\u30eb\u30d7"},
	{"About", "Frost\u306b\u3064\u3044\u3066"},

	// Buttons
	{"Download", "\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9"},
	{"Browse...", "\u53c2\u7167..."},
	{"New message", "\u6295\u7a3f"},
	{"Reply", "\u8fd4\u4fe1"},
	{"Update", "\u66f4\u65b0"},
	{"Add board", "\u677f\u306e\u8ffd\u52a0"},
	{"Remove selected board", "\u677f\u3092\u30ea\u30b9\u30c8\u304b\u3089\u524a\u9664"},
	{"Download attachment(s)", "\u6dfb\u4ed8\u30d5\u30a1\u30a4\u30eb\u306e\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9"},

	// Check Boxes
	{"Automatic update", "\u81ea\u52d5\u66f4\u65b0"},
	{"Activate downloading", "\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9\u306e\u6709\u52b9"},
	{"Activate uploading", "\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u306e\u6709\u52b9"},

	// Popup Menues
	{"Download selected keys", "\u3053\u306ekey\u3092\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9"},
	{"Download all keys", "\u5168\u3066\u306ekey\u3092\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9"},
	{"Cancel", "\u30ad\u30e3\u30f3\u30bb\u30eb"},

	{"Remove selected downloads", "\u9078\u629e\u9805\u76ee\u3092\u30ea\u30b9\u30c8\u304b\u3089\u524a\u9664"},
	{"Remove all downloads", "\u5168\u9805\u76ee\u3092\u30ea\u30b9\u30c8\u304b\u3089\u524a\u9664"},
	{"Retry selected downloads", "\u9078\u629e\u9805\u76ee\u306e\u518d\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9"}, 
	{"Move selected downloads up", "\u30ea\u30b9\u30c8\u306e\u4e00\u756a\u4e0a\u3078"},
	{"Move selected downloads down", "\u30ea\u30b9\u30c8\u306e\u4e00\u756a\u4e0b\u3078"},

	{"Remove selected files", "\u9078\u629e\u9805\u76ee\u3092\u30ea\u30b9\u30c8\u304b\u3089\u524a\u9664"},
	{"Remove all files", "\u5168\u9805\u76ee\u3092\u30ea\u30b9\u30c8\u304b\u3089\u524a\u9664"},
	{"Move selected files up", "\u30ea\u30b9\u30c8\u306e\u4e00\u756a\u4e0a\u3078"}, 
	{"Move selected files down", "\u30ea\u30b9\u30c8\u306e\u4e00\u756a\u4e0b\u3078"}, 
	{"Reload selected files", "\u9078\u629e\u9805\u76ee\u306e\u518d\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9"}, 
	{"Reload all files", "\u5168\u9805\u76ee\u306e\u518d\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9"}, 
	{"Set prefix for selected files", "\u9078\u629e\u9805\u76ee\u306e\u540d\u524d\u306b\u63a5\u982d\u8a9e\u3092\u3064\u3051\u308b"},
	{"Set prefix for all files", "\u5168\u9805\u76ee\u306e\u540d\u524d\u306b\u63a5\u982d\u8a9e\u3092\u3064\u3051\u308b"},
	{"Restore default filenames for selected files", "\u9078\u629e\u9805\u76ee\u306e\u540d\u524d\u3092\u5143\u306b\u623b\u3059"},
	{"Restore default filenames for all files", "\u5168\u9805\u76ee\u306e\u540d\u524d\u3092\u5143\u306b\u623b\u3059"},

	{"Save message to disk", "\u3053\u306e\u6295\u7a3f\u3092\u30c7\u30a3\u30b9\u30af\u306b\u4fdd\u5b58"},

	// Preferences
	{"Options", "\u30aa\u30d7\u30b7\u30e7\u30f3"},	
	{"OK", "OK"},
	{"On", "On"},
	{"Off", "Off"},
	{"Select download directory.", "\u4fdd\u5b58\u3059\u308b\u30d5\u30a9\u30eb\u30c0\u3092\u9078\u629e"},

	{"Download directory:", "\u4fdd\u5b58\u3059\u308b\u30d5\u30a9\u30eb\u30c0:"},
	{"Minimum HTL (5):", "\u6700\u5c0fHTL (5):"},
	{"Maximum HTL (25):", "\u6700\u5927HTL (25):"},
	{"Number of simultaneous downloads (3):", "\u540c\u6642\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9\u6570 (3):"},
	{"Number of splitfile threads (3):", "splitfile \u30b9\u30ec\u30c3\u30c9\u306e\u6570 (3):"},
	{"Remove finished downloads every 5 minutes.(off)", "\u5b8c\u4e86\u3057\u305f\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9\u30925\u5206\u304a\u304d\u306b\u30ea\u30b9\u30c8\u304b\u3089\u53d6\u308a\u9664\u304f(\u30aa\u30d5)"},

	{"Upload HTL (5):", "\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9HTL (5):"},
	{"Number of simultaneous uploads (3):", "\u540c\u6642\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u6570 (3):"},
	{"Number of splitfile threads (3):", "splitfile\u30b9\u30ec\u30c3\u30c9\u306e\u6570 (3):"},
	{"Reload interval (8 days):", "\u518d\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u3059\u308b\u9593\u9694 (8\u65e5):"},
	
	{"Message upload HTL (5):", "\u6295\u7a3f\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9HTL (5):"},
	{"Message download HTL(15):", "\u6295\u7a3f\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9HTL (15):"},
	{"Number of days to display (10):", "\u30ea\u30b9\u30c8\u306b\u8868\u793a\u3059\u308b\u65e5\u6570 (10):"},
	{"Number of days to download backwards (3):", "\u3055\u304b\u306e\u307c\u3063\u3066\u6295\u7a3f\u3092\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9\u3059\u308b\u65e5\u6570 (3):"},
	
	{"Keypool path:", "Key\u30d7\u30fc\u30eb\u306e\u30d1\u30b9:"},
	{"Keyfile upload HTL (5):", "Key\u30d5\u30a1\u30a4\u30eb\u306e\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9HTL (5):"},
	{"Keyfile download HTL (15):", "Key\u30d5\u30a1\u30a4\u30eb\u306e\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9HTL (15):"},
	{"Node address (127.0.0.1):", "\u30ce\u30fc\u30c9\u306e\u30a2\u30c9\u30ec\u30b9 (127.0.0.1):"},
	{"Node port (8481):", "\u30dd\u30fc\u30c8 (8481):"},
	{"Maximum number of keys to store (100000):", "\u4fdd\u5b58\u3059\u308bkey\u306e\u6700\u5927\u6570 (100000):"},
	
	{"Use skins, please restart Frost after changing this.(off)", "\u30b9\u30ad\u30f3\u3092\u4f7f\u7528\u3059\u308b(Frost\u306e\u518d\u8d77\u52d5\u304c\u5fc5\u8981) (\u30aa\u30d5)"},
	{"Image Extension:", "Image Extension:"},
	{"Video Extension:", "Video Extension:"},
	{"Archive Extension:", "Archive Extension:"},
	{"Audio Extension:", "Audio Extension:"},
	{"Document Extension:", "Document Extension:"},
	{"Executable Extension:", "Executable Extension:"},
	{"Show time in titlebar", "Show Time in titlebar"},
	{"Show time", "Show time"},

	// Preferences / Tabbed Pane
	{"Downloads", "\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9"},
	{"Uploads", "\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9"},
	{"Miscellaneous", "\u3044\u308d\u3044\u308d\u306a\u8a2d\u5b9a"},
	{"Skins", "\u30b9\u30ad\u30f3"},
	{"News", "\u30cb\u30e5\u30fc\u30b9"},

	// Search table
	{"Filename", "\u30d5\u30a1\u30a4\u30eb\u540d"},
	{"Size", "\u30b5\u30a4\u30ba"},
	{"Age", "\u66f4\u65b0\u65e5"},
	{"Key", "Key"},
	
	// Download table
	{"Status", "\u72b6\u6cc1"},
	{"HTL", "HTL"},
	{"Trying", "\u958b\u59cb"},
	{"Waiting", "\u5f85\u6a5f\u4e2d"},
	{"Done", "\u5b8c\u4e86"},
	{"Failed", "\u5931\u6557"},

	// Upload table
	{"Last upload", "\u66f4\u65b0\u65e5"},
	{"Path", "\u30d1\u30b9"},
	{"Never", "\u672a\u9001\u4fe1"},
	{"Uploading", "\u9001\u4fe1\u4e2d"},
	
	// News table
	{"Index", "Index"},
	{"From", "\u6295\u7a3f\u8005"},
	{"Subject", "\u984c\u540d"},
	{"Date", "\u65e5\u4ed8"},
	
	// About box
	{"About", "Frost \u306b\u3064\u3044\u3066"},
	
	// Board settings frame
	{"Generate new keypair", "\u6697\u53f7\u9375\u306e\u4f5c\u6210"},
	{"Public board", "\u81ea\u7531\u306a\u6295\u7a3f\u3092\u8a31\u53ef\u3059\u308b"},
	{"Secure board", "\u6697\u53f7\u9375\u3067\u6295\u7a3f\u3092\u5236\u9650\u3059\u308b"},
	{"Board settings for ", "\u677f\u306e\u8a2d\u5b9a: "},
	{"Private key :", "\u79d8\u5bc6\u9375:"},
	{"Public key :", "\u516c\u958b\u9375:"},
	{"Not available", "\u5229\u7528\u4e0d\u53ef"},
	{"Configure selected board", "\u677f\u306e\u8a2d\u5b9a"},

	// Message frame
	{"Send message", "\u6295\u7a3f"},
	{"Add attachment(s)", "\u30d5\u30a1\u30a4\u30eb\u306e\u6dfb\u4ed8"},	
	{"Board: ", "\u677f: "},	
	{"From: ", "\u6295\u7a3f\u8005: "},	
	{"Subject: ", "\u984c\u540d: "},	
	{"No subject", "\u7121\u984c"},	
	{"Create message", "\u30e1\u30c3\u30bb\u30fc\u30b8\u306e\u4f5c\u6210"},	
	{"Choose file(s) / directory(s) to attach", "\u6dfb\u4ed8\u3059\u308b\u30d5\u30a1\u30a4\u30eb\u307e\u305f\u306f\u30c7\u30a3\u30ec\u30af\u30c8\u30ea\u3092\u9078\u629e"},	
	{"Do you want to enter a subject?", "\u984c\u540d\u3092\u6c7a\u3081\u307e\u3059\u304b?"},
	{"No subject specified!", "\u984c\u540d\u304c\u300c\u7121\u984c\u300d\u306e\u307e\u307e\u3067\u3059"},
	{"Yes", "\u306f\u3044"},	
	{"No", "\u3044\u3044\u3048"},	

	// Status bar
	{"Up: ", "Up: "},	
	{"   Down: ", "   Down: "},	
	{"   TOFUP: ", "   \u6295\u7a3fUP: "},	
	{"   TOFDO: ", "   \u6295\u7a3fDOWN: "},	
	{"   Results: ", "   \u691c\u7d22\u7d50\u679c: "},	
	{"   Files: ", "   \u7dcf\u30d5\u30a1\u30a4\u30eb\u6570: "},	

	// Other 
	{"Anonymous", "\u540d\u7121\u3057\u3055\u3093"},
	{"Frost by Jantho", "Frost by Jantho"},
	{"Copyright (c) 2001 Jan-Thomas Czornack", "Copyright (c) 2001 Jan-Thomas Czornack"},
	{"Select a message to view its content.", "\u898b\u305f\u3044\u6295\u7a3f\u3092\u9078\u3093\u3067\u304f\u3060\u3055\u3044"},
	{"Choose file(s) and/or directories to upload", "\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u3059\u308b\u30d5\u30a1\u30a4\u30eb\u307e\u305f\u306f\u30c7\u30a3\u30ec\u30af\u30c8\u30ea\u3092\u9078\u629e"},
	{"Please enter the prefix you want to use for your files.", "\u30d5\u30a1\u30a4\u30eb\u540d\u306e\u982d\u306b\u3064\u3051\u305f\u3044\u8a00\u8449\u3092\u5165\u529b\u3057\u3066\u304f\u3060\u3055\u3044\u3002"},
	{"Retry", "\u518d\u8a66\u884c"},
	{"I was not able to upload your message.", "\u6295\u7a3f\u3092\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u3059\u308b\u3053\u3068\u304c\u51fa\u6765\u307e\u305b\u3093\u3067\u3057\u305f\u3002"},
	{"Upload of message failed", "\u6295\u7a3f\u306e\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u306b\u5931\u6557\u3057\u307e\u3057\u305f\u3002"},

    }; 
} 
