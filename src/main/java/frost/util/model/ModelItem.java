/*
 ModelItem.java / Frost
 Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as
 published by the Free Software Foundation; either version 2 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.util.model;


abstract public class ModelItem<T extends ModelItem<T>> {

	private SortedModel<T> model;

	/**
	 * Report an update to the model (if it has already been set).
	 */
	@SuppressWarnings("unchecked")
	protected void fireChange() {
		if (model != null) {
			model.itemChanged((T) this);
		}
	}

	/**
	 * @return
	 */
	public SortedModel<T> getModel() {
		return model;
	}

	/**
	 * @param model
	 */
	public void setModel(SortedModel<T> newModel) {
		model = newModel;
	}
}
