# Frost Windows Installer
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

# Translator : Benoit Laniel <nels@pgroupe.net>
# Last check : 011207

!include config.nsh
!define LANG "fr"

# Section names
!define SEC_DESKTOPICON "Icône sur le bureau"
!define SEC_ICONS "Icônes du menu démarrer"

# Details strings
!define DET_COPYING "Copie des fichiers..."

# Mic strings (in messagebox...)
!define STR_JAVANOTFOUND "Frost nécessite Java pour fonctionner, veuillez d'abord l'installer (aucun java.exe/javaw.exe n'a été trouvé)."

# Link strings
!define LNK_FROSTCONSOLE "Frost (console)"
!define LNK_HOMEPAGE "Page d'accueil"
!define LNK_UNINSTALL "Désinstaller"

# Installation types
InstType /CUSTOMSTRING=Personnalisée
InstType Minimale
InstType Complète

# Dialogs
LicenseText "Frost est distribué sous la licence publique générale GNU:" "J'accepte"
ComponentText "Ceci installera Frost ${CONF_VERSION} sur votre système." "Sélectionnez le type d'installation:" "ou, sélectionnez les composants à installer:"
DirText "Aucun fichier ne sera placé en dehors de ce dossier (c.a.d. Windows\system)"  "Sélectionnez le dossier dans lequel Frost sera installé:" "Parcourir..."
UninstallText "Ceci désinstallera Frost de votre système" "à partir de: "

# Captions
Caption "Installation de Frost"
SubCaption 0 ": Accord de licence"
SubCaption 1 ": Options d'installation"
SubCaption 2 ": Dossier d'installation"
SubCaption 3 ": Installation des fichiers"
SubCaption 4 ": Terminé"

UninstallCaption "Désinstallation de Frost"
UninstallSubCaption 0 ": Confirmation"
UninstallSubCaption 1 ": Désinstallation des fichiers"
UninstallSubCaption 2 ": Terminé"

# Buttons
MiscButtonText "< Précédent" "Suivant >" "Annuler" "Fermer"
DetailsButtonText "Détails"
InstallButtonText "Installer"
UninstallButtonText "Désinstaller"

# Misc
SpaceTexts "Espace requis: " "Espace disponible: "
CompletedText "Terminé"

!include frost.nsi