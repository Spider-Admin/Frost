package frost.storage;

/**
 * Uses the functionality of MessageDatabaseTable, but different table names.
 */
public class SentMessageDatabaseTable extends MessageDatabaseTable {

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
