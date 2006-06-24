package frost.storage;

import frost.gui.objects.*;

public interface MessageDatabaseTableCallback {
    public boolean messageRetrieved(FrostMessageObject mo);
}