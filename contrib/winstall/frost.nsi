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

# Used strings:
# $1 : javaw.exe path
# $2 : Installed flag ('Yes' if Frost was already installed, otherwise 'No')
# $3 : java.exe path

Name "Frost ${VERSION}"
OutFile "${LANG}.exe"
Icon jtc_inst.ico

LicenseData "data\frost\gpl.txt"

UninstallExeName Uninstall-Frost.exe

InstProgressFlags smooth
# !packhdr will further optimize your installer package if you have upx.exe in your directory
!packhdr temp.dat "upx.exe --best temp.dat"

InstallDir "$PROGRAMFILES\Frost"
InstallDirRegKey HKEY_LOCAL_MACHINE "Software\Frost" "InstPath"

;-----------------------------------------------------------------------------------
Section
  # Required section which copies all the files

  # Have to say to the installer that Frost is 696 Kb since we only copy files
  AddSize 696

  # -- Assume Frost is already installed
  StrCpy $2 "Yes"
  # -- Check if gpg.exe exists
  IfFileExists "$INSTDIR\classes\frost.class" DoUpdate
  StrCpy $2 "No"

DoUpdate:
  DetailPrint "${DET_COPYING}"
  SetOutPath "$INSTDIR"
  CreateDirectory "$INSTDIR\downloads"
  CreateDirectory "$INSTDIR\keypool"
  CopyFiles "$EXEDIR\frost\*.*" "$INSTDIR"

  # Register installpath
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Frost" "InstPath" '$INSTDIR'

  # Create uninstall keys
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\Frost" "DisplayName" "Frost ${VERSION}"
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\Frost" "UninstallString" '"$INSTDIR\Uninstall-Frost.exe"'
SectionEnd

;-----------------------------------------------------------------------------------
Section "${SEC_DESKTOPICON}"
  # This section creates a desktop icon
  SectionIn 1,2

  CreateShortCut "$DESKTOP\Frost.lnk" "$1" "-cp .;classes;data frost" "$INSTDIR\jtc.ico" 0
SectionEnd

;-----------------------------------------------------------------------------------
Section "${SEC_ICONS}"
  # This section creates the startmenu icons
  SectionIn 1,2

  CreateDirectory "$SMPROGRAMS\Frost"
  CreateShortCut "$SMPROGRAMS\Frost\Frost.lnk" "$1" "-cp .;classes;data frost" "$INSTDIR\jtc.ico" 0
  CreateShortCut "$SMPROGRAMS\Frost\${LNK_FROSTCONSOLE}.lnk" "$3" "-cp .;classes;data frost" "$INSTDIR\jtc.ico" 0
  CreateShortCut "$SMPROGRAMS\Frost\${LNK_UNINSTALL}.lnk" "$INSTDIR\Uninstall-Frost.exe" "" "$INSTDIR\Uninstall-Frost.exe" 0
  WriteINIStr "$SMPROGRAMS\Frost\${LNK_HOMEPAGE}.url" "InternetShortcut" "URL" "http://jtcfrost.sourceforge.net"
SectionEnd

;-----------------------------------------------------------------------------------
Section "Uninstall"
  # Uninstall section
  RMDir /r "$INSTDIR"
  Delete "$DESKTOP\Frost.lnk"
  RMDir /r "$SMPROGRAMS\Frost"

  DeleteRegKey HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\Frost"
  DeleteRegKey HKEY_LOCAL_MACHINE "Software\Frost"
SectionEnd

;-----------------------------------------------------------------------------------
Function .onInit
  # Check if Freenet is installed
  ReadRegStr $0 HKEY_LOCAL_MACHINE "Software\Freenet" "instpath"
  StrCmp $0 "" NoFreenet
  Goto End

NoFreenet:
  # No, then abort installation
  MessageBox MB_OK|MB_ICONSTOP "${STR_FREENETNOTFOUND}"
  Abort

End:
  # Yes, then get the java path ;)
  ReadINIStr $3 "$0\FLaunch.ini" "Freenet Launcher" "java"
  ReadINIStr $1 "$0\FLaunch.ini" "Freenet Launcher" "javaw"
FunctionEnd

;-----------------------------------------------------------------------------------
Function .onInstFailed
  # -- Do not delete INSTDIR if frost.class was there before installation
  StrCmp $2 "Yes" DontDelete
  RMDir /r "$INSTDIR"

DontDelete:
FunctionEnd