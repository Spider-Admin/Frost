package frost.transferlayer;

public interface IndexFileUploaderCallback {

    public int findFirstFreeUploadSlot();

    public int findNextFreeSlot(int beforeIndex);

    public void setSlotUsed(int i);

}
