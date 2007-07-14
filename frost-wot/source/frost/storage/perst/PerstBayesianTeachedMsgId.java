package frost.storage.perst;

import org.garret.perst.*;

public class PerstBayesianTeachedMsgId extends Persistent {

    public String msgId;
    public long firstTeachDate;

    public PerstBayesianTeachedMsgId() {}

    public PerstBayesianTeachedMsgId(String mi) {
        msgId = mi;
        firstTeachDate = 0;
    }
    public PerstBayesianTeachedMsgId(String mi, long ftd) {
        msgId = mi;
        firstTeachDate = ftd;
    }

    public String toString() {
        return msgId;
    }

    @Override
    public int hashCode() {
        return msgId.hashCode();
    }
}
