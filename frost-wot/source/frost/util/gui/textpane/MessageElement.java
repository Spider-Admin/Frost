/*
 MessageElement.java / Frost
 Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.util.gui.textpane;

/**
 * Message element use by {@link MessageDecoder}.
 * @author ET
 */
public class MessageElement implements Comparable<MessageElement> {

	private final int messageLength;
	private final Integer messagePosition;
	private final int messageType;
	private final int messageTypeIndex;

	protected MessageElement(final Integer position, final int type, final int typeIndex, final int length) {
		this.messageLength = length;
		this.messagePosition = position;
		this.messageType = type;
		this.messageTypeIndex = typeIndex;
	}

	protected int getLength() {
		return messageLength;
	}

	protected Integer getPosition() {
		return messagePosition;
	}

	protected int getType() {
		return messageType;
	}

	protected int getTypeIndex() {
		return messageTypeIndex;
	}

	public int compareTo(final MessageElement me) {
		return messagePosition.compareTo(me.getPosition());
	}
}
