package freenet;


/**
 * Implementations of Presentation are used to handle connections
 * with a certain message protocol. It is used used to initialize
 * new connections, read messages off the connection into the RawMessage
 * format, and creating new RawMessages for writing to a stream with
 * this protocol.
 **/

public abstract class Presentation {

    /**
     * These are control byte values used in the streams of all presentations
     **/
    public static final int
        CB_OK        = 0x00,
        CB_RESTARTED = 0x01,  // parallels QueryRestarted
        CB_ABORTED   = 0x02;  // parallels QueryAborted

    //Values over 128 are internal
    public static final int
        CB_BAD_DATA       = 0x81,
        CB_SEND_CONN_DIED = 0x82,
        CB_RECV_CONN_DIED = 0x83,
        CB_BAD_KEY        = 0x84,
        CB_CACHE_FAILED   = 0x85,
        CB_CANCELLED      = 0x86;


    public static final String getCBname(int cb) {
        switch (cb) {
            case CB_OK:                 return "CB_OK";
            case CB_RESTARTED:          return "CB_RESTARTED";
            case CB_ABORTED:            return "CB_ABORTED";
            case CB_BAD_DATA:           return "CB_BAD_DATA";
            case CB_SEND_CONN_DIED:     return "CB_SEND_CONN_DIED";
            case CB_RECV_CONN_DIED:     return "CB_RECV_CONN_DIED";
            case CB_BAD_KEY:            return "CB_BAD_KEY";
            case CB_CACHE_FAILED:       return "CB_CACHE_FAILED";
            case CB_CANCELLED:          return "CB_CANCELLED";
            default:                    return "Unknown control byte";
        }
    }

    public static final String getCBdescription(int cb) {
        return "0x"+Integer.toHexString(cb)+" ("+getCBname(cb)+")";
    }



}


