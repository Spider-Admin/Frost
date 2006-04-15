* Frost - text over freenet *
_---------------------------_


Frost can run on Freenet 0.5 and Freenet 0.7. During the first startup of
Frost you decide which Freenet version is used. Each installed Frost
instance can only run with one Freenet version, you can't run one Frost
instance on both Freenet versions, because the freenet key format is different.

Frost assumes a first startup if the identities.xml file is not found. The
first startup dialog allows you to choose the Freenet version, and optionally
you can import an existing identities.xml file from another installed Frost
instance (0.5 or 0.7).


Updating Frost 0.5 or 0.7 to a new version:
--------------------------------------------
Stop Frost if it is running, and copy the contents of the downloaded ZIP file
over your existing Frost installation, replace all existing files. All your
settings are preserved. As always, a backup is recommended before updating :)


You have Frost 0.5 running and want to start to use Frost 0.7 (or vice versa):
-------------------------------------------------------------------------------
Copy the contents of the downloaded ZIP file into a NEW directory and start
Frost. In the first startup dialog, choose the Freenet version you want to use
with this Frost installation, and optionally choose to import an existing
identities.xml file (the Frost instance from which the identities.xml file is
imported should be stopped before importing the file). Or you decide to create
a new identity.


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

