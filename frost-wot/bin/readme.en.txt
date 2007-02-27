* Frost - text over freenet *
_---------------------------_

Frost can run on Freenet 0.5 and Freenet 0.7. During the first startup of
Frost you decide which Freenet version is used. Each installed Frost
instance can only run with one Freenet version, you can't run one Frost
instance on both Freenet versions, because the freenet key format is different.

The first startup dialog allows you to choose the Freenet version, and
optionally you can import an existing identities.xml file from another installed
Frost instance (0.5 or 0.7). You must export the identities from the existing
Frost installation. You can also export your own identities, and import them
into the new installion after the first startup.

NOTE: You should always have a backup of your own identities. Export your own
      identities and store the file on a save place.


Update from a Frost 23-Dec-2006 or higher to latest version:
-------------------------------------------------------------

Stop Frost if it is running, and copy the contents of the downloaded ZIP file
over your existing Frost installation, replace all existing files.

NOTE: All your settings, messages, etc... will be preserved, but making a backup
      of your Frost directory prior to updating is strongly recommended just in
      case something goes wrong.

NOTE: If you changed the file store/applayerdb.conf for any reason (e.g. you
      moved your database to another place), do NOT overwrite this file!


Update from Frost 20-Jun-2006 to latest version:
-------------------------------------------------

Do NOT update your existing Frost! Start a new installation in a new folder.

Download the zip file, extract it into a NEW folder and start Frost as usual.
The first dialog allows you to choose: start a clean new Frost, or import
messages and identities from an existing Frost version 20-Jun-2006.
The import could take some time, depending on the amount of messages you have
in the keypool and in your archive.


Update from Frost older than 20-Jun-2006 to latest version:
------------------------------------------------------------

You can't update from versions older than 20-Jun-2006 to the latest version.
Upgrade to version 20-Jun-2006 first, then to a later version.


You have Frost 0.5 running and want to start to use Frost 0.7 (or vice versa):
-------------------------------------------------------------------------------
Copy the contents of the downloaded ZIP file into a NEW directory and start
Frost. In the first startup dialog, choose the Freenet version you want to use
with this Frost installation. Create an identity (you can delete it later).
Export your own identities from the existing Frost installation, and import
them into the new Frost installation if you want to use your existing
identities.


Troubleshooting:
-----------------
Frost assumes that your Freenet node runs on the same machine with the default
FCP port settings. For Freenet 0.5 this is "127.0.0.1:8481", and for
Freenet 0.7 its "127.0.0.1:9481". If your Freenet node runs on another machine,
or if you configured another FCP client port the connection to the node will
fail and Frost can't start during the first startup. In this case you need to
edit the 'frost.ini' file that can be found in the 'config' directory.
The 'frost.ini' file is automatically created during first startup of Frost.
Open the 'frost.ini' with a text editor and find the line
containing 'availableNodes=127.0.0.1:8481'. Change the setting to fit your
needs (e.g. 'availableNodes=otherhost:12345'), and then start Frost.
It should now be able to connect to your Freenet node.
Be aware that you maybe have to configure the Freenet node to allow
connections from different hosts than localhost! After startup of Frost you
can change the 'availableNodes' setting in the options dialog.

If (and ONLY if) you inadvertently choosed the wrong freenet version during the
update, you can set the correct version by changing the frost.ini file. Find the
line "freenetVersion=" and set it to "freenetVersion=05" or "freenetVersion=07".


Note for u*ix users:
---------------------
After extraction of the ZIP file the *.sh files may not be executable on your system.
To set the executable bit, run the command "chmod +x *.sh" in the Frost directory.
