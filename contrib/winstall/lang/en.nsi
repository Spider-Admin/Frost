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

# Last check : 011207

!include config.nsh
!define LANG "en"

# Section names
!define SEC_DESKTOPICON "Desktop icon"
!define SEC_ICONS "Startmenu icons"

# Details strings
!define DET_COPYING "Copying files..."

# Mic strings (in messagebox...)
!define STR_JAVANOTFOUND "Frost requires Java to work, please install it first (no java.exe/javaw.exe was found)."

# Link strings
!define LNK_FROSTCONSOLE "Frost (console)"
!define LNK_HOMEPAGE "Homepage"
!define LNK_UNINSTALL "Uninstall"

# Installation types
InstType Minimal
InstType Complete

# Misc strings
LicenseText "Frost is published under the GNU general public license:"
ComponentText "This will install Frost ${CONF_VERSION} on your system."
DirText "No files will be placed outside this directory (e.g. Windows\system)"
UninstallText "This uninstalls Frost"

!include frost.nsi