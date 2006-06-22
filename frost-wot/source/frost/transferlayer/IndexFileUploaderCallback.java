package frost.transferlayer;

import java.sql.*;

public interface IndexFileUploaderCallback {

    public int findFirstFreeUploadSlot(java.sql.Date date) throws SQLException;

    public int findNextFreeSlot(int beforeIndex, java.sql.Date date) throws SQLException;

    public void setSlotUsed(int i, java.sql.Date date) throws SQLException;
}
