package res;

import java.util.*;
public class LangRes_de extends ListResourceBundle {																			 
	
    public Object[][] getContents() { 
	return contents;
    }
    static final Object[][] contents = {

	// Welcome message
	{"Welcome message", "Bitte lesen!\n\nUm Frost benutzen zu können muss zuerst mindestens ein Forum ausgewählt werden. Du kannst eine Übersicht aller aktiven Foren im Forumsinformationsfenster bekommen (i-Button in der Button Leiste). Wenn Du Frost zum ersten mal startest werden wahrscheinlich noch keine Foren vorhanden sein. Nach ein paar Minuten sollten aber einige gefunden werden (auf aktualisieren drücken)."},

	// Tabbed pane
	{"Search", "Suchen"},
	{"Downloads", "Runterladen"},
	{"Uploads", "Hochladen"},
	{"News", "Nachrichten"},

	// Menues
	{"File", "Datei"},
	{"Exit", "Ende"},

	{"News", "Nachrichten"},
	{"Automatic message update", "Foren automatisch aktualisieren"},
 	{"Increase Font Size", "Grössere Schrift"},
 	{"Decrease Font Size", "Kleinere Schrift"},
	{"Configure selected board", "Aktuelles Forum konfigurieren"},
	{"Display board information window", "Forumsinformationen anzeigen"},

	{"Options", "Optionen"},
	{"Preferences", "Einstellungen"},

	{"Help", "Hilfe"},
	{"About", "Über"},

	// Buttons
	{"Download", "Herunterladen"},
	{"Browse...", "Durchsuchen..."},
	{"New message", "Neue Nachricht"},
	{"Reply", "Antworten"},
	{"Update", "Aktualisieren"},
	{"Add board", "Neues Forum hinzufügen"},
	{"Remove selected board", "Aktuelles Forum entfernen"},
	{"Download attachment(s)", "Attachments herunterladen"},
	{"New board", "Neues Forum einfügen"},
	{"Rename board", "Gewähltes Forum / Verzeichnis umbenennen"},
	{"Remove board", "Gewähltes Forum / Verzeichnis entfernen"},
	{"Cut board", "Gewähltes Forum / Verzeichnis ausschneiden"},
	{"Copy board", "Gewähltes Forum / Verzeichnis kopieren"},
	{"Paste board", "Clipboard einfügen"},
	{"Configure board", "Forum konfigurieren"},
	{"Save message", "Nachricht speichern"},
	{"Search all boards", "Alle Foren durchsuchen"},
	{"Minimize to System Tray", "In die Taskleiste minimieren"},

	// Check Boxes
	{"Automatic update", "Automatisches aktualisieren"},
	{"Activate downloading", "Herunterladen aktivieren"},
	{"Activate uploading", "Hochladen aktivieren"},

	// Popup Menues
	{"Download selected keys", "Gewählte Dateien herunterladen"},
	{"Download all keys", "Alle Dateien herunterladen"},
	{"Cancel", "Abbruch"},

	{"Remove selected downloads", "Ausgewählte Dateien entfernen"},
	{"Remove all downloads", "Alle Dateien entfernen"},
	{"Retry selected downloads", "Ausgewählte Dateien erneut herunterladen"}, 
	{"Move selected downloads up", "Gewählte Dateien an den Anfang der Liste bewegen"},
	{"Move selected downloads down", "Gewählte Dateien an das Ende der Liste bewegen"},

	{"Remove selected files", "Ausgewählte Dateien entfernen"},
	{"Remove all files", "Alle Dateien entfernen"},
	{"Move selected files up", "Gewählte Dateien an den Anfang der Liste bewegen"}, 
	{"Move selected files down", "Gewählte Dateien an das Ende der Liste bewegen"}, 
	{"Reload selected files", "Gewählte Dateien hochladen"}, 
	{"Reload all files", "Alle Dateien hochladen"}, 
	{"Set prefix for selected files", "Präfix für gewählte Dateien festlegen"},
	{"Set prefix for all files", "Präfix für alle Dateien festlegen"},
	{"Restore default filenames for selected files", "Standard Dateinamen für gewählte Dateien wieder herstellen"},
	{"Restore default filenames for all files", "Standard Dateinamen für alle Dateien wieder herstellen"},
	{"Change destination board", "Zielforum für gewählte Dateien ändern."},
	{"Add files to board", "Dateien zu Forum hinzufügen"},

	{"Save message to disk", "Nachricht speichern"},
	{"Download attachment(s)", "Attachments herunterladen"},

	{"Add new board / folder", "Neues Forum / Verzeichnis hinzufügen"},
	{"Remove selected board / folder", "Forum / Verzeichnis entfernen"},
	{"Copy selected board / folder", "Forum / Verzeichnis kopieren"},
	{"Cut selected board / folder", "Forum / Verzeichnis ausschneiden"},
	{"Paste board / folder", "Forum / Verzeichnis einfügen"},
	{"Configure selected board", "Forum konfigurieren"},
	{"Cancel", "Abbruch"},

	// Preferences
	{"Options", "Optionen"},	
	{"OK", "OK"},
	{"(On)", "(An)"},
	{"(Off)", "(Aus)"},
	{"Select download directory.", "Zielverzeichnis auswählen."},

	{"Download directory:", "Zielverzeichnis:"},
	{"Minimum HTL:", "Minimale HTL:"},
	{"Maximum HTL:", "Maximale HTL:"},

	{"Disable uploads", "Hochladen deaktivieren"},
	{"Disable downloads", "Herunterladen deaktivieren"},
	{"Show only signed messages", "Zeige nur signierte Nachrichten"},
	{"Show only GOOD messages", "Zeige nur Nachrichten mit GOOD-Flag"},
	{"Block message body containing:", "Blocke Nachrichten die enthalten:"},
	{"Block message from/subject containing:", "Blocke Nachrichten deren from/subject enthält:"},
	{"Do spam detection", "Spamerkennung an"},
	{"Sample interval (hours)", "Testinterval (Stunden)"},
	{"Threshold of blocked messages", "Schwellwert für zu blockierende Nachrichten"},
	{"Insert request if HTL tops:", "Aktive Anfrage senden wenn HTL größer:"},
	{"Clean the keypool", "Räume den 'keypool' auf"},

	{"Automatic update options", "Einstellungen zum automatischen Update"},
	{"Minimum update interval of a board (minutes) :", "Mindestdauer zwischen Board-Updates (Minuten) :"},
	{"Number of concurrently updating boards:", "Anzahl gleichzeitig aufzufrischender Boards:"},

	{"Number of simultaneous downloads:", "Gleichzeitge Downloads:"},
	{"Number of splitfile threads:", "Simultaner Splitfile Threads:"},
	{"Remove finished downloads every 5 minutes.", "Fertige Downloads nach 5 Minuten aus der Liste entfernen."},

	{"Upload HTL:", "Hochlade HTL:"},
	{"Number of simultaneous uploads:", "Gleichzeitige Uploads:"},
	{"Number of splitfile threads:", "Simultane Splitfile Threads:"},

	{"Message upload HTL:", "Hochlade HTL:"},
	{"Message download HTL:", "Herunterlade  HTL:"},
	{"Number of days to display:", "Nachrichtentage darstellen:"},
	{"Number of days to download backwards:", "Nachrichtentage herunterladen:"},
	{"Signature", "Signatur"},
	{"Message base:", "Nachrichten Basis:"},
	{"Block messages: ", "Blockiere Nachrichten: "},

	{"Keyfile upload HTL:", "Schlüssel hochlade HTL:"},
	{"Keyfile download HTL:", "Schlüssel herunterlade HTL:"},
	{"Node address:", "Node Adresse:"},
	{"Node port:", "Node Port:"},
	{"Maximum number of keys to store:", "Maximaler Schlüsselspeicher:"},
	{"Allow 2 byte characters", "2 Byte Zeichen erlauben"},

	{"Image Extension:", "Bild-Endung:"},
	{"Video Extension:", "Video-Endung:"},
	{"Archive Extension:", "Archiv-Endung:"},
	{"Audio Extension:", "Audio-Endung:"},
	{"Document Extension:", "Dokument-Endung:"},
	{"Executable Extension:", "Executable-Endung:"},
	{"Use editor for writing messages: ", "Verwende Editor für neue Nachrichten: "},

	// Preferences / Tabbed Pane
	{"Downloads", "Herunterladen"},
	{"Uploads", "Hochladen"},
	{"Miscellaneous", "Verschiedenes"},
	{"Skins", "Texturen"},

	// Search table
	{"Filename", "Dateiname"},
	{"Size", "Größe"},
	{"Age", "Alter"},
	{"Key", "Schlüssel"},
	{"Board", "Forum"},
	
	// Download table
	{"State", "Status"},
	{"HTL", "HTL"},
	{"Trying", "Anfrage"},
	{"Waiting", "Warte"},
	{"Done", "Erledigt"},
	{"Failed", "Fehler"},
	{"Source", "Quelle"},

	// Upload table
	{"Last upload", "Hochgeladen am"},
	{"Path", "Pfad"},
	{"Never", "Nie"},
	{"Uploading", "Aktiv"},
	{"Destination", "Ziel"},
	{"Unknown", "Unbekannt"},
	{"Requested", "Gesucht"},

	// News table
	{"Index", "Index"},
	{"From", "Von"},
	{"Subject", "Betreff"},
	{"Date", "Datum"},
	
	// About box
	{"About", "Über"},
	{"Copyright (c) 2001 Jan-Thomas Czornack", "Copyright (c) 2001 Jan-Thomas Czornack"},

	// Board information frame
	{"Board information", "Forum Informationen"},
	{"BoardInfoFrame.UpdateSelectedBoardButton", "Ausgewähltes Forum aktualisieren"},
	{"Add board", "Forum abonnieren"},
	{"Board", "Forum"},
	{"State", "Status"},
	{"Messages", "Nachrichten"},
	{"New messages", "Neue Nachrichten"},
	{"Files", "Dateien"},
	{"Update", "Aktualisieren"},

	// Board settings frame
	{"Generate new keypair", "Neues Schlüsselpaar erstellen"},
	{"Public board", "Öffentliches Forum"},
	{"Secure board", "Gesichertes Forum"},
	{"Board settings for ", "Forumeinstellungen für "},
	{"Private key :", "Privater Schlüssel :"},
	{"Public key :", "Öffentlicher Schlüssel :"},
	{"Not available", "Nicht vorhanden"},
	{"Boards: ", "Foren: "},
	{"   Messages: ", "   Nachrichten: "},
	{"   Files: ", "   Dateien: "},

	// Message frame
	{"Send message", "Nachricht senden"},
	{"Add attachment(s)", "Datei(en) anhängen"},	
	{"Board: ", "Forum: "},	
	{"From: ", "Von: "},	
	{"Subject: ", "Betreff: "},
	{"Create message", "Nachricht erstellen"},	
	{"Choose file(s) / directory(s) to attach", "Dateien oder Verzeichnisse auswählen."},	
	{"Do you want to enter a subject?", "Wollen sie einen Betreff eingeben?"},
	{"No subject specified!", "Sie haben keinen Betreff angegeben!"},
	{"Yes", "Ja"},	
	{"No", "Nein"},	

	// Status bar
	{"Up: ", "Up: "},	
	{"   Down: ", "   Down: "},	
	{"   TOFUP: ", "   TOFUP: "},	
	{"   TOFDO: ", "   TOFDO: "},	
	{"   Results: ", "   Resultate: "},	
	{"   Files: ", "   Dateien: "},	
	{"   Selected board: ", "   Ausgewähltes Forum: "},

	// New node dialog window
	{"New Node Name","Name des neuen Forums"},
	{"New Folder","Neues Forum"},

	// Other 
	{"Frost by Jantho", "Frost von Jantho"},
	{"Select a message to view its content.", "Bitte eine Nachricht auswählen um ihren Inhalt zu sehen."},
	{"Please enter the prefix you want to use for your files.", "Bitte gewünschtes Stichwort eingeben."},
	{"Retry", "Erneut versuchen"},
	{"I was not able to upload your message.", "Die Nachricht konnte nicht hochgeladen werden."},
	{"Upload of message failed", "Hochladen der Nachricht fehlgeschlagen"},

	{"Do you want to add this board to the public boardlist?\n\n","Willst Du dieses Forum zur öffentlichen Forumsliste hinzufügen?\n\n"},
	{"ATTENTION: If you choose yes EVERY Frost user will\n","ACHTUNG: Wenn Du zustimmst wird JEDER Frost Benutzer\n"},
	{"know about this board AND he will be able to read and\n","dieses Forum lesen und Nachrichten an dieses Forum\n"},
	{"write messages on this board.","schicken können."},
	{" announcement."," Bekanntmachung."},
    }; 
} 
