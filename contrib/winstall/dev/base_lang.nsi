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

# Translator : first_name last_name <email_address>

!include versions.nsh

# Define your language code here
!define LANG "xx"

# Section names
!define SEC_DESKTOPICON "Desktop icon"
!define SEC_ICONS "Startmenu icons"

# Details strings
!define DET_COPYING "Copying files..."

# Mic strings (in messagebox...)
!define STR_FREENETNOTFOUND "Frost requires Freenet to work, please install it first."

# Link strings
!define LNK_FROSTCONSOLE "Frost (console)"
!define LNK_HOMEPAGE "Homepage"
!define LNK_UNINSTALL "Uninstall"

# Installation types
InstType /CUSTOMSTRING=Custom
InstType Minimal
InstType Complete

# Dialogs
LicenseText "Frost is published under the GNU general public license:" "I agree"
ComponentText "This will install Frost ${VERSION} on your system." "Select the type of install:" "or, select the optional components you wish to install:"
DirText "No files will be placed outside this directory (e.g. Windows\system)"  "Select the directory to install Frost in:" "Browse..."
UninstallText "This uninstalls Frost from your system" "Uninstalling from: "

# Captions
Caption "Frost installation"
SubCaption 0 ": License Agreement"
SubCaption 1 ": Installation Options"
SubCaption 2 ": Installation Directory"
SubCaption 3 ": Installing Files"
SubCaption 4 ": Completed"

UninstallCaption "Frost Uninstall"
UninstallSubCaption 0 ": Confirmation"
UninstallSubCaption 1 ": Uninstalling Files"
UninstallSubCaption 2 ": Completed"

# Buttons
MiscButtonText "< Back" "Next >" "Cancel" "Close"
DetailsButtonText "Details"
InstallButtonText "Install"
UninstallButtonText "Uninstall"

# Misc
SpaceTexts "Space required: " "Space available: "
CompletedText "Completed"

!include frost.nsi