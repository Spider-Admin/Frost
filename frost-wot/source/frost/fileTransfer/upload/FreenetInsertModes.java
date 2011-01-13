package frost.fileTransfer.upload;

public enum FreenetInsertModes {
	
	COMPAT_1250_EXACT("COMPAT_1250_EXACT"), 
	COMPAT_1250("COMPAT_1250"), 
	COMPAT_1251("COMPAT_1251"), 
	COMPAT_1254("COMPAT_1254"),
	COMPAT_CURRENT("COMPAT_CURRENT"); 

	String mode;
	
	FreenetInsertModes(String mode){
		this.mode = mode;
	}
	
	public static FreenetInsertModes getDefault() {
		return FreenetInsertModes.COMPAT_CURRENT;
	}
}
