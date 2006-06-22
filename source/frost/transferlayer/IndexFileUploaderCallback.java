package frost.transferlayer;

import java.sql.*;

public interface IndexFileUploaderCallback {

    public int findFirstFreeUploadSlot() throws SQLException;

    public int findNextFreeSlot(int beforeIndex) throws SQLException;

    public void setSlotUsed(int i) throws SQLException;
}
