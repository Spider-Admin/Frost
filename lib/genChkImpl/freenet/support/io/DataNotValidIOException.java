package freenet.support.io;
import java.io.IOException;

public class DataNotValidIOException extends IOException {

    private int code;

    public DataNotValidIOException(int code) {
        super("Data not valid, code 0x" + Integer.toHexString(code));
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
