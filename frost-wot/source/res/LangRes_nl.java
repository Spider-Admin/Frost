package res;
import java.util.*;

public class LangRes_nl extends ListResourceBundle {																			 
    public Object[][] getContents() { 
	return contents;
    }
    static final Object[][] contents = {

	// Tabbed pane
	{"Search", "Zoeken"},
	{"Downloads", "Downloads"},
	{"Uploads", "Uploads"},
	{"News", "Nieuws"},
	{"Status", "Status"},


	// Menues
	{"File", "Bestand"},
	{"Exit", "Afsluiten"},

	{"News", "Nieuws"},
	{"Automatic message update", "Berichten automatisch ophalen"},
 	{"Increase Font Size", "Vergroot lettertype"},
 	{"Decrease Font Size", "Verklein lettertype"},
	{"Configure selected board", "Stel geselecteerde forum in"},
	{"Display board information window", "Toon forum informatievenster"},

	{"Options", "Opties"},
	{"Preferences", "Voorkeuren"},

	{"Help", "Help"},
	{"About", "Info"},

	// Buttons
	{"Download", "Download"},
	{"Browse...", "Bladeren..."},
	{"New message", "Nieuw bericht"},
	{"Reply", "Beantwoorden"},
	{"Update", "Vernieuwen"},
	{"New board", "Nieuw forum"},
	{"Rename board", "Naam forum wijzigen"},
	{"Remove board", "Verwijder forum"},
	{"Cut board", "Forum knippen"},
	{"Copy board", "Forum kopieëren"},
	{"Paste board", "Forum plakken"},
	{"Configure board", "Forum instellen"},
	{"Save message", "Bericht opslaan"},
	{"Search all boards", "Doorzoek alle forums"},
	{"Download attachment(s)", "Download bijlage(n)"},

	// Check Boxes
	{"Automatic update", "Automatische vernieuwing"},
	{"Activate downloading", "Activeer downloaden"},
	{"Activate uploading", "Activeer uploaden"},

	// Popup Menues
	{"Download selected keys", "Download geselecteerde sleutels"},
	{"Download all keys", "Download alle sleutels"},
	{"Copy as attachment to clipboard", "Kopieër als bijlage naar forum"},
	{"Cancel", "Annuleren"},

	{"Remove selected downloads", "Verwijder geselecteerde downloads"},
	{"Remove all downloads", "Verwijder alle downloads"},
	{"Retry selected downloads", "Probeer geselecteerde downloads opnieuw"}, 
	{"Move selected downloads up", "Verplaats geselecteerde downloads omhoog"},
	{"Move selected downloads down", "Verplaats geselecteerde downloads omlaag"},

	{"Remove selected files", "Verwijder geselecteerde bestanden"},
	{"Remove all files", "Verwijder alle bestanden"},
	{"Move selected files up", "Verplaats geselecteerde bestanden omhoog"}, 
	{"Move selected files down", "Verplaats geselecteerde bestanden omlaag"}, 
	{"Reload selected files", "Herlaad geselecteerde Bestanden"}, 
	{"Reload all files", "Herlaad alle bestanden"}, 
	{"Set prefix for selected files", "Stel voorvoegsel voor geselecteerde bestanden in"},
	{"Set prefix for all files", "Stel voorvoegsel voor alle bestanden in"},
	{"Restore default filenames for selected files", "Herstel standaard bestandsnaam voor geselecteerde bestanden"},
	{"Restore default filenames for all files", "Herstel standaard bestandsnaam voor alle bestanden"},

	{"Save message to disk", "Bewaar bericht op schijf"},
	{"Download attachment(s)", "Dowload bijlage(n)"},

	{"Add new board / folder", "Voeg nieuw forum/map toe"},
	{"Remove selected board / folder", "Verwijder geselecteerde forum/map"},
	{"Copy selected board / folder", "Kopieër geselecteerde forum/map"},
	{"Cut selected board / folder", "Knip geselecteerde forum/map"},
	{"Paste board / folder", "Plak forum/map"},
	{"Configure selected board", "Stel selecteerde forum in"},
	{"Cancel", "Annuleren"},

	// Preferences
	{"Options", "Opties"},	
	{"OK", "OK"},
	{"On", "On"},
	{"Off", "Off"},
	{"Select download directory.", "Selecteer download map."},

	{"Download directory:", "Download map:"},
	{"Minimum HTL (5):", "Minimum HTL (5):"},
	{"Maximum HTL (25):", "Maximum HTL (25):"},
	{"Number of simultaneous downloads (3):", "Aantal gelijktijdige downloads (3):"},
	{"Number of splitfile threads (3):", "Aantal splitfile threads (3):"},
	{"Remove finished downloads every 5 minutes.(off)", "Verwijder voltooide downloads elke 5 minuten.(uit)"},

	{"Upload HTL (5):", "Upload HTL (5):"},
	{"Number of simultaneous uploads (3):", "Aantal gelijktijdige uploads (3):"},
	{"Number of splitfile threads (3):", "Aantal splitfile threads (3):"},
	{"Reload interval (8 days):", "Herlaad interval (8 dagen):"},

	{"Message upload HTL (5):", "Bericht upload HTL (5):"},
	{"Message download HTL(15):", "Bericht download HTL(15):"},
	{"Number of days to display (10):", "Aantal te tonen dagen (10):"},
	{"Number of days to download backwards (3):", "Aantal achteraf te downloaden dagen (3):"},

	{"Keyfile upload HTL (5):", "Sleutelbestand upload HTL (5):"},
	{"Keyfile download HTL (15):", "Sleutelbestand download HTL (15):"},
	{"Node address (127.0.0.1):", "Knooppunt adres (127.0.0.1):"},
	{"Node port (8481):", "Knooppunt poort (8481):"},
	{"Maximum number of keys to store (100000):", "Maximum aantal op te slaan sleutels (100000):"},
	{"Allow 2 byte characters", "Sta 2 byte characters toe"},

	{"Use skins, please restart Frost after changing this.(off)", "Gebruik skins, start Frost opnieuw op als dit veranderd is.(uit)"},

	// Preferences / Tabbed Pane
	{"Downloads", "Downloads"},
	{"Uploads", "Uploads"},
	{"Miscellaneous", "Divers"},
	{"Skins", "Skins"},
	{"News", "Nieuws"},

	// Search table
	{"Filename", "Bestandsnaam"},
	{"Size", "Grootte"},
	{"Age", "Leeftijd"},
	{"Key", "Sleutel"},
	{"Board", "Forum"},

	// Download table
	{"Status", "Status"},
	{"HTL", "HTL"},
	{"Trying", "Proberen"},
	{"Waiting", "Wachten"},
	{"Done", "Klaar"},
	{"Failed", "Mislukt"},
	{"Source", "Bron"},

	// Upload table
	{"Last upload", "Laatste upload"},
	{"Path", "Pad"},
	{"Never", "Nooit"},
	{"Uploading", "Uploaden"},
	{"Destination", "Bestemming"},

	// News table
	{"Index", "Index"},
	{"From", "Van"},
	{"Subject", "Onderwerp"},
	{"Date", "Datum"},

	// About box
	{"About", "Info"},

	// Board information frame
	{"Board information", "Forum informatie"},
	{"Add board", "Voeg forum toe"},
	{"Board", "Forum"},
	{"State", "Status"},
	{"Messages", "Berichten"},
	{"New messages", "Nieuw bericht"},
	{"Files", "Bestanden"},
	{"Update", "Vernieuwen"},
	
	// Board settings frame
	{"Generate new keypair", "Genereer nieuw sleutelpaar"},
	{"Public board", "Publiek forum"},
	{"Secure board", "Beveiligd forum"},
	{"Board settings for ", "Forum instelling voor "},
	{"Private key :", "Privé sleutel:"},
	{"Public key :", "Public key :"},
	{"Not available", "Niet beschikbaar"},

	// Message frame
	{"Send message", "Verstuur bericht"},
	{"Add attachment(s)", "Voeg bijlage(n) toe"},	
	{"Board: ", "Forum: "},	
	{"From: ", "Van: "},	
	{"Subject: ", "Onderwerp: "},	
	{"Create message", "Maak bericht aan"},	
	{"Choose file(s) / directory(s) to attach", "Kies toe te voegen bestand(en) / map(pen) "},	
	{"Do you want to enter a subject?", "Wilt u een onderwerp invoeren?"},
	{"No subject specified!", "Geen onderwerp ingevoerd!"},
	{"Yes", "Ja"},	
	{"No", "Nee"},	

	// Status bar
	{"Up: ", "Omhoog: "},	
	{"   Down: ", "   Omlaag: "},	
	{"   TOFUP: ", "   TOFUP: "},	
	{"   TOFDO: ", "   TOFDO: "},	
	{"   Results: ", "   Resultaten: "},	
	{"   Files: ", "   Bestanden: "},
	{"   Selected board: ", "   Geselecteerde forum: "},

	// Other 
	{"Frost by Jantho", "Frost door Jantho"},
	{"Copyright (c) 2001 Jan-Thomas Czornack", "Copyright (c) 2001 Jan-Thomas Czornack"},
	{"Select a message to view its content.", "Selecteer een bericht om de inhoud hiervan te zien."},
	{"Choose file(s) and/or directories to upload", "Kies bestand(en) en/of map(pen) om te uploaden"},
	{"Please enter the prefix you want to use for your files.", "Geef a.u.b. het voorvoegsel dat U wilt gebruiken voor deze bestanden."},
	{"Retry", "Opnieuw proberen"},
	{"I was not able to upload your message.", "Het is niet gelukt om het bericht te uploaden"},
	{"Upload of message failed", "Het uploaden van uw bericht is mislukt."},
	{"Invalid key.  Key must begin with one of", "Invalid key.  Key must begin with one of"},
	{"Invalid key", "Invalid key"},
	{"Name","Name"},
    }; 
} 


