/*
 * Created on Nov 9, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.gui.translation;

import java.util.EventListener;


/**
 * @author $Author$
 * @version $Revision$
 */
public interface LanguageListener extends EventListener {

	/**
	 * @param event
	 */
	public void languageChanged(LanguageEvent event);

}
