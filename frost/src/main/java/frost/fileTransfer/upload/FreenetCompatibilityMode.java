package frost.fileTransfer.upload;

public enum FreenetCompatibilityMode {
	
	COMPAT_1250_EXACT("COMPAT_1250_EXACT"), 
	COMPAT_1250("COMPAT_1250"), 
	COMPAT_1251("COMPAT_1251"), 
	COMPAT_1255("COMPAT_1255"),
	COMPAT_CURRENT("COMPAT_CURRENT"); 

	final String mode;
	
	FreenetCompatibilityMode(String mode){
		this.mode = mode;
	}
	
	public static FreenetCompatibilityMode getDefault() {
		return FreenetCompatibilityMode.COMPAT_CURRENT;
	}
	
	public String toString() {
		return mode;
	}
}
