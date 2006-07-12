package frost.storage.database.applayer;

import frost.messages.*;

public interface FileListDatabaseTableCallback {
    public boolean fileRetrieved(FrostSharedFileObject fo);
}
