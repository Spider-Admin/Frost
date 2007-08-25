* Frost - text ueber freenet *
_----------------------------_

Frost kann man auf Freenet 0.5 und Freenet 0.7 laufen lassen. Waehrend des
ersten Starts von Frost kann man waehlen, fuer welche Freenet Version dieses
Frost gedacht ist. Jede Frost Installation kann nur entweder auf
Freenet 0.5 oder auf Freenet 0.7 laufen, nicht auf beiden gleichzeitig, da die
Freenet Keys ein anderes Format haben.

Beim ersten Start von Frost kann man die gewuenschte Freenet Version waehlen,
und eine existierende identities.xml Datei importieren. Diese Datei muss man
vorher im alten Frost exportieren. Man kann aus dem alten Frost auch seine
eigenenen Identitaeten exportieren, und diese nach dem Start ins neue Frost
importieren.

ACHTUNG: Man sollte immer eine Sicherheitskopie seiner eigenen Identitaeten
         an einem sicheren Ort aufbewahren!


Frost Version 23-Dec-2006 oder neuer auf eine neuere Version updaten:
----------------------------------------------------------------------
Beende Frost falls es laeuft und kopiere den Inhalt der heruntergeladenen
ZIP-Datei in den bestehenden Frost Ordner. Einfach alle Dateien ueberschreiben.
Alle Einstellungen, nachrichten usw. bleiben dabei erhalten. Aber wie immer
empfehlen wir eine Sicherung des Frost-Ordners vor dem Update :)

ACHTUNG: Wenn die Datei store/applayerdb.conf geaendert wurde (z.B. um die
          Datenbank an einen anderen Ort zu legen) dann diese Datei nicht
          ueberschreiben lassen!


Frost aelter als Version 23-Dec-2006 auf eine neuere Version updaten:
----------------------------------------------------------------------
Ein direktes Update von aelteren Versionen ist nicht moeglich. Man muss zuerst
zur Version 23-Dec-2006 (oder neuer, bis max. 21-Apr-2007) hochziehen, und dann
zu der neuesten Version.


Frost 0.5 laeuft schon und du moechtest jetzt auch Frost 0.7 verwenden (oder andersrum):
-----------------------------------------------------------------------------------------
Entpacke den Inhalt der heruntergeladenen ZIP Datei in einen NEUEN Ordner.
Dann starte Frost. In dem ersten Startdialog kann man waehlen fuer welches
Freenet diese Frost Installation gedacht ist. Lege eine neue Identitaet an,
diese kann spaeter wieder geloescht werden. Exportiere die eigenen Identitaeten
aus dem alten Frost und importiere sie in das neue Frost (wenn gewuenscht).


Problemloesungen:
------------------
Frost nimmt an, das der Freenet Konten auf dem selben Computer wie Frost laeuft
und den Standard FCP Port verwendet. Fuer Freenet 0.5 ist das "127.0.0.1:8481",
und fuer Freenet 0.7 "127.0.0.1:9481".
Wenn dein Freenet Knoten auf einem anderen Rechner laeuft, oder einen anderen
FCP-Port konfiguriert ist, schlaegt der erste Start von Frost fehl. In diesem
Fall musst du die Datei 'frost.ini' im 'config' Ordner per hand aendern. Die
frost.ini wird beim ersten Start automatisch erstellt. �ffne die frost.ini mit
einem Text Editor und suche die Zeile mit dem Inhalt
'availableNodes=127.0.0.1:8481'. �ndere nun einfach die Einstellung so, wie du
sie benoetigst, z.B. 'availableNodes=andererrechner.de:12345'. Dann starte
Frost, nun sollte die Verbindung zum Freenet Knoten klappen.
Und immer daran denken: wenn Frost auf einem anderen Rechner laeuft, muss auch
in der Konfiguration des Freenetknotens erlaubt werden, das sich andere Rechner
(insbesondere deiner:) mit dem Knoten verbinden duerfen!
Wenn Frost einmal gestartet ist, kann man die 'availableNodes' Einstellung auch
im Optionen Dialog von Frost vornehmen.

Wenn (und NUR wenn) du versehentlich beim Update eine flasche Freenet Version gew�hlt
hast, musst du die frost.ini Datei per Hand �ndern. Finde in der Datei die Zeile
"freenetVersion=" und �ndere sie zu "freenetVersion=05" oder "freenetVersion=07".


Hinweis fuer U*ix Benutzer:
----------------------------
Nach dem Entpacken der ZIP Datei sind die *.sh Dateien evtl. noch nicht ausfuehrbar.
Um das 'executable' Bit zu setzen, fuehre folgendes Kommando im Frost Ordner aus:
"chmod +x *.sh".

Hinweis fuer Beryl Benutzer (oder wenn sich nur ein graues Fenster auftut):
----------------------------
Wenn ihr als Fenstermanager Beryl benutzt, m�sst ihr eine Zeile in die Datei frost.sh
einf�gen. Diese befindet sich in eurem Frost-Verzeichnis (meistens ~/Freenet/frost/).
 
 export AWT_TOOLKIT="MToolkit"
 
 Das sollte dann so aussehen:
 [...]
 cd $PROGDIR
 
 export AWT_TOOLKIT="MToolkit"
 java -jar frost.jar "$@"
 [...]

