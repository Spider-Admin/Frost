package res;

import java.util.ListResourceBundle;
public class LangRes_fr extends ListResourceBundle {																			 	
    public Object[][] getContents() { 
	return contents;
    }
    static final Object[][] contents = {
	// Tabbed pane
	{"Search", "Recherche"},
	{"Downloads", "Téléchargements"},
	{"Uploads", "Envois"},
	{"News", "Forums"},

	// Menues
	{"File", "Fichier"},
	{"Exit", "Quitter"},
	{"News", "Forums"},
	{"Force update of selected board", "Forcer la mise à jour du forum sélectionné"},
	{"Options", "Options"},
	{"Preferences", "Préférences"},
	{"Help", "Aide"},
	{"About", "A propos"},

	// Buttons
	{"Download", "Télécharger"},
	{"Browse...", "Parcourir..."},
	{"New message", "Nouveau message"},
	{"Reply", "Répondre"},
	{"Update", "Mettre à jour"},
	{"Add board", "Ajouter"},
	{"Remove selected board", "Supprimer le forum sélectionné"},
	{"Download attachment(s)", "Télécharger la(les) pièce(s) jointe(s)"},

	// Check Boxes
	{"Automatic update", "Mise à jour automatique"},
	{"Activate downloading", "Activer le téléchargement"},
	{"Activate uploading", "Activer l'envoi"},

	// Popup Menues
	{"Download selected keys", "Télécharger les clefs sélectionnées"},
	{"Download all keys", "Télécharger toutes les clefs"},
	{"Cancel", "Annuler"},

	{"Remove selected downloads", "Effacer les téléchargements sélectionnés"},
	{"Remove all downloads", "Effacer tous les téléchargements"},
	{"Retry selected downloads", "Réessayer les téléchargements sélectionnés"},
	{"Move selected downloads up", "Déplacer les téléchargements sélectionnés vers le haut"},
	{"Move selected downloads down", "Déplacer les téléchargements sélectionnés vers le bas"},
	{"Remove selected files", "Effacer les fichiers sélectionnés"},
	{"Remove all files", "Effacer tous les fichiers"},
	{"Move selected files up", "Déplacer les fichiers sélectionnés vers le haut"},
	{"Move selected files down", "Déplacer les fichiers sélectionnés vers le bas"},
	{"Reload selected files", "Recharger les fichiers sélectionnés"},
	{"Reload all files", "Recharger tous les fichiers"},
	{"Set prefix for selected files", "Définir le préfixe des fichiers sélectionnés"},
	{"Set prefix for all files", "Définir le préfixe de tous les fichiers"},
	{"Restore default filenames for selected files", "Restaurer les noms de fichiers par défaut des fichiers sélectionnés"},
	{"Restore default filenames for all files", "Restaurer les noms de fichiers par défaut de tous les fichiers"},
	{"Save message to disk", "Enregistrer ce message sur le disque..."},

	// Preferences
	{"Options", "Options"},
	{"OK", "Ok"},
	{"On", "On"},
	{"Off", "Off"},

	{"Select download directory.", "Sélectionnez le dossier de téléchargement"},
	{"Download directory:", "Dossier de téléchargement:"},
	{"Minimum HTL (5):", "HTL mimimum (5):"},
	{"Maximum HTL (25):", "HTL maximum (25):"},
	{"Number of simultaneous downloads (3):", "Nombre de téléchargements simultanés (3):"},
	{"Number of splitfile threads (3):", "Nombre de tâches par fichiers (3):"},
	{"Remove finished downloads every 5 minutes.(off)", "Effacer les téléchargements terminés toutes les 5 minutes (off)"},
	{"Upload HTL (5):", "HTL d'envoi (5):"},
	{"Number of simultaneous uploads (3):", "Nombre d'envois simultanés (3):"},
	{"Number of splitfile threads (3):", "Nombre de tâches par fichier (3):"},
	{"Reload interval (8 days):", "Interval de rechargement (8 jours):"},
	{"Message upload HTL (5):", "HTL d'envoi des messages (5):"},
	{"Message download HTL(15):", "HTL de téléchargement des messages (15):"},
	{"Number of days to display (10):", "Nombre de jours à afficher (10):"},
	{"Number of days to download backwards (3):", "Nombre de jours à télécharger (3):"},
	{"Keypool path:", "Dossier de stockage des clefs:"},
	{"Keyfile upload HTL (5):", "HTL d'envoi du fichier de clefs (5):"},
	{"Keyfile download HTL (15):", "HTL de téléchargement du fichier de clefs (15):"},
	{"Node address (127.0.0.1):", "Adresse du node (127.0.0.1):"},
	{"Node port (8481):", "Port du node (8481):"},
	{"Maximum number of keys to store (100000):", "Nombre maximum de clefs à stocker (100000):"},
	{"Use skins, please restart Frost after changing this.(off)", "Utiliser les thèmes, veuillez redémarrer Frost après avoir changé ceci. (off)"},
	{"Image Extension:", "Image Extension:"},
	{"Video Extension:", "Video Extension:"},
	{"Archive Extension:", "Archive Extension:"},
	{"Audio Extension:", "Audio Extension:"},
	{"Document Extension:", "Document Extension:"},
	{"Executable Extension:", "Executable Extension:"},
	{"Show time in titlebar", "Show Time in titlebar"},
	{"Show time", "Show time"},

	//	Preferences. Display panel		//TODO: Translate
	{"EnableSkins", "Enable Skins"}, 	
	{"MessageBodyFont", "Message Body Font"}, 
	{"MoreSkinsAt", "You can get more skins at"},

	// Preferences / Tabbed Pane
	{"Downloads", "Téléchargements"},
	{"Uploads", "Envois"},
	{"Miscellaneous", "Divers"},
	{"Skins", "Thèmes"},
	{"News", "Forums"},
	{"Display", "Display"},		//TODO: Translate

	// Search table
	{"Filename", "Nom du fichier"},
	{"Size", "Taille"},
	{"Age", "Age"},
	{"Key", "Clef"},
	
	// Download table
	{"Status", "Statut"},
	{"HTL", "HTL"},
	{"Trying", "Essai en cours"},
	{"Waiting", "En attente"},
	{"Done", "Terminé"},
	{"Failed", "Echoué"},

	// Upload table
	{"Last upload", "Dernier envoi"},
	{"Path", "Chemin"},
	{"Never", "Jamais"},
	{"Uploading", "Envoi en cours"},
	
	// News table
	{"Index", "Index"},
	{"From", "Auteur"},
	{"Subject", "Sujet"},
	{"Date", "Date"},
	
	// About box
	{"About", "A propos"},
	
	// Board settings frame
	{"Generate new keypair", "Générer une nouvelle paire de clefs"},
	{"Public board", "Forum public"},
	{"Secure board", "Forum sécurisé"},
	{"Board settings for ", "Paramètres du forum "},
	{"Private key :", "Clef privée :"},
	{"Public key :", "Clef publique :"},
	{"Not available", "Indisponible"},
	{"Configure selected board", "Configurer le forum sélectionné"},

	// Message frame
	{"Send message", "Envoyer le message"},
	{"Add attachment(s)", "Joindre"},
	{"Board: ", "Forum: "},
	{"From: ", "Auteur: "},
	{"Subject: ", "Sujet: "},
	{"Create message", "Nouveau message"},
	{"Choose file(s) / directory(s) to attach", "Sélectionnez le(s) fichier(s) / dossier(s) à joindre"},
	{"Do you want to enter a subject?", "Voulez-vous entrer un sujet?"},
	{"No subject specified!", "Aucun sujet spécifié!"},
	{"Yes", "Oui"},
	{"No", "Non"},

	// Status bar
	{"Up: ", "Envois: "},
	{"   Down: ", "   Téléchargements: "},
	{"   TOFUP: ", "   TOFEN: "},
	{"   TOFDO: ", "   TOFTE: "},
	{"   Results: ", "   Résultats: "},
	{"   Files: ", "   Fichiers: "},

	//	Skin chooser				//TODO: Translate
	{"AvailableSkins", "Available Skins"},
	{"Preview", "Preview"},
	{"RefreshList", "Refresh List"},
	{"NoSkinsFound", "No Skins Found"},

	// Other 
	{"Frost by Jantho", "Frost par Jantho"},
	{"Copyright (c) 2001 Jan-Thomas Czornack", "Copyright (c) 2001 Jan-Thomas Czornack"},
	{"Select a message to view its content.", "Sélectionnez un message pour voir son contenu."},
	{"Choose file(s) and/or directories to upload", "Sélectionnez le(s) fichier(s) / dossier(s) à envoyer"},
	{"Please enter the prefix you want to use for your files.", "Veuillez entrer le préfixe que vous voulez utiliser pour vos fichiers:"},
	{"Retry", "Réessayer"},
	{"I was not able to upload your message.", "Impossible d'envoyer le message."},
	{"Upload of message failed", "L'envoi du message à échoué"},
    }; 
}
