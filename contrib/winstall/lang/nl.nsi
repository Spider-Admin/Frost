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

# Translator : Huib Kleinhout <huib@stack.nl>
# Last check : 011214

!include config.nsh

# Define your language code here
!define LANG "nl"

# Section names
!define SEC_DESKTOPICON "Desktop icoon"
!define SEC_ICONS "Startmenu iconen"

# Details strings
!define DET_COPYING "Bestanden kopieeren..."

# Mic strings (in messagebox...)
!define STR_JAVANOTFOUND "Java is nodig om Frost te kunnen laten werken, installeer dit eerst (geen java.exe/javaw.exe gevonden)."

# Link strings
!define LNK_FROSTCONSOLE "Frost (console)"
!define LNK_HOMEPAGE "Homepage"
!define LNK_UNINSTALL "Installatie verwijderen"

# Installation types
InstType /CUSTOMSTRING=Aangepast
InstType Minimaal
InstType Compleet

# Dialogs
LicenseText "Frost is gepubliceerd onder de GNU general public license:" "Ik ga akkoord"
ComponentText "Dit programma zal Frost ${CONF_VERSION} op uw systeem installeren." "Selecteer het type installatie:" "of, selecteer de optionele componenten die u wilt installeren:"
DirText "Er zullen geen bestanden buiten deze map worden geplaatst (bijv. Windows\system)"  "Selecteer de map waar u Frost wilt installeren:" "Bladeren..."
UninstallText "Dit programma zal Frost van uw systeem verwijderen" "Deinstalleren van: "

# Captions
Caption "Frost installatie"
SubCaption 0 ": Gebruiksovereenkomst"
SubCaption 1 ": Installatie Opties"
SubCaption 2 ": Installatie Map"
SubCaption 3 ": Installeren bestanden"
SubCaption 4 ": Einde"

UninstallCaption "Frost verwijderen"
UninstallSubCaption 0 ": Bevestiging"
UninstallSubCaption 1 ": Verwijderen bestanden"
UninstallSubCaption 2 ": Einde"

# Buttons
MiscButtonText "< Vorige" "Volgende >" "Annuleren" "Sluiten"
DetailsButtonText "Details"
InstallButtonText "Installeren"
UninstallButtonText "Deinstalleren"

# Misc
SpaceTexts "Ruimte benodigd: " "Ruimte beschikbaar: "
CompletedText "Klaar"

!include frost.nsi
