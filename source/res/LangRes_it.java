package res;
import java.util.*;

public class LangRes_it extends ListResourceBundle {
    public Object[][] getContents() {
    return contents;
    }
    static final Object[][] contents = {

    // Tabbed pane
    {"Search", "Cerca"},
    {"Downloads", "Downloads"},
    {"Uploads", "Uploads"},
    {"News", "Forum"},
    {"Status", "Status"},


    // Menues
    {"File", "File"},
    {"Exit", "Esci"},

    {"News", "News"},
    {"Automatic message update", "Autoaggiornamento forum"},
    {"Increase Font Size", "Ingrandisci carattere"},
    {"Decrease Font Size", "Riduci carattere"},
    {"Configure selected board", "Configura il forum selezionato"},
    {"Display board information window", "Visualizza finestra informazioni forum"},

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
    {"New board", "Nuovo forum"},
    {"Rename folder", "Rinomina cartella"},
    {"Remove board", "Elimina forum"},
    {"Cut board", "Taglia forum"},
    {"Copy board", "Copia forum"},
    {"Paste board", "Incolla forum"},
    {"Configure board", "Configura forum"},
    {"Save message", "Salva messaggio"},
    {"Search all boards", "Cerca su tutti i forum"},
    {"Download attachment(s)", "Download allegato/i"},

    // Check Boxes
    {"Automatic update", "Autoaggiornamento forum"},
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

    {"Save message to disk", "Salva il messaggio"},
    {"Download attachment(s)", "Download allegato/i"},

    {"Add new board / folder", "Aggiungi forum / cartella"},
    {"Remove selected board / folder", "Elimina forum/cartella selez."},
    {"Copy selected board / folder", "Copia forum/cartella selez."},
    {"Cut selected board / folder", "Taglia forum/cartella selez."},
    {"Paste board / folder", "Incolla forum/cartella"},
    {"Configure selected board", "Configura forum selezionato"},
    {"Cancel", "Annulla"},

    // Preferences
    {"Options", "Opzioni"},
    {"OK", "OK"},
    {"On", "On"},
    {"Off", "Off"},
    {"Select download directory.", "Seleziona cartella di download."},

    {"Download directory:", "Cartella di download:"},
    {"Minimum HTL (5):", "Minimo HTL (5):"},
    {"Maximum HTL (25):", "Massimo HTL (25):"},
    {"Number of simultaneous downloads (3):", "Downloads simultanei (3):"},
    {"Number of splitfile threads (3):", "Splitfile threads (3):"},
    {"Remove finished downloads every 5 minutes.(off)", "Elimina downloads completati ogni 5min.(off)"},

    {"Upload HTL (5):", "Upload HTL (5):"},
    {"Number of simultaneous uploads (3):", "Uploads simultanei (3):"},
    {"Number of splitfile threads (3):", "Splitfile threads (3):"},
    {"Reload interval (8 days):", "Intervallo autoreload (8 giorni):"},

    {"Message upload HTL (5):", "Upload messaggi HTL (5):"},
    {"Message download HTL(15):", "Download messaggi HTL(15):"},
    {"Number of days to display (10):", "Numero di giorni visualizzati (10):"},
    {"Number of days to download backwards (3):", "Giorni precedenti da scaricare (3):"},

    {"Keyfile upload HTL (5):", "Keyfile upload HTL (5):"},
    {"Keyfile download HTL (15):", "Keyfile download HTL (15):"},
    {"Node address (127.0.0.1):", "Indirizzo nodo (127.0.0.1):"},
    {"Node port (8481):", "Porta nodo (8481):"},
    {"Maximum number of keys to store (100000):", "Massimo numero di chiavi da salvare (100000):"},
    {"Allow 2 byte characters", "Permetti caratteri lunghi 2 byte"},

    {"Use skins, please restart Frost after changing this.(off)", "Usa skins, riavvia Frost se cambi questa opzione.(uit)"},

    // Preferences / Tabbed Pane
    {"Downloads", "Downloads"},
    {"Uploads", "Uploads"},
    {"Miscellaneous", "Altro"},
    {"Skins", "Skins"},
    {"News", "Forum"},

    // Search table
    {"Filename", "Nome file"},
    {"Size", "Dimensione"},
    {"Age", "Data"},
    {"Key", "URI"},
    {"Board", "Forum"},

    // Download table
    {"Status", "Status"},
    {"HTL", "HTL"},
    {"Trying", "Provo"},
    {"Waiting", "Attesa"},
    {"Done", "Finito"},
    {"Failed", "Fallito"},
    {"Source", "Forum"},

    // Upload table
    {"Last upload", "Ultimo upload"},
    {"Path", "Path"},
    {"Never", "Mai"},
    {"Uploading", "Uploading"},
    {"Destination", "Forum"},

    // News table
    {"Index", "ID"},
    {"From", "Da"},
    {"Subject", "Oggetto"},
    {"Date", "Data"},

    // About box
    {"About", "Informazioni"},

    // Board information frame
    {"Board information", "Informazioni Forum"},
    {"Add board", "Aggiungi forum"},
    {"Board", "Forum"},
    {"State", "Status"},
    {"Messages", "Messaggi"},
    {"New messages", "Nuovi messaggi"},
    {"Files", "Files"},
    {"Update", "Aggiorna"},

    // Board settings frame
    {"Generate new keypair", "Genera nuove chiavi"},
    {"Public board", "Forum pubblico"},
    {"Secure board", "Forum sicuro"},
    {"Board settings for ", "Settaggi per il forum "},
    {"Private key :", "Chiave privata :"},
    {"Public key :", "Chiave pubblica :"},
    {"Not available", "N/A: non disponibile"},

    // Message frame
    {"Send message", "Invia messaggio"},
    {"Add attachment(s)", "Aggiungi allegato/i"},
    {"Board: ", "Forum: "},
    {"From: ", "Da: "},
    {"Subject: ", "Oggetto: "},
    {"Create message", "Scrivi messaggio"},
    {"Choose file(s) / directory(s) to attach", "Scegli il file o la cartella da allegare"},
    {"Do you want to enter a subject?", "Vuoi inserire l'oggetto?"},
    {"No subject specified!", "Oggetto mancante!"},
    {"Yes", "Si"},
    {"No", "No"},

    // Status bar
    {"Up: ", "Up: "},
    {"   Down: ", "   Down: "},
    {"   TOFUP: ", "   TOFUP: "},
    {"   TOFDO: ", "   TOFDO: "},
    {"   Results: ", "   Risultati: "},
    {"   Files: ", "   Files: "},
    {"   Selected board: ", "   Forum selezionato: "},

    // Other
    {"Frost by Jantho", "Frost by Jantho"},
    {"Copyright (c) 2001 Jan-Thomas Czornack", "Copyright (c) 2001 Jan-Thomas Czornack"},
    {"Select a message to view its content.", "Seleziona un messaggio per visualizzare il contenuto."},
    {"Choose file(s) and/or directories to upload", "Scegli i files e/o le cartelle da caricare"},
    {"Please enter the prefix you want to use for your files.", "Prego inserisci il prefisso da dare ai tuoi files."},
    {"Retry", "Riprova"},
    {"I was not able to upload your message.", "Non sono riuscito a caricare il tuo messaggio"},
    {"Upload of message failed", "L'upload del messaggio è fallito."},
    {"Invalid key.  Key must begin with one of", "Chiave non valida. Deve iniziare con"},
    {"Invalid key", "Chiave non valida"},
    {"Name","Nome"},
    };
}



