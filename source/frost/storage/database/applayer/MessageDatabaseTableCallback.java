package frost.storage.database.applayer;

import frost.gui.objects.*;

public interface MessageDatabaseTableCallback {
    public boolean messageRetrieved(FrostMessageObject mo);
}