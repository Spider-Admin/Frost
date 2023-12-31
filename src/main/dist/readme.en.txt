* Frost - text over freenet *
_---------------------------_

NOTE: You should always have a backup of your own identities. Export your own
      identities and store the file in a safe location.


Update from a Frost 23-Dec-2006 or higher to latest version:
-------------------------------------------------------------

Stop Frost if it is running, and copy the contents of the downloaded ZIP file
over your existing Frost installation, replace all existing files.

NOTE: All your settings, messages, etc... will be preserved, but making a backup
      of your Frost directory prior to updating is strongly recommended just in
      case something goes wrong.

NOTE: If you changed the file store/applayerdb.conf for any reason (e.g. you
      moved your database to another place), do NOT overwrite this file!


Update from Frost older than 23-Dec-2006 to latest version:
------------------------------------------------------------

You can't update from versions older than 20-Jun-2006 to the latest version.
Upgrade to version 23-Dec-2006 (or later, until 21-Apr-2007) first, then to a
later version.


You have legacy Frost 0.5 running and want to start to use Frost 0.7:
-------------------------------------------------------------------------------
Copy the contents of the downloaded ZIP file into a NEW directory and start
Frost. In the first startup dialog, choose the Freenet version 0.7 for use
with this Frost installation. Create an identity (you can delete it later).
Export your own identities from the existing Frost installation, and import
them into the new Frost installation if you want to use your existing
identities.


Troubleshooting:
-----------------
Frost assumes that your Freenet node runs on the same machine with the default
FCP port settings.
For Freenet 0.7 it's "127.0.0.1:9481". If your Freenet node runs on another machine,
or if you configured another FCP client port the connection to the node will
fail and Frost can't start during the first startup. In this case you need to
edit the 'frost.ini' file that can be found in the 'config' directory.
The 'frost.ini' file is automatically created during first startup of Frost.
Open the 'frost.ini' with a text editor and find the line
containing 'availableNodes=127.0.0.1:9481'. Change the setting to fit your
needs (e.g. 'availableNodes=otherhost:12345'), and then start Frost.
It should now be able to connect to your Freenet node.
Be aware that you maybe have to configure the Freenet node to allow
connections from different hosts than localhost! After startup of Frost you
can change the 'availableNodes' setting in the options dialog.


Note for u*ix users:
---------------------
After extraction of the ZIP file the *.sh files may not be executable on your system.
To set the executable bit, run the command "chmod +x *.sh" in the Frost directory.

Note for beryl users (or if a grayed window appears):
---------------------
If you are using beryl, you have to add one line in your frost.sh usually
located in your frost directory ~/Freenet/frost

  export AWT_TOOLKIT="MToolkit"

It should look like this:
  [...]
  cd $PROGDIR

  export AWT_TOOLKIT="MToolkit"
  java -jar frost.jar "$@"
  [...]