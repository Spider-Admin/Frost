package frost.storage.database;

import java.util.*;

public abstract class AbstractDatabaseTable {
    
    protected static final Object syncObj = new Object();
    
    protected Object getSyncObj() {
        return syncObj;
    }
    
    abstract public List getTableDDL();

}
