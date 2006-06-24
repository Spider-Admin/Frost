package frost.storage;

/**
 * Uses the functionality of MessageDatabaseTable, but different table names.
 */
public class SentMessageDatabaseTable extends MessageDatabaseTable {

    private static SentMessageDatabaseTable instance2 = null;
    
    public static MessageDatabaseTable getInstance() {
        if( instance2 == null ) {
            instance2 = new SentMessageDatabaseTable();
        }
        return instance2;
    }

    protected String getMessageTableName() {
        return "SENTMESSAGES";
    }
    protected String getFileAttachmentsTableName() {
        return "SENTFILEATTACHMENTS";
    }
    protected String getBoardAttachmentsTableName() {
        return "SENTBOARDATTACHMENTS";
    }
}
