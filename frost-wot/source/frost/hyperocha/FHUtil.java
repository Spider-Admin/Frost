/**
 * 
 */
package frost.hyperocha;

import hyperocha.freenet.fcp.utils.FCPUtil;

/**
 * @author saces
 *
 */
public class FHUtil {
	
	public static String getNextJobID() {
		return FCPUtil.getNewConnectionId("Frost-");
	}
	
	
	 //replaces all / with | in url
    protected static String StripSlashes(String uri){
        //replacing all / with |
    	if (uri.startsWith("KSK@")) {
    		String myUri = null;
    		myUri= uri.replace('/','|');
    		return myUri;
    	} else if (uri.startsWith("SSK@")) {
    		String sskpart= uri.substring(0, uri.indexOf('/') + 1);
    		String datapart = uri.substring(uri.indexOf('/')+1).replace('/','|');
    		return sskpart + datapart;
    	} else {
    		return uri;
        }
    }




}
