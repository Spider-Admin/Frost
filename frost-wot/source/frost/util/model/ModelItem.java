/*
 * Created on Apr 19, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model;


/**
 * 
 */
public class ModelItem {

	private Model model;

	/**
	 * 
	 */
	public ModelItem() {
		super();
	}

	/**
	 * Report a field update to the model (if it has already been set).
	 * No event is fired if old and new are equal and non-null.
	 *
	 * @param fieldID  The ID of the field that was changed.
	 * @param oldValue The old value of the field.
	 * @param newValue The new value of the field.
	 */
	protected void fireFieldChange(int fieldID, Object oldValue, Object newValue) {
		
		if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
			return;
		}
		if (model != null) {
			model.itemChanged(this, fieldID, oldValue, newValue);
		}
	}
	
	/**
	 * Report a field update to the model (if it has already been set).
	 * No event is fired if old and new are equal and non-null.
	 * <p>
     * This is merely a convenience wrapper around the more general
     * fireFieldChange method that takes Object values.
	 *
	 * @param fieldID  The ID of the field that was changed.
	 * @param oldValue The old value of the field.
	 * @param newValue The new value of the field.
	 */
	protected void fireFieldChange(int fieldID, int oldValue, int newValue) {
		if (oldValue == newValue) {
			return;
		}
		if (model != null) {
			model.itemChanged(this, fieldID, new Integer(oldValue), new Integer(newValue));
		}
	}
	
	/**
	 * Report a field update to the model (if it has already been set).
	 * No event is fired if old and new are equal and non-null.
	 * <p>
	 * This is merely a convenience wrapper around the more general
	 * fireFieldChange method that takes Object values.
	 *
	 * @param fieldID  The ID of the field that was changed.
	 * @param oldValue The old value of the field.
	 * @param newValue The new value of the field.
	 */
	protected void fireFieldChange(int fieldID, boolean oldValue, boolean newValue) {
		if (oldValue == newValue) {
			return;
		}
		if (model != null) {
			model.itemChanged(this, fieldID, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
		}
	}
	
	/**
	 * Report an update to the model (if it has already been set).
	 */
	protected void fireChange() {
		if (model != null) {
			model.itemChanged(this);
		}
	}

	/**
	 * @return
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * @param model
	 */
	public void setModel(Model newModel) {
		model = newModel;
	}
	
}
