package res;

import java.util.ListResourceBundle;
public class LangRes_it extends ListResourceBundle {

    public Object[][] getContents() {
    return contents;
    }
    static final Object[][] contents = {

    // Welcome message
    {"Welcome message", "Per favore leggi!\n\nPer usare Frost, devi prima selezionare un soggetto dalla finestra informazioni board. Puoi aprire questa finestra tramite il tasto 'I' sopra. Puoi inviare messaggi e files a persone usando la stessa board. Probabilmente ci vorrà un po' di tempo prima di vedere i messaggi (premi il tasto aggiorna)."},

    // Tabbed pane
    {"Search", "Cerca"},
    {"Downloads", "Downloads"},
    {"Uploads", "Uploads"},
    {"News", "News"},
    {"Status", "Status"},

    //About box
    {"Version 20030126", "Version 20030126"},
    {"Open Source Project", "Open Source Project"},
    // Menus
    {"File", "File"},
    {"Exit", "Esci"},

    {"News", "News"},
    {"Automatic message update", "Autoaggiornamento board"},
    {"Increase Font Size", "Ingrandisci carattere"},
    {"Decrease Font Size", "Riduci carattere"},
    {"Configure selected board", "Configura la board selezionato"},
    {"Display board information window", "Visualizza finestra informazioni board"},

    {"Options", "Opzioni"},
    {"Preferences", "Preferenze"},

    {"Help", "Aiuto"},
    {"About", "Informazioni"},

    // Buttons
    {"Download", "Download"},
    {"Browse...", "Sfoglia..."},
    {"New message", "Nuovo messaggio"},
    {"Reply", "Rispondi"},
    {"Update", "Aggiorna"},
    {"No spam mode", "Modalità antispam"},
    {"New board", "Nuova board"},
    {"Rename folder", "Rinomina cartella"},
    {"Remove board", "Elimina board"},
    {"Cut board", "Taglia board"},
    {"Copy board", "Copia board"},
    {"Paste board", "Incolla board"},
    {"Board Information Window", "Finestra informazioni board"},
    {"Configure board", "Configura board"},
    {"Save message", "Salva messaggio"},
    {"Search all boards", "Cerca su tutte le board"},
    {"Minimize to System Tray", "Minimizza nella System Tray"},

    // Check Boxes
    {"Automatic update", "Autoaggiornamento board"},
    {"Activate downloading", "Attiva downloads"},
    {"Activate uploading", "Attiva uploads"},

    // Popup Menues
    {"Download selected keys", "Download files selezionati"},
    {"Download all keys", "Download tutti i files"},
    {"Copy as attachment to clipboard", "Copia come allegato"},
    {"Cancel", "Annulla"},

    {"Remove selected downloads", "Elimina i downloads selezionati"},
    {"Remove all downloads", "Elimina tutti i downloads"},
    {"Retry selected downloads", "Ritenta i downloads selezionati"},
    {"Move selected downloads up", "Muovi i downloads selezionati SU"},
    {"Move selected downloads down", "Muovi i downloads selezionati GIU"},

    {"Remove selected files", "Elimina i files selezionati"},
    {"Remove all files", "Elimina tutti i files"},
    {"Move selected files up", "Muovi i files selezionati SU"},
    {"Move selected files down", "Muovi i files selezionati GIU"},
    {"Reload selected files", "Ricarica i files selezionati"},
    {"Reload all files", "Ricarica tutti i files"},
    {"Set prefix for selected files", "Imposta prefisso per i files selezionati"},
    {"Set prefix for all files", "Imposta prefisso per tutti i files"},
    {"Restore default filenames for selected files", "Ripristina nome originale dei files selezionati"},
    {"Restore default filenames for all files", "Ripristina nome originale di tutti i files"},
    {"Change destination board", "Cambia board di destinazione"},
    {"Add files to board", "Aggiungi files alla board"},

    {"Save message to disk", "Salva il messaggio"},
    {"Download attachment(s)", "Download allegato/i"},
    {"Add Board(s)","Add Board(s)"},

    {"Add new board / folder", "Aggiungi board / cartella"},
    {"Remove selected board / folder", "Elimina board/cartella selezionata"},
    {"Copy selected board / folder", "Copia board/cartella selezionata"},
    {"Cut selected board / folder", "Taglia board/cartella selezionata"},
    {"Paste board / folder", "Incolla board/cartella"},
    {"Configure selected board", "Configura board selezionata"},
    {"Cancel", "Annulla"},

    // Preferences
    {"Options", "Opzioni"},
    {"OK", "OK"},
    {"(On)", "(On)"},
    {"(Off)", "(Off)"},
    {"Select download directory.", "Seleziona cartella di download."},

    {"Download directory:", "Cartella di download:"},
    {"Minimum HTL:", "Minimo HTL:"},
    {"Maximum HTL:", "Massimo HTL:"},

    {"Disable uploads", "Disabilita uploads"},
    {"Disable downloads", "Disabilita downloads"},
    {"Show only signed messages", "Visualizza solo messaggi firmati"},
    //{"Show only GOOD messages", "Visualizza solo GOOD"},
    {"Hide messages flagged BAD", "Nascondi BAD"},
    {"Hide messages flagged CHECK", "Nascondi CHECK"},
    {"Block message body containing:", "Blocca messaggi il cui corpo contiene:"},
    {"Block message from/subject containing:", "Blocca messaggi il cui mittente o oggetto contiene:"},
    {"Do spam detection", "Abilita rilevamento spam"},
    {"Sample interval (hours)", "Intervallo campionatura (ore)"},
    {"Threshold of blocked messages", "Sensibilità filtro messaggi"},
    {"Insert request if HTL tops:", "Invia richiesta se HTL supera:"},
    {"Clean the keypool", "Tieni pulita la keypool"},

    {"Automatic update options", "Opzioni aggiornamento automatico"},
    {"Minimum update interval of a board (minutes) :", "Minimo intervallo di aggiornamento per una board (minuti) :"},
    {"Number of concurrently updating boards:", "Board da aggiornare contemporaneamente:"},

    {"Number of simultaneous downloads:", "Downloads simultanei:"},
    {"Number of splitfile threads:", "Splitfile threads:"},
    {"Remove finished downloads every 5 minutes.", "Elimina downloads completati ogni 5min."},

    {"Upload HTL:", "Upload HTL:"},
    {"Number of simultaneous uploads:", "Uploads simultanei:"},
    {"Number of splitfile threads:", "Splitfile threads:"},

    {"Message upload HTL:", "Upload messaggi HTL:"},
    {"Message download HTL:", "Download messaggi HTL:"},
    {"Number of days to display:", "Numero di giorni visualizzati:"},
    {"Number of days to download backwards:", "Giorni precedenti da scaricare:"},
    {"Signature", "Firma"},
    {"Message base:", "Message base:"},
    {"Block messages: ", "Blocca messaggi: "},

    {"Keyfile upload HTL:", "Keyfile upload HTL:"},
    {"Keyfile download HTL:", "Keyfile download HTL:"},
    {"Node address:", "Indirizzo nodo:"},
    {"Node port:", "Porta nodo:"},
    {"Maximum number of keys to store:", "Massimo numero di chiavi da salvare:"},
    {"Allow 2 byte characters", "Permetti caratteri lunghi 2 byte"},

    {"Image Extension:", "Estensione Immagine:"},
    {"Video Extension:", "Estensione Video:"},
    {"Archive Extension:", "Estensione Archivio:"},
    {"Audio Extension:", "Estensione Audio:"},
    {"Document Extension:", "Estensione Documento:"},
    {"Executable Extension:", "Estensione Eseguibile:"},
    {"Use editor for writing messages: ", "Usa editor per scrivere messaggi: "},

	//	Preferences. Display panel		//TODO: Translate
	{"EnableSkins", "Enable Skins"}, 	
	{"MessageBodyFont", "Message Body Font"}, 

    // Preferences / Tabbed Pane
    {"Downloads", "Downloads"},
    {"Uploads", "Uploads"},
    {"Miscellaneous", "Altro"},
    {"Skins", "Skins"},
    {"News", "News"},
	{"Display", "Display"},		//TODO: Translate

    // Search table
    {"Filename", "Nome file"},
    {"Size", "Dimensione"},
    {"Age", "Data"},
    {"Key", "URI"},
    {"Board", "Board"},

    // Download table
    {"State", "Status"},
    {"HTL", "HTL"},
    {"Trying", "Provo"},
    {"Waiting", "Attesa"},
    {"Done", "Finito"},
    {"Failed", "Fallito"},
    {"Source", "Board"},

    // Upload table
    {"Last upload", "Ultimo upload"},
    {"Path", "Path"},
    {"Never", "Mai"},
    {"Uploading", "Uploading"},
    {"Destination", "Board"},
    {"Unknown", "Sconosciuto"},
    {"Requested", "Richiesto"},

    // News table
    {"Index", "ID"},
    {"From", "Da"},
    {"Subject", "Oggetto"},
    {"Date", "Data"},

    // About box
    {"About", "Informazioni"},
    {"Copyright (c) 2001 Jan-Thomas Czornack", "Copyright (c) 2001 Jan-Thomas Czornack"},

    // Board information frame
    {"Board information", "Informazioni board"},
    {"BoardInfoFrame.UpdateSelectedBoardButton", "Aggiorna la board selezionata"},
    {"Add board", "Aggiungi board"},
    {"Board", "Board"},
    {"State", "Status"},
    {"Messages", "Messaggi"},
    {"New messages", "Nuovi messaggi"},
    {"Files", "Files"},
    {"Update", "Aggiorna"},
    {"Boards: ", "Board: "},
    {"   Messages: ", "   Messaggi: "},
    {"   Files: ", "   Files: "},

    // Board settings frame
    {"Generate new keypair", "Genera nuove chiavi"},
    {"Public board", "Board pubblica"},
    {"Secure board", "Board sicura"},
    {"Board settings for ", "Settaggi per la board "},
    {"Private key :", "Chiave privata :"},
    {"Public key :", "Chiave pubblica :"},
    {"Not available", "N/A: non disponibile"},

    // Message frame
    {"Send message", "Invia messaggio"},
    {"Add attachment(s)", "Aggiungi allegato/i"},
    {"Board: ", "Board: "},
    {"From: ", "Da: "},
    {"Subject: ", "Oggetto: "},
    {"Create message", "Scrivi messaggio"},
    {"Choose file(s) / directory(s) to attach", "Scegli il file o la cartella da allegare"},
    {"Do you want to enter a subject?", "Vuoi inserire l'oggetto?"},
    {"No subject specified!", "Oggetto mancante!"},
    {"Yes", "Sì"},
    {"No", "No"},
    {"Add public key to this message", "Aggiungi la chiave pubblica a questo messaggio"},

    // Status bar
    {"Up: ", "Up: "},
    {"   Down: ", "   Down: "},
    {"   TOFUP: ", "   TOFUP: "},
    {"   TOFDO: ", "   TOFDO: "},
    {"   Results: ", "   Risultati: "},
    {"   Files: ", "   Files: "},
    {"   Selected board: ", "   Board selezionata: "},

    // New node dialog window
    {"New Node Name","Nuovo nome board"},
    {"New Folder","Nuova cartella"},
    
	//	Skin chooser				//TODO: Translate
	{"AvailableSkins", "Available Skins"},
	{"Preview", "Preview"},
	{"RefreshList", "Refresh List"},

    // Other
    {"Frost by Jantho", "Frost by Jantho"},
    {"Select a message to view its content.", "Seleziona un messaggio per visualizzare il contenuto."},
    {"Choose file(s) and/or directories to upload", "Scegli i files e/o le cartelle da caricare"},
    {"Please enter the prefix you want to use for your files.", "Prego inserisci il prefisso da dare ai tuoi files."},
    {"Retry", "Riprova"},
    {"I was not able to upload your message.", "Non sono riuscito a caricare il tuo messaggio"},
    {"Upload of message failed", "L'upload del messaggio è fallito."},
    {"Invalid key.  Key must begin with one of", "Chiave non valida. Deve iniziare con"},
    {"Invalid key", "Chiave non valida"},
    {"Name","Nome"},
    {"Trust","Fidati"},
    {"Do not trust","Non fidarti"},

    {"Do you want to add this board to the public boardlist?\n\n","Vuoi aggiungere questa board alla lista pubblica?\n\n"},
    {"ATTENTION: If you choose yes EVERY Frost user will\n","ATTENZIONE: Se scegli sì TUTTI gli utenti di Frost ne saranno\n"},
    {"know about this board AND he will be able to read and\n","a conoscenza E potranno leggere e\n"},
    {"write messages on this board.","scrivere i messaggi su questa board."},
    {" announcement."," annuncia."},
    };
}


