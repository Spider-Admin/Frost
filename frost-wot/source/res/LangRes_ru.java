/*
  LangRes.java
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>
  Public domain -c- 2006 VolodyA! V A <VolodyA! V A@r0pa7z7JA1hAf2xtTt7AKLRe+yw>

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
public class LangRes_ru extends ListResourceBundle {

    public Object[][] getContents() {
    return contents;
    }
    static final Object[][] contents = {


	///////////////////////////////////////////////////
	// Unknown translations
	// Have to look where in the GUI these translations are needed
	///////////////////////////////////////////////////
	{"Minimize to System Tray", "Свернуть в Трей"},
	{"Index", "Индекс"},
	{"From", "От"},
	{"Subject", "Тема"},
	{"Date", "Дата"},
	{"Filename", "Имя файла"},
	{"Key", "Ключ"},
	{"Select a board to view its content.", "Выбери форум для просмотра."},
	{"Select a message to view its content.", "Выбери сообщение для просмотра."},
	{"Size", "Размер"},
	{"Age", "Возраст"},
	{"Board", "Форум"},
	{"State", "Состояние"},
	{"Source", "Source"},
	{"Last upload", "Последняя загрузка"},
	{"Path", "Путь"},
	{"Destination", "Цель"},
	{"Frost by Jantho", "Морозко созданный Jantho"},
	{"Reload selected files", "Закачать выбранные файлы"},
	{"Reload all files", "Закачать все файлы заново"}, //reload????
	{"Show systray icon","Показывать иконку в трее"},
	{"Display","Показать"},
	{"Miscellaneous","Разное"},
	{"Messages Today","Сообщений сегодня"},
	{"Public board","Общий форум"},
	{"Secure board","Закрытый форум"},
	{"Generate new keypair","Сгенирировать новую пару ключей"},
	{"Private key","Личный ключ"},
	{"Public key","Общественный ключ"},
	{"Not available","Нет"},

	///////////////////////////////////////////////////
	// Splash messages
	///////////////////////////////////////////////////
	{"Initializing Mainframe","Инициализируем аппаратуру"},
	{"Hypercube fluctuating!","Тестируем гиперкуб!"},
	{"Sending IP address to NSA","Регистрируем IP с ФСБ"},
	{"Wasting more time","Похмеляемся"},
	{"Reaching ridiculous speed...","Разгоняемся до офигенной скорости..."},

	///////////////////////////////////////////////////
    // Welcome message
	///////////////////////////////////////////////////
    {"Welcome message", "Пожалуйста, прочитай это!\n\nЧтобы использовать Морозко тебе сперва надо выбрать форумы на которые подписаться. Это можно сделать нажав кнопку i на верхней панели. Ты можешь отправлять сообщения и файлы людям читающим форум, на который ты пишешь. Возможно пройдёт какое-то время перед тем как новые форумы начнут появляться."},

	///////////////////////////////////////////////////
	// Main Window Menu
	///////////////////////////////////////////////////
    // Menu File
    {"File", "Файл"},
    {"Exit", "Выход"},

	// Menu News
    {"News", "Новости"},
    {"Automatic message update", "Обновлять форумы автоматически"},
    {"Increase Font Size", "Увиличить шрифт"},
    {"Decrease Font Size", "Уменьшить шрифт"},
    {"Configure selected board", "Конфигурация форума"},
    {"Configure selected folder", "Конфигурация папки"},
    {"Display board information window", "Информация форумов"},
	{"Display known boards", "Известные форумы"},

	// Menu Options
    {"Options", "Опции"},
    {"Preferences", "Настройки"},

	// Menu Plugins
	{"Plugins", "Расширения"},
	{"Experimental Freenet Browser", "Экспериментальный Браузер Фринета"},
	{"Translate Frost into another language", "Перевести Морозко на другой язык"},

	// Menu Language
	{"Language", "Язык"},
	{"Default", "Default"},
	{"German", "German"},
	{"English", "English"},
	{"Dutch", "Dutch"},
	{"Japanese", "Japanese"},
	{"French", "French"},
	{"Italian", "Italian"},
	{"Spanish", "Spanish"},
	{"Bulgarian","Bulgarian"},
    {"Russian","Russian"},

	// Menu Help
    {"Help", "Помощь"},
    {"About", "О Морозко"},

	///////////////////////////////////////////////////
    // Main Window ToolBar
	///////////////////////////////////////////////////
	{"New board", "Новый форум"},
	{"New folder", "Новая папка"},
	{"Configure board", "Конфигурация форума"},
	{"Rename folder", "Переименовать папку"},
	{"Cut board", "Вырезать форум"},
	{"Paste board", "Вставить форум"},
	{"Remove board", "Удалить форум"},
	{"Board Information Window", "Информация форума"},
	{"Display list of known boards", "Показать спасок известных форумов"},

	///////////////////////////////////////////////////
	// Main Window Tabbed Pane
	///////////////////////////////////////////////////
	{"Search", "Поиск"},
	{"Downloads", "Скачивания"},
	{"Uploads", "Закрузки"},
	{"News", "Форумы"},
	{"Status", "Статус"},

	///////////////////////////////////////////////////
    // Main Window News Tab
	///////////////////////////////////////////////////
    // ToolBar in News Tab
	{"Save message", "Сохранить сообщение"},
    {"Next unread message","Следующее непрочитаное сообщение"},
	{"New message", "Новое сообщение"},
	{"Reply", "Ответить"},
	{"Update", "Обновить"},
	{"Download attachment(s)", "Скачать приложение(я)"},
	{"Add Board(s)", "Добавить форум(ы)"},
    {"Add Board(s) to folder", "Добавить форум(ы) в папку"},
	{"Trust","ДРУГ"},
	{"Set to CHECK", "НЕЗНАКОМЫЙ"},
    {"Set to OBSERVE", "ТОВАРИШЬ"},
	{"Do not trust","ВРАГ"},

	// Popup over message table
	{"Mark message unread", "Пометить как непрочитаное"},
	{"Mark ALL messages read", "Пометить ВСЕ сообщения прочитанными"},
	{"Delete message", "Удалить сообщение"},
	{"Undelete message", "Воскресить сообщение"},
	{"Cancel", "Отмена"},

	// Popup over message (main frame)
	{"Copy","Копировать"},
	{"Save message to disk", "Сохранить сообщение на диск"},

	// Popup over attachments table
	{"Add selected board", "Добавить выделеный форум"},
	{"Download selected attachment", "Скачать выделеные приложения"},

	//Message table header
	{"Sig", "Подпись"},

	///////////////////////////////////////////////////
	// Main Window Search Tab
	///////////////////////////////////////////////////
	// ToolBar in Search tab
	//{"Search", "Search"}, // Defined above
	{"Download selected keys", "Скачать выделенные файлы"},
	{"all boards", "все папки"},

	// Popup over search results table
	{"Download all keys", "Скачать все"},
	{"help user (sets to GOOD)", "помогать ДРУЗЬЯМ"},
	{"block user (sets to BAD)", "блокировать как ВРАГА"},
	{"set to neutral (CHECK)", "пометить как НЕЗНАКОМОГО"},
    {"observe user (OBSERVE)", "пометить как ТОВАРИЩА"},

	//SearchComboBox
	{"All files","Все файлы"},
	{"Audio","Аудио"},
	{"Video","Видео"},
	{"Images","Изовражения"},
	{"Documents","Документы"},
	{"Executables","Программы"},
	{"Archives","Архивы"},

	///////////////////////////////////////////////////
    // Main Window Downloads Tab
	///////////////////////////////////////////////////
	// ToolBar in Downloads Tab
	{"Activate downloading", "Активизировать скачивания"},
    {"Pause downloading", "Приостановить скачивания"},
	{"Show healing information", "Показать информацию про лечение"},

    // Popup over download table
	{"Restart selected downloads", "Перезапустить выделеные скачивания"},
	{"Enable downloads", "Разрешить скачивания"},
	{"Enable selected downloads", "Разрешить выделеные скачивания"},
	{"Disable selected downloads", "Запретить выделеные скачивания"},
	{"Invert enabled state for selected downloads", "Инвертировать разрешения/запреты на скачивания для выделеных файлов"},
	{"Enable all downloads", "Разрешить все скачивания"},
	{"Disable all downloads", "Запрет на все скачивания"},
	{"Invert enabled state for all downloads", "Инвертировать разрешения/запреты на скачивания для всех файлов"},
	{"Remove", "Убрать"},
	{"Remove selected downloads", "Убрать выделеные скачивания"},
	{"Remove all downloads", "Убрать все скачивания"},
	{"Remove finished downloads", "Убрать закончиные"},

	///////////////////////////////////////////////////
    // Main Window Uploads Tab
	///////////////////////////////////////////////////
	// ToolBar in Uploads Tab
	{"Browse", "Просмотр"},

    // FileChooser in Uploads Tab
	{"Select files you want to upload to the", "Выбери файлы для загрузки на"},
	{"board", "форум"},

	// Popup over uploads table
	{"Remove selected files", "Убрать выделеные"},
	{"Remove all files", "Убрать все"},
	{"Start encoding of selected files", "Начать подготовку выделеных файлов"},
	{"Upload selected files", "Загрузить выделеные файлы"},
	{"Upload all files", "Загрузить все"},
	{"Set prefix for selected files", "Указать префикс для выделеных файлов"},
	{"Set prefix for all files", "Указать префикс для всех файлов"},
	{"Restore default filenames for selected files", "Обычные имена для выделеных файлов"},
	{"Restore default filenames for all files", "Обычные имена для всех файлов"},
	{"Change destination board", "Сменить форум назначения"},
	{"Please enter the prefix you want to use for your files.", "Пожалуйста введи префикс для файлов."},

	///////////////////////////////////////////////////
	// Main Window Board Selection Tree
	///////////////////////////////////////////////////
    // Popup over Tree
	{"Refresh", "Обновить"},
	{"Remove", "Удалить"},
	{"folder", "папка"},
	{"Folder", "Папка"},
	{"board", "форум"},
	{"Board", "Форум"},
	{"Cut", "Вырезать"},
	{"Paste", "Вставить"},
	{"Add new board", "Добавить новый форум"},
	{"Add new folder", "Новая папка"},
	{"Configure selected board", "Конфигурация форума"},
	{"Sort folder", "Сортировать папку"},

	///////////////////////////////////////////////////
    // Main Window Status Bar
	///////////////////////////////////////////////////
	{"Up", "Up"},
	{"Down", "Down"},
	{"TOFUP", "TOFUP"},
	{"TOFDO", "TOFDO"},
	{"Results", "Результаты"},
	{"Files", "Файлы"},
	{"Selected board", "Выбрать форум"},

	///////////////////////////////////////////////////
	// New Message Window
	///////////////////////////////////////////////////
	{"Create message", "Новое сообщение"},
	{"Send message", "Отправить сообщение"},
	{"Add attachment(s)", "Добавить проложение(я)"},
	{"Sign", "Подписать"},
    {"Encrypt for", "Шифровать для"},
	{"Indexed attachments", "Индексировать приложения"},
	{"Should file attachments be added to upload table?", "Добавлять-ли приложения в таблицу загрузок?"},
	{"Board", "Форум"},
	{"From", "От"},
	{"Subject", "Тема"},
	{"Remove", "Убрать"},
	{"Do you want to enter a subject?", "Нужна-ли тема?"},
	{"No subject specified!", "Тема не указана!"},
	{"You must enter a subject!", "Нужно указать тему!"},
	{"You must enter a sender name!", "Нужно имя отправителя!"},
	{"No 'From' specified!", "Имя отправителя не указано!"},
	{"Choose file(s) / directory(s) to attach", "Выбор файлов"},
	{"Choose boards to attach", "Выбор форумов"},

	///////////////////////////////////////////////////
	// About box
	///////////////////////////////////////////////////
	{"About", "О Морозко"},
	{"Version", "Версия"},
	{"Open Source Project (GPL license)", "Open Source Project (GPL license)"},
	{"OK", "OK"},
	{"More", "Больше"},
	{"Less", "Меньше"},
	{"Development:", "Разработчики:"},
	{"Windows Installer:", "Инстолятор для Windows'а:"},
	{"System Tray Executables:", "Системный Трей:"},
	{"Translation Support:", "Помощь с Переводом:"},
	{"Splash Screen Logo:", "Логотип при Старте:"},
	{"Misc code contributions:", "Разнообразные дополнения к коду:"},

	///////////////////////////////////////////////////
	// Preferences
	///////////////////////////////////////////////////
	// More often used translations
	{"On", "Вкл"},
	{"Off", "Выкл"},

	// Downloads Panel
	{"Downloads", "Скачивания"},
	{"Disable downloads", "Запретить скачивания"},
	{"Download directory", "Директория для скачиваемых файлов"},
	{"Restart failed downloads", "Перезапускать неудавшиеся скачивания"},
	{"Maximum number of retries", "Максимум перезапусков"},
	{"Waittime after each try", "Пауза между попытками"},
	{"minutes", "минут"},
	{"Enable requesting of failed download files", "Разрешить запросы для неудавшихся скачиваний"},
	{"Request file after this count of retries", "Запрос после данного количества попыток"},
	{"Number of simultaneous downloads", "Количество скачиваний"},
	{"Number of splitfile threads", "Количество процессов"},
	{"Remove finished downloads every 5 minutes", "Убирать законченые скачивания каждые 5 минут"},
	{"Try to download all segments, even if one fails", "Пробывать скачать все сегменты, даже если один не удался"},
	{"Decode each segment immediately after its download", "Раскодировать каждый сегмент сразу после окончания"},
	{"Select download directory", "Выбор директории для скачивания"},

	// Uploads Panel
	{"Disable uploads", "Отменить загрузки"},
	{"Restart failed uploads", "Перезапускать неудачные загрузки"},
	{"Automatic Indexing", "Автоматическое Индексирование"},
	{"Share Downloads","Распространять Скачивания"},
	{"Sign shared files", "Подписывать загрузки"},
	{"Help spread files from people marked GOOD","Помогать ДРУЗЬЯМ"},
	{"Upload HTL", "ПЖ для загрузок"},
	{"up htl explanation","(чем выше, тем медленней, но лучше)"},
	{"Number of simultaneous uploads", "Количество активных загрузок"},
	{"Number of splitfile threads", "Количество процессов"},
	{"splitfile explanation","(чем выше, тем быстре но требует больше cpu)"},
	{"Upload batch size","Размер оповещёния о загрузках"},
	{"batch explanation", "чем выше, тем быстрее, но мениьше препядствует спаму"},
	{"Index file redundancy","Index file redundancy"},
	{"redundancy explanation", "ещё не работает"},

	// News (1) Panel
	{"Message upload HTL", "ПЖ для загрузки сообщений"},
	{"Message download HTL", "ПЖ для скачивания сообщений"},
	{"Number of days to display", "Дней показывать"},
	{"Number of days to download backwards", "Дней скачивать"},
	{"Message base", "Основание сообщений"},
	{"Signature", "Автоподпись"},

	// News (2) Panel
	{"Block messages with subject containing (separate by ';' )", "Не показывать сообщения с темами содержащими (через ;)"},
	{"Block messages with body containing (separate by ';' )", "Не показывать сообщения содержащие (через ;)"},
	{"Block messages with these attached boards (separate by ';' )", "Не показывать сообщения содержащие данные форумы (через ;)"},
	{"Hide unsigned messages", "Не показывать анонимные сообщения"},
	{"Hide messages flagged BAD", "Не показывать сообщения от ВРАГов"},
	{"Hide messages flagged CHECK", "Не показывать сообщения от НЕЗНАКОМЫХ"},
 	{"Hide messages flagged OBSERVE", "Не показывать сообщения от ТОВАРИЩей"},
	{"Do spam detection", "Пытаться убрать спам"},
	{"Sample interval", "Интервал просмотра"},
	{"hours", "часов"},
	{"Threshold of blocked messages", "Threshold of blocked messages"},

	// News (3) Panel
	{"Automatic update options", "Опции автоматического обновления"},
	{"Minimum update interval of a board", "Минимальный интервал между автообнавлениями"},
	{"Number of concurrently updating boards", "Количество обновляемых форумов"},
	{"Show board update visualization", "Показывать какие форумы обновляются"},
	{"Background color if updating board is selected", "Фон если обновляемый форум выделен"},
	{"Background color if updating board is not selected", "Фон если обновляемый форум не выделен"},
	{"Choose", "Выбор"},
	{"Color", "Цвет"},
	{"Choose updating color of SELECTED boards", "Выбор цвета обновляемого ВЫДЕЛЕННОГО форума"},
	{"Choose updating color of NON-SELECTED boards", "Выбор цвета обновляемого НЕ ВЫДЕЛЕННОГО форума"},
	{"Silently retry failed messages", "Пытаться загрузить сообщения без выдачи ошибок"},
	{"Show deleted messages", "Показывать удалённые сообщения"},

    // Expiration panel
    // TODO:
    {"Expiration", "Годность"},
    {"Number of days before a message expires","Количество дней годности сообщений"},
    {"Keep expired messages in keypool","Оставлять истёкщие сообщения не архивируя"},
    {"Archive expired messages","Архивировать истёкшие сообщения"},
    {"Archive folder","Директория для архивации"},
    {"Delete expired messages from keypool","Удалить истёкшие сообщения"},
    {"Select archive directory","Выбор директории для архивации"},

    // Search Panel
	{"Image Extension", "Расширения изображений"},
	{"Video Extension", "Расширения видео"},
	{"Archive Extension", "Расширения архивов"},
	{"Document Extension", "Расширения документов"},
	{"Audio Extension", "Расширения аудио"},
	{"Executable Extension", "Расширения програм"},
	{"Maximum search results", "Максимальное количество найденный файлов"},
	{"Hide files from people marked BAD","Не показывать файлы от ВРАГов"},
	{"Hide files from anonymous users","Не показывать файлы от анонимных пользователей"},

	// Miscelaneous Panel
	{"Keyfile upload HTL", "ПЖ загрузки ключевого файла"},
	{"Keyfile download HTL", "ПЖ скачивания ключевого файла"},
	{"list of nodes","Список подключений (через ,)"},
	{"list of nodes 2","(nodeA:portA, nodeB:portB, ...)"},
	{"Maximum number of keys to store", "Максимальное количество сохраняемых ключей"},
	{"Allow 2 byte characters", "Разрешение 2-байтовых знаков"},
	{"Use editor for writing messages", "Использовать программу для редактирования сообщений"},
	{"Clean the keypool", "Очистка keypool"},
	{"Automatic saving interval", "Интервал автозаписи"},
	{"Disable splashscreen", "Отменито логотип при загрузке"},
	{"Enable logging", "Разрешить файл журнала"},
	{"Logging level", "Уровень журналирования"},
	{"Log file size limit (in KB)", "Ограничения журнала (в КБ)"},
	{"Very high", "Очень высокий"},
	{"High", "Высокий"},
	{"Medium", "Средний"},
	{"Low", "Низкий"},
	{"Very low", "Очень низкий"},

    // Display Panel
    {"EnableSkins", "Разрешить шкурки"},
	{"MoreSkinsAt", "Дополнительные шкурки можно скачать на"},
	{"Preview","Предпросмотр"},
	{"RefreshList","Обновить список"},
	{"NoSkinsFound","Скурок не найдено!"},
	{"AvailableSkins","Доступные шкурки"},
	{"Plain","Обычный"},
	{"Italic","Курсив"},
	{"Bold","Жирный"},
	{"Bold Italic","Жирный Курсив"},
	{"Sample","Пример"},
	{"Choose a Font","Выбор шрифта"},
	{"Fonts","Шрифты"},
	{"Message Body","Сообщение"},
	{"Message List","Список сообщений"},
	{"File List","Список файлов"},
	{"Choose","Выбор"},
	{"EnableMessageBodyAA", "Разрешить сглаживание кривых в сообщениях"},

	///////////////////////////////////////////////////
	// Board Information Window
	///////////////////////////////////////////////////
	{"BoardInfoFrame.UpdateSelectedBoardButton","Обновить выделенные форумы"},
	{"BoardInfoFrame.Update","Обновить"},
	{"BoardInfoFrame.Update all boards","Обновить все форумы"},
	{"BoardInfoFrame.Close","Закрыть"},
	{"BoardInfoFrame.Board information window","Информация о форумах"},
	{"BoardInfoFrame.Boards","Форумы"},
	{"BoardInfoFrame.Messages","Сообщения"},
	{"BoardInfoFrame.Files","Файлы"},

	// Board information window table
	{"Messages","Сообщения"},

	///////////////////////////////////////////////////
	// List of known boards window
	///////////////////////////////////////////////////
	{"KnownBoardsFrame.List of known boards","Список известных форумов"},
	{"KnownBoardsFrame.Close","Закрыть"},
	{"KnownBoardsFrame.Lookup","Просмотреть"},
    {"KnownBoardsFrame.Filter","Фильтр"},
	{"KnownBoardsTableModel.Boardname","Имя форума"},

	///////////////////////////////////////////////////
	// Core start messages
	///////////////////////////////////////////////////

	{"Core.init.NodeNotRunningBody","Убедись что фринет запущен и правильно работает.\n"
									+ "Морозко всё ещё будет запущен для чтения сообщений.\n"
                                    + "Внимание! Автоматическое обновление будет отменино!\n"
									+ "Возможно будут выдаваться ошибки... без паники ;)\n"},
	{"Core.init.NodeNotRunningTitle","Ошибка - соединение с фринетом не установлено."},
	{"Core.init.TransientNodeBody","You are running a TRANSIENT node. Better run a PERMANENT freenet node."},
	{"Core.init.TransientNodeTitle","Transient node detected"},
	{"Core.loadIdentities.ConnectionNotEstablishedBody","Frost could not establish a connection to your freenet node(s).\n"
									+ "For first setup of Frost and creating your identity a connection is needed,\n"
									+ "later you can run Frost without a connection.\n"
									+ "Please ensure that you are online and freenet is running, then restart Frost."},
	{"Core.loadIdentities.ConnectionNotEstablishedTitle","Соединение с фринетом не установлено"},
	{"Core.loadIdentities.ChooseName","Выбери псевдоним, он не должен быть уникальным\n"},
	{"Core.loadIdentities.InvalidNameBody","Псевдоним не может содержать '@'!"},
	{"Core.loadIdentities.InvalidNameTitle","Псевдоним неправильный"},

	//Board Settings Dialog
	{"Settings for board","Настройки форума"},
	{"Override default settings","Использовать нестандартные настройки"},
	{"Use default","Использовать стандарт"},
	{"Set to","Установить"},
	{"Yes","Да"},
	{"No","Нет"},
	{"Enable automatic board update","Разрешить автоматическое обновление"},
	{"Maximum message display (days)","Показывать дней"},
	{"Warning","Осторожно"},

	//	Uploads underway warning when exiting
	 {"UploadsUnderway.title","Активные загрузки сообщений"},
	 {"UploadsUnderway.body","Некоторые сообщения всё ещё загружаются.\n"
							 + "Всё равно выходить?"},

	//email notification stuff
	{"SMTP.server","Адрес сервера"},
	{"SMTP.username","имя"},
	{"SMTP.password","пароль"},
	{"Email.address","отсылать нотификации"},

	///
	///	TofTree
	///

	{"New Folder Name","Имя новой папки"},
	{"New Node Name","Имя нового форума"},
	{"newboard","Новый форум"},
	{"newfolder","Новая папка"},
	{"Please enter a name for the new board","Имя для нового форума"},
	{"Please enter a name for the new folder","Имя для новой папки"},
	{"You already have a board with name","Форум с данным именем уже существует"},
	{"Please choose a new name","Нужно новое имя"},
	{"Do you really want to overwrite it?","Действительно перезаписать?"},
	{"This will not delete messages","Это не удалит сообщений"},

	///
	///	SearchTableFormat
	///

	{"FrostSearchItemObject.Offline","Недоступет"},
	{"FrostSearchItemObject.Anonymous","Анонимный"},
	{"SearchTableFormat.Uploading","Загружается"},
	{"SearchTableFormat.Downloading","Скачивается"},
	{"SearchTableFormat.Downloaded","Скачался"},

	///
	/// DownloadTableFormat
	///

	{"DownloadTableFormat.Enabled", "Разрешён"},
	{"Blocks", "Блоков"},
	{"Tries", "Попыток"},
	{"Waiting", "Ожидание"},
	{"Trying", "Попытка"},
	{"Done", "Завершён"},
	{"Failed", "Неудача"},
	{"Requesting","Запрашиватся"},
	{"Requested","Запрошен"},
	{"Decoding segment","Сегмент разкодируется"},

	///
	/// UploadTableFormat
	///

	{"Never","Никогда"},
	{"Uploading","Загрузка"},
	{"Encode requested","Кодирование запрошено"},
	{"Encoding file","Кодируется"},
	{"Unknown", "Неизвестно"},

	///
	///	NewBoardDialog
	///

	{"NewBoardDialog.title", "Добавить новый форум"},
	{"NewBoardDialog.details", "Настройки:"},
	{"NewBoardDialog.name","Имя:"},
	{"NewBoardDialog.description","Описание (не безательно, но желательно) видемо всеми (не для личной информации):"},
	{"NewBoardDialog.add","Добавить форум"},

	///
	/// BoardSettingsFrame
	///

	{"BoardSettingsFrame.description","Описание:"},
	{"BoardSettingsFrame.confirmTitle","Сделать форум общественным?"},
	{"BoardSettingsFrame.confirmBody","Это приведёт к потере ключей. Продолжать?"},

	///
	/// Misc (appear in several classes)
	///

	{"Description","Описание"},

	//	Frost startup error messages
	{"Frost.lockFileFound", "Морозко уже запущен из данной директории.\n" +
	  					  	"Это может привести к потере информации.\n"+
							"При уверености что это ошибка нужно удалить файл блокировки:\n"},

	//
	//  Message upload failed dialog
	//
	{"Upload of message failed", "Загрузка сообщения не удалась"},
	{"Frost was not able to upload your message.", "Морозко не смог загрузить сообщение."},
	{"Retry", "Попробывать стова"},
	{"Retry on next startup", "Попробывать в следующий раз"},
	{"Discard message", "Удалить сообщение"},

	//AttachedBoardTableModel
	{"Board Name", "Имя форума"},
	{"Access rights", "Права доступа"},

	//Saver popup
	{"Saver.AutoTask.title", "Проблема."},
	{"Saver.AutoTask.message", "Морозко нашёл ошибку во время записи и будет закрыт."},

	// Popup over message body (message frame)
	{"Cut","Вырезать"},
	{"Paste","Вставить"},

	// Attach Boards Chooser (message frame)
	{"MessageFrame.ConfirmBody1", "У тебя есть ключ для загрузок на данный форум '"},
	{"MessageFrame.ConfirmBody2", "'.      Действительно нужно его распространять?\n" +
								  "При ответе нет только ключ чтения будет добавлен.",},
	{"MessageFrame.ConfirmTitle", "Добавлять ключ загрузок?"},

	// Status bar
	{"UploadStatusPanel.Uploading", "Загружается:"},
	{"StatusPanel.file", "файло"},
    {"StatusPanel.files", "файлов"},
	{"DownloadStatusPanel.Downloading", "Скачивается:"},

	// Copy to clipboard submenu
	{"Copy to clipboard", "Копировать"},
	{"Copy keys only", "Копировать только ключи"},
	{"Copy keys with filenames", "Копировать ключи и имя"},
	{"Copy extended info", "Копировать дополнительную информацию"},
	{"Key not available yet", "Ключ ещё не доступен"},
	{"clipboard.File:",  "Файл:   "},	//These three strings are a special case.
	{"clipboard.Key:",   "Ключ:   "},	//They must have the same length so that the
	{"clipboard.Bytes:", "Байтов: "}	//format of the output is preserved.

    };
}
