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

!include config.nsh

Name "Frost ${VERSION}"
OutFile "frost_${VERSION}.exe"
Icon jtc_inst.ico
SilentInstall silent

Section
  SetOutPath "$TEMP\frost_inst"
  File /r "data\*.*"

  # -- Get language settings
  ReadRegStr $1 HKCU "Control Panel\International" "Locale"

  # -- Set english as default language
  StrCpy $2 "en.exe"

  StrCmp $1 "0000040C" Fr
  StrCmp $1 "0000080C" Fr
  StrCmp $1 "00000C0C" Fr
  StrCmp $1 "0000100C" Fr
  StrCmp $1 "0000140C" Fr
  
  StrCmp $1 "00000407" De
  StrCmp $1 "00000807" De
  StrCmp $1 "00000C07" De
  StrCmp $1 "00001007" De
  StrCmp $1 "00001407" De

  Goto End

Fr:
  IfFileExists "$TEMP\frost_inst\fr.exe" FrExists
  Goto End

FrExists:
  StrCpy $2 "fr.exe"
  Goto End

De:
  IfFileExists "$TEMP\frost_inst\de.exe" DeExists
  Goto End

DeExists:
  StrCpy $2 "de.exe"
  Goto End

End:
  ExecWait "$TEMP\frost_inst\$2"
  RMDir /r "$TEMP\frost_inst"
SectionEnd