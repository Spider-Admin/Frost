/**
 * 
 */
package hyperocha.freenet.fcp.utils;

/**
 * @author saces
 *
 */
public class FCPUtil {
	
	private static int counter = 1;

	/**
	 * 
	 */
	public static String getNewConnectionId() {
		return getNewConnectionId("", "");
	}
	
	public static String getNewConnectionId(String prefix) {
		return getNewConnectionId(prefix, "");
	}
	
	public static String getNewConnectionId(String prefix, String suffix) {
		return (prefix + System.currentTimeMillis() + suffix);
	}
	
	public static String getNewConnectionId2() {
		return getNewConnectionId2("", "");
	}
	
	public static String getNewConnectionId2(String prefix) {
		return getNewConnectionId2(prefix, "");
	}
	
	public static String getNewConnectionId2(String prefix, String suffix) {
		return (prefix + counter++ + suffix);
	}

}
