                   Frost Windows Installer
                   =======================


What do you need?
-----------------

You need NSIS >=1.60 to compile the installer.
You can find it at http://www.firehose.net/free/nsis

You can optionally get UPX to compress the installer headers.
It can be found at http://upx.sf.net

How to compile the installer?
-----------------------------

1) Copy all of your frost files in 'data\frost'
2) Edit versions.nsh and change it to match the current version of Frost
3) Edit make.bat and define the path to makensis.exe
4) Execute make.bat
5) Have fun with Frost ;)

How to translate the installer?
-------------------------------

1) Copy the dev\base_lang.nsi file to lang\xx.nsi (where xx is the language code)
2) Put your name and your email address in file
3) Translate all the strings in xx.nsi
4) Send me the script so I can include it...

Note:
-----

Type 'make clean' to clean directories.