/*
 * Created on Nov 8, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package res;

import java.util.ListResourceBundle;


public class LangRes_es extends ListResourceBundle {

    public Object[][] getContents() {
    return contents;
    }
    static final Object[][] contents = {
	
	///////////////////////////////////////////////////
	// Unknown translations
	// Have to look where in the GUI these translations are needed
	///////////////////////////////////////////////////
	{"Minimize to System Tray", "Minimiza a la Bandeja del Sistema"},
	{"Index", "Índice"},
	{"From", "De"},
	{"Subject", "Asunto"},
	{"Date", "Fecha"},
	{"Filename", "Nombre del fichero"},
	{"Key", "Clave"},
	{"Select a message to view its content.", "Selecciona un mensaje para ver su contenido."},
	{"Size", "Tamaño"},
	{"Age", "Antigüedad"},
	{"Board", "Foro"},
	{"Waiting", "Esperando"},
	{"State", "Estado"},
	{"Source", "Origen"},
	{"Last upload", "Última subida"},
	{"Path", "Directorio"},
	{"Destination", "Destino"},
	{"Frost by Jantho", "Frost por Jantho"},
	{"Reload selected files", "Recarga los ficheros seleccionados"},
	{"Reload all files", "Recarga todos los ficheros"},
	{"Trying", "Intentando"},
	{"Done", "Hecho"},
	{"Failed", "Falló"},
	{"Never", "Nunca"},
	{"Requested", "Pedido"},
	{"Uploading", "Subiendo"},
	{"Unknown", "Desconocido"},

	///////////////////////////////////////////////////
    // Welcome message
	///////////////////////////////////////////////////
    {"Welcome message", "!Por favor, lee esto!\n\nPara usar Frost, primero debes seleccionar un tema en la ventana de información de foros. Puedes abrir esta ventana con el botón i de arriba. Puedes enviar mensajes y ficheros a gente usando el mismo foro. Probablemente pase un rato hasta que aparezcan los primeros foros (pulsa el botón de refresco)."},

	///////////////////////////////////////////////////
	// Main Window Menu
	///////////////////////////////////////////////////
    // Menu File
    {"File", "Fichero"},
    {"Exit", "Salir"},

	// Menu News
    {"News", "Noticias"},
    {"Automatic message update", "Refresco automático de foros"},
    {"Increase Font Size", "Aumenta Tamaño del Fuente"},
    {"Decrease Font Size", "Reduce Tamaño del Fuente"},
    {"Configure selected board", "Configura foro seleccionado"},
    {"Display board information window", "Muestra ventana de información de foros"},
	{"Display known boards", "Muestra foros conocidos"},
	
	// Menu Options
    {"Options", "Opciones"},
    {"Preferences", "Preferencias"},

	// Menu Plugins
	{"Plugins", "Plugins"},
	{"Experimental Freenet Browser", "Navegador Experimental para Freenet"},
	{"Translate Frost into another language", "Traduce Frost a otro idioma"},

	// Menu Language
	{"Language", "Idioma"},
	{"German", "Alemán"},
	{"English", "Inglés"},
	{"Dutch", "Holandés"},
	{"Japanese", "Japonés"},
	{"French", "Francés"},
	{"Italian", "Italiano"},
	{"Spanish", "Español"},

	// Menu Help
    {"Help", "Ayuda"},
    {"About", "Acerca de"},

	///////////////////////////////////////////////////
    // Main Window ToolBar
	///////////////////////////////////////////////////
	{"New board", "Nuevo foro"},
	{"New folder", "Nueva carpeta"},
	{"Configure board", "Configura foro"},
	{"Rename folder", "Renombra carpeta"},
	{"Cut board", "Corta foro"},
	{"Paste board", "Pega foro"},
	{"Remove board", "Elimina foro"},
	{"Board Information Window", "Ventana de Información de Foros"},
	{"Display list of known boards", "Muestra lista de foros conocidos"},
    
	///////////////////////////////////////////////////
	// Main Window Tabbed Pane
	///////////////////////////////////////////////////
	{"Search", "Busca"},
	{"Downloads", "Descargas"},
	{"Uploads", "Subidas"},
	{"News", "Noticias"},
	{"Status", "Estado"},

	///////////////////////////////////////////////////
    // Main Window News Tab
	///////////////////////////////////////////////////
    // ToolBar in News Tab
	{"Save message", "Graba mensaje"},
	{"New message", "Nuevo mensaje"},
	{"Reply", "Contesta"},
	{"Update", "Refresca"},
	{"Download attachment(s)", "Descarga adjunto(s)"},
	{"Add Board(s)","Añade Foro(s)"},
	{"Trust","Confiar"},
	{"Set to CHECK", "Marca como COMPROBAR"},
	{"Do not trust","No confiar"},

	// Popup over message table
	{"Mark message unread", "Marca el mensaje como no leído"},
	{"Mark ALL messages read", "Marca TODOS los mensajes como leídos"},
	{"Cancel", "Cancela"},	

	// Popup over message
	{"Save message to disk", "Graba mensaje a disco"},
	//{"Cancel", "Cancel"}, // Defined above

	// Popup over attachments table
	{"Add selected board", "Añade foro seleccionado"},	
	//{"Cancel", "Cancel"}, // Defined above
	
	///////////////////////////////////////////////////
	// Main Window Search Tab
	///////////////////////////////////////////////////
	// ToolBar in Search tab
	//{"Search", "Search"}, // Defined above
	{"Download selected keys", "Descarga claves seleccionadas"},
	{"all boards", "todos los foros"},
	
	// Popup over search results table
	//{"Download selected keys", "Download selected keys"}, // Defined above
	{"Download all keys", "Descarga todas las claves"},
	{"help user (sets to GOOD)", "ayuda a usuario (marca como BUENO)"},
	{"block user (sets to BAD)", "bloquea usuario (marca como MALO)"},
	//{"Cancel", "Cancel"}, // Defined above
		
	///////////////////////////////////////////////////
    // Main Window Downloads Tab
	///////////////////////////////////////////////////
	// ToolBar in Downloads Tab
	{"Activate downloading", "Activa descarga"},
	{"Show healing information", "Muestra información de reparación"},
    
    // Popup over download table
	{"Restart selected downloads", "Reanuda descargas seleccionadas"},
	{"Enable downloads", "Activa descargas"},
	{"Enable selected downloads", "Activa descargas seleccionadas"},
	{"Disable selected downloads", "Desactiva descargas seleccionadas"},
	{"Invert enabled state for selected downloads", "Invierte la activación de las descargas seleccionadas"},
	{"Enable all downloads", "Activa todas las descargas"},
	{"Disable all downloads", "Desactiva todas las descargas"},
	{"Invert enabled state for all downloads", "Invierte la activación de todas las descargas"},
	{"Remove", "Elimina"},
	{"Remove selected downloads", "Elimina descargas seleccionadas"},
	{"Remove all downloads", "Elimina todas las descargas"},
	{"Remove finished downloads", "Elimina las descargas finalizadas"},
	//{"Cancel", "Cancel"}, // Defined above
        
	///////////////////////////////////////////////////
    // Main Window Uploads Tab
	///////////////////////////////////////////////////
	// ToolBar in Uploads Tab
	{"Browse", "Examina"},

    // FileChooser in Uploads Tab
	{"Select file you want to upload to the", "Selecciona el fichero que quieres subir al"},
	{"board", "foro"},

	// Popup over uploads table    
	{"Copy to clipboard", "Copia al portapapeles"},
	{"CHK key", "clave CHK"},
	{"CHK key + filename", "clave CHK + nombre de fichero"},
	//{"Remove", "Remove"}, // Defined above
	{"Remove selected files", "Elimina ficheros seleccionados"},
	{"Remove all files", "Elimina todos los ficheros"},
	{"Start encoding of selected files", "Inicia la codificación de los ficheros seleccionados"},
	{"Upload selected files", "Sube ficheros seleccionados"},
	{"Upload all files", "Sube todos los ficheros"},
	{"Set prefix for selected files", "Establece prefijo para los ficheros seleccionados"},
	{"Set prefix for all files", "Establece prefijo para todos los ficheros"},
	{"Restore default filenames for selected files", "Restaura nombres por defecto para los ficheros seleccionados"},
	{"Restore default filenames for all files", "Restaura nombres por defecto para todos los ficheros"},
	{"Change destination board", "Cambia foro de destino"},
	//{"Cancel", "Cancel"}, // Defined above    
    
	///////////////////////////////////////////////////
	// Main Window Board Selection Tree
	///////////////////////////////////////////////////
    // Popup over Tree
	{"Refresh folder", "Refresca carpeta"},
	{"Add new board", "Añade nuevo foro"},
	{"Add new folder", "Añade nueva carpeta"},
	{"Configure selected board", "Configura foro seleccionado"},
	//{"Remove board", "Remove board"}, // Defined above
	//{"Cut board", "Cut board"}, // Defined above
	//{"Cancel", "Cancel"}, // Defined above
    
	///////////////////////////////////////////////////
    // Main Window Status Bar
	///////////////////////////////////////////////////
	{"Up", "Arriba"},
	{"Down", "Abajo"},
	{"TOFUP", "TOFSU"},
	{"TOFDO", "TOFDE"},
	{"Results", "Resultados"},
	{"Files", "Ficheros"},
	{"Selected board", "Foro seleccionado"},
      
	///////////////////////////////////////////////////
	// New Message Window
	///////////////////////////////////////////////////
	{"Create message", "Crea mensaje"},
	{"Send message", "Envía mensaje"},
	{"Add attachment(s)", "Añade adjunto(s)"},
	{"Sign", "Firma"},
	{"Indexed attachments", "Adjuntos indexados"},
	{"Should file attachments be added to upload table?", "¿Debo añadir los ficheros adjuntos a la tabla de subidas?"},
	{"Board", "Foro"},
	{"From", "De"},
	{"Subject", "Asunto"},
	{"Remove", "Elimina"},
	{"Do you want to enter a subject?", "¿Quieres introducir un asunto?"},
	{"No subject specified!", "!No se ha especificado un asunto!"},
	{"You must enter a subject!", "!Debes introducir un asunto!"},
	{"You must enter a sender name!", "!Debes introducir un nombre de remitente!"},
	{"No 'From' specified!", "!No se ha especificado 'Origen'!"},
	{"Choose file(s) / directory(s) to attach", "Escoge el fichero(s) / directorio(s) a adjuntar"},
	{"Choose boards to attach", "Escoge el foro(s) a adjuntar"},

	///////////////////////////////////////////////////
	// About box
	///////////////////////////////////////////////////
	{"About", "Acerca de"},
	{"OK", "De acuerdo"},

	///////////////////////////////////////////////////
	// Preferences
	///////////////////////////////////////////////////
	// More often used translations
	{"On", "Encendido"},
	{"Off", "Apagado"},
	//{"OK", "OK"}, // Defined above
	//{"Cancel", "Cancel"}, // Defined above
	
	// Downloads Panel
	{"Downloads", "Descargas"},
	{"Disable downloads", "Desactiva descargas"},
	{"Download directory", "Directorio para descargas"},
	//{"Browse", "Browse"}, // Defined above
	{"Restart failed downloads", "Reintenta descargas fallidas"},
	{"Maximum number of retries", "Número máximo de reintentos"},
	{"Waittime after each try", "Tiempo de espera tras cada intento"},
	{"minutes", "minutos"},
	{"Enable requesting of failed download files", "Activa la petición de ficheros de descargas fallidas"},
	{"Request file after this count of retries", "Solicita el fichero tras este número de reintentos"},
	{"Number of simultaneous downloads", "Número de descargas simultáneas"},
	{"Number of splitfile threads", "Número de hilos de splitfile"},
	{"Remove finished downloads every 5 minutes", "Elimina las descargas finalizadas cada 5 minutos"},
	{"Try to download all segments, even if one fails", "Intenta descargar todos los segmentos, aunque alguno falle"},
	{"Decode each segment immediately after its download", "Decodifica cada segmento nada más descargarse"},

	// Uploads Panel
	{"Disable uploads", "Desactiva subidas"},
	{"Automatic Indexing", "Indexación automática"},
	{"Share Downloads","Comparte descargas"},
	{"Sign shared files", "Firma ficheros compartidos"},
	{"Help spread files from people marked GOOD" , "Ayuda a difundir los ficheros de la gente marcada BUENA"},
	{"Upload HTL", "HTL para subidas"},
	{"up htl explanation","(cuanto mayor más lento pero más fiable)"},
	{"Number of simultaneous uploads", "Número de subidas simultáneas"},
	{"Number of splitfile threads", "Número de hilos de splitfile"},
	{"splitfile explanation","(cuanto mayor más rápido pero consume más CPU)"},
	{"Upload batch size","Tamaño del lote de subidas"},
	{"batch explanation", "(cuanto mayor más rápido pero menos resistente a spam)"},
	{"Index file redundancy","Redundancia en ficheros de índice"},
	{"redundancy explanation", "(todavía no funciona)"},

	// News (1) Panel
	{"Message upload HTL", "HTL para subida de mensajes"},
	{"Message download HTL", "HTL para bajada de mensajes"},
	{"Number of days to display", "Número de días a mostrar"},
	{"Number of days to download backwards", "Número de días en los que descargar hacia atrás"},
	{"Message base", "Base de mensajes"},
	{"Signature", "Firma"},
		
	// News (2) Panel
	{"Block messages with subject containing (separate by ';' )", "Bloquea mensajes cuyo asunto contiene (separados por ';' )"},
	{"Block messages with body containing (separate by ';' )", "Bloquea mensajes cuyo cuerpo contiene (separados por ';' )"},
	{"Hide unsigned messages", "Oculta mensajes sin firmar"},
	{"Hide messages flagged BAD", "Oculta mensajes marcados como MALO"},
	{"Hide messages flagged CHECK", "Oculta mensajes marcados como COMPROBAR"},
	{"Hide messages flagged N/A", "Oculta mensajes marcados como N/A"},
	{"Do spam detection", "Realiza detección de spam"},
	{"Sample interval", "Intervalo de muestreo"},
	{"hours", "horas"},
	{"Threshold of blocked messages", "Umbral de mensajes bloqueados"},
		
	// News (3) Panel
	{"Automatic update options", "Opciones de refresco automático"},
	{"Minimum update interval of a board", "Intervalo mínimo de refresco de un foro"},
	//{"minutes", "minutes"}, // Defined above
	{"Number of concurrently updating boards", "Número de foros refrescándose a la vez"},
	{"Show board update visualization", "Muestra visualmente el refresco de los foros"},
	{"Choose background color if updating board is selected", "Escoge el color de fondo para los foros seleccioados que se están refrescando"},
	{"Choose background color if updating board is not selected", "Escoge el color de fondo para los foros no seleccioados que se están refrescando"},
	
    // Search Panel
	{"Image Extension", "Extensiones de Imagen"},
	{"Video Extension", "Extensiones de Vídeo"},
	{"Archive Extension", "Extensiones de Archivo"},
	{"Document Extension", "Extensiones de Documento"},
	{"Audio Extension", "Extensiones de Sonido"},
	{"Executable Extension", "Extensiones Ejecutables"},
	{"Maximum search results", "Número máximo de resultados de búsqueda"},
	{"Hide files from people marked BAD" ,"Oculta ficheros de gente marcada como MALA"},
	{"Hide files from anonymous users", "Oculta ficheros de usuarios anónimos"},

	// Miscelaneous Panel
	{"Keyfile upload HTL", "HTL de subida de Keyfiles"},
	{"Keyfile download HTL", "HTL de descarga de Keyfiles"},
	{"list of nodes","Lista separada por comas de nodos a los que tienes acceso por FCP"},
	{"Maximum number of keys to store", "Número máximo de claves a guardar"},
	{"Allow 2 byte characters", "Permite caracteres de 2 bytes"},
	{"Use editor for writing messages", "Usa editor para escribir mensajes"},
	{"Clean the keypool", "Limpia el pool de claves"},
	{"Automatic saving interval", "Intervalo de grabación automática"}, 	
	{"Disable splashscreen", "Desactiva pantalla de splash"}, 	

    // Display Panel
    {"EnableSkins", "Activa Skins"}, 	
	{"MessageBodyFont", "Fuente del cuerpo de los mensajes"}, 
	{"MoreSkinsAt", "Puedes conseguir más skins en"},
	{"Preview", "Vista previa"},
	{"RefreshList", "Refresca lista"},
	{"NoSkinsFound", "¡No se han encontrado skins!"},
	{"AvailableSkins","Skins disponibles"},

	//other stuff which people forget to include
	{"BoardInfoFrame.UpdateSelectedBoardButton", "Refresca Foro Seleccionado"},
	{"Show systray icon", "Muestra icono en Bandeja del Sistema"},
	{"Display", "Pantalla"},
	{"Miscellaneous", "Miscelánea"},
	{"Messages", "Mensajes"},
	{"Messages Today", "Mensajes de Hoy"},
	{"Board information", "Información del Foro"},
	{"Boards", "Foros"},
	{"Public board", "Foro público"},
	{"Secure board", "Foro seguro"},
	{"Generate new keypair", "Genera nueva pareja de claves"},
	{"Private key", "Clave privada"},
	{"Public key", "Clave pública"},
	{"Not available", "No disponible"}

    };
}
