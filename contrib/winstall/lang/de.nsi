# Copyright (C) 2001 Benoit Laniel <nels@pgroupe.net>
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

# Translator : Jan-Thomas Czornack <jantho@users.sourceforge.net>

!include config.nsh
!define LANG "de"

# Section names
!define SEC_DESKTOPICON "Desktop Icon"
!define SEC_ICONS "Startmenü Icons"

# Details strings
!define DET_COPYING "Kopiere Dateien..."

# Mic strings (in messagebox...)
!define STR_FREENETNOTFOUND "Frost benötigt zur Ausführung Freenet. Bitte installieren Sie es zuerst."

# Link strings
!define LNK_HOMEPAGE "Homepage"
!define LNK_UNINSTALL "Uninstall"

# Installation types
InstType /CUSTOMSTRING=Custom
InstType Minimal
InstType Complete

# Dialogs
LicenseText "Frost untersteht der GNU general public license:" "Einverstanden"
ComponentText "Dies wird Frost ${VERSION} auf Ihrem System installieren." "Bitte Installationstyp wählen:" "oder wählen Sie optionale Komponenten, die Sie installieren wollen:"
DirText "Keine Dateien werden ausserhalb dieses Verzeichnisses installiert (z.B. Windows\system)"  "Bitte wählen Sie das Zielverzeichnis in dem Frost installiert werden soll:" "Durchsuchen..."
UninstallText "Dies deinstalliert Frost von Ihrem System" "Deinstalliere von: "

# Captions
Caption "Frost Installation"
SubCaption 0 ": Lizenzbestimmungen"
SubCaption 1 ": Installationseinstellungen"
SubCaption 2 ": Installationsverzeichnis"
SubCaption 3 ": Installiere Dateien"
SubCaption 4 ": Fertig"

UninstallCaption "Frost Deinstallation"
UninstallSubCaption 0 ": Bestätigung"
UninstallSubCaption 1 ": Lösche Dateien"
UninstallSubCaption 2 ": Fertig"

# Buttons
MiscButtonText "< Zurück" "Weiter >" "Abbruch" "Schliessen"
DetailsButtonText "Details"
InstallButtonText "Installiere"
UninstallButtonText "Deinstalliere"

# Misc
SpaceTexts "Benötigter Speicher: " "Verfügbarer Speicher: "
CompletedText "Fertig"

!include frost.nsi