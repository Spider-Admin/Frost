* Frost - text ueber freenet *
_----------------------------_

Frost kann man auf Freenet 0.5 und Freenet 0.7 laufen lassen. Waehrend des
ersten Starts von Frost kann man waehlen, fuer welche Freenet Version dieses
Frost gedacht ist. Jede Frost Installation kann nur entweder auf
Freenet 0.5 oder auf Freenet 0.7 laufen, nicht auf beiden gleichzeitig, da die
Freenet Keys ein anderes Format haben.

Frost nimmt an, das er das erste Mal gestartet wird wenn, er keine Datei
'identities.xml' findet. Dann erscheint der Startdialog, in dem man die
gewuenschte Freenet Version waehlen kann, und falls man moechte,
kann eine bestehende identities.xml Datei einer anderen Frost-
installation (0.5 oder 0.7) importiert werden.


Frost 0.5 oder 0.7 auf eine neuere Version updaten:
----------------------------------------------------
Beende Frost falls es laeuft und kopiere den inhalt der heruntergeladenen
ZIP-Datei in den bestehenden Frost Ordner. Einfach alle Dateien ueberschreiben.
Alle Einstellungen, nachrichten usw. bleiben dabei erhalten. Aber wie immer
empfehlen wir eine Sicherung des Frost-Ordners vor dem Update :)


Frost 0.5 laeuft schon und du moechtest jetzt auch Frost 0.7 verwenden (oder andersrum):
-----------------------------------------------------------------------------------------
Entpacke den Inhalt der heruntergeladenen ZIP Datei in einen NEUEN Ordner.
Dann starte Frost. In dem ersten Startdialog kann man waehlen fuer welches
Freenet diese Frost Installation gedacht ist, und optional kann eine
existierende identities.xml Datei importiert werden (von Frost 0.5 oder 0.7).
Die Frost Installation, von der die identities.xml Datei importiert wird,
sollte auf jeden Fall beendet sein. Oder du erstellst einfach eine neue
Identitaet. ;)


Problemloesungen:
------------------
Frost nimmt an, das der Freenet Konten auf dem selben Computer wie Frost laeuft
und den Standard FCP Port verwendet. Fuer Freenet 0.5 ist das "127.0.0.1:8481",
und fuer Freenet 0.7 "127.0.0.1:9481".
Wenn dein Freenet Knoten auf einem anderen Rechner laeuft, oder einen anderen
FCP-Port konfiguriert ist, schlaegt der erste Start von Frost fehl. In diesem
Fall musst du die Datei 'frost.ini' im 'config' Ordner per hand aendern. Die
frost.ini wird beim ersten Start automatisch erstellt. Öffne die frost.ini mit
einem Text Editor und suche die Zeile mit dem Inhalt
'availableNodes=127.0.0.1:8481'. Ändere nun einfach die Einstellung so, wie du
sie benoetigst, z.B. 'availableNodes=andererrechner.de:12345'. Dann starte
Frost, nun sollte die Verbindung zum Freenet Knoten klappen.
Und immer daran denken: wenn Frost auf einem anderen Rechner laeuft, muss auch
in der Konfiguration des Freenetknotens erlaubt werden, das sich andere Rechner
(insbesondere deiner:) mit dem Knoten verbinden duerfen!
Wenn Frost einmal gestartet ist, kann man die 'availableNodes' Einstellung auch
im Optionen Dialog von Frost vornehmen.

Wenn (und NUR wenn) du versehentlich beim Update eine flasche Freenet Version gewählt
hast, musst du die frost.ini Datei per Hand ändern. Finde in der Datei die Zeile
"freenetVersion=" und ändere sie zu "freenetVersion=05" oder "freenetVersion=07".