package frost.fcp;

public class BoardKeyPair {

    private String publicBoardKey;
    private String privateBoardKey;
    
    public BoardKeyPair(String pubKey, String privKey) {
        publicBoardKey = pubKey;
        privateBoardKey = privKey;
    }

    public String getPrivateBoardKey() {
        return privateBoardKey;
    }

    public String getPublicBoardKey() {
        return publicBoardKey;
    }
}
