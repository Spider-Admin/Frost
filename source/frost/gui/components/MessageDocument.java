/*
 * Created on Jan 16, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.gui.components;

import java.util.*;
import java.util.ArrayList;

import javax.swing.text.*;


/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MessageDocument extends PlainDocument {

	private ArrayList immutableAreas = new ArrayList();
	private MessageFilter filter = new MessageFilter();

	/**
	 * 
	 */
	private class MessageFilter extends DocumentFilter {
		
		public static final int CLIPPING_OUTSIDE = 0;
		public static final int CLIPPING_INSIDE = 1;
		public static final int CLIPPING_BOTH_SIDES = 2;
		public static final int CLIPPING_LEFT = 3;
		public static final int CLIPPING_RIGHT = 4;
		
		/**
		 * 
		 */
		public MessageFilter() {
			super();
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.text.DocumentFilter#replace(javax.swing.text.DocumentFilter.FilterBypass, int, int, java.lang.String, javax.swing.text.AttributeSet)
		 */
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
			throws BadLocationException {

			boolean allowReplace = true;
			boolean noAreasEnabled = true;
			Iterator areas = immutableAreas.iterator();
			while (areas.hasNext() && allowReplace) {
				ImmutableArea area = (ImmutableArea) areas.next();
				if (area.isEnabled()) {
					int clippingValue = clip(area, offset, length, false);
					int endOffset = offset + length;
					Position position = createPosition(offset);
					Position endPosition = createPosition(endOffset);

					if (clippingValue != CLIPPING_OUTSIDE) {
						noAreasEnabled = false;
					}
					if ((clippingValue == CLIPPING_LEFT) || (clippingValue == CLIPPING_BOTH_SIDES)) {
						int newStringLength = text.length() - (endOffset - area.getStartPos());
						int newLength = area.getStartPos() - offset;
						if (newStringLength >= newLength) {
							fb.replace(offset, newLength, substring(text, 0, newStringLength), attrs);
						} else {
							fb.replace(offset, newLength, substring(text, 0, newLength), attrs);
						}
						
					}
					if ((clippingValue == CLIPPING_RIGHT) || (clippingValue == CLIPPING_BOTH_SIDES)){
						int newStart = area.getEndPos() - position.getOffset();
						int newLength = endPosition.getOffset() - area.getEndPos();
						if (newStart >= text.length()) {
							fb.replace(area.getEndPos(), newLength, "", attrs);
						} else {
							fb.replace(area.getEndPos(), newLength, text.substring(newStart), attrs);
						}
					}
					allowReplace = false;
				}
			}
			if (noAreasEnabled) {
				super.replace(fb, offset, length, text, attrs);
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.text.DocumentFilter#insertString(javax.swing.text.DocumentFilter.FilterBypass, int, java.lang.String, javax.swing.text.AttributeSet)
		 */
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
			throws BadLocationException {

			boolean noAreasEnabled = true;
			boolean allowInsert = true;
			Iterator areas = immutableAreas.iterator();
			while (areas.hasNext() && allowInsert) {
				ImmutableArea area = (ImmutableArea) areas.next();
				if (area.isEnabled()) {
					int clippingValue = clip(area, offset, string.length(), false);
					if (clippingValue == CLIPPING_INSIDE) {
						noAreasEnabled = false;
						allowInsert = false;
					}
				}
			}
			if (noAreasEnabled) {
				super.insertString(fb, offset, string, attr);
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.text.DocumentFilter#remove(javax.swing.text.DocumentFilter.FilterBypass, int, int)
		 */
		public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
			
			boolean allowRemove = true;
			boolean noAreasEnabled = true;
			Iterator areas = immutableAreas.iterator();
			while (areas.hasNext() && allowRemove) {
				ImmutableArea area = (ImmutableArea) areas.next();
				if (area.isEnabled()) {
					int clippingValue = clip(area, offset, length, true);
					int endOffset = offset + length;
					Position position = createPosition(offset);
					Position endPosition = createPosition(endOffset);

					if (clippingValue != CLIPPING_OUTSIDE) {
						noAreasEnabled = false;
					}
					if ((clippingValue == CLIPPING_LEFT) || (clippingValue == CLIPPING_BOTH_SIDES))  {
						int newLength = area.getStartPos() - offset;
						fb.remove(offset, newLength);
					}
					if ((clippingValue == CLIPPING_RIGHT) || (clippingValue == CLIPPING_BOTH_SIDES)) {
						int newLength = endPosition.getOffset() - area.getEndPos();
						fb.remove(area.getEndPos(), newLength);
					}
					allowRemove = false;
				}
			}
			if (noAreasEnabled) {
				super.remove(fb, offset, length);
			}
		}

		/**
		 * @param area
		 * @param offset
		 * @return
		 */
		private int clip(ImmutableArea area, int offset, int length, boolean isRemoving) {
			int endOffset = offset + length;
			boolean offsetOutsideLeft = false;
			boolean offsetOutsideRight = false;
			boolean endOffsetOutsideLeft = false;
			boolean endOffsetOutsideRight = false;
			
			if (isRemoving) {
				if (offset < area.getStartPos()) {
					offsetOutsideLeft = true;
				} else if (offset >= area.getEndPos()) {
					offsetOutsideRight = true;
				}
				if (endOffset < area.getStartPos()) {
					endOffsetOutsideLeft = true;
				} else if (endOffset >= area.getEndPos()) {
					endOffsetOutsideRight = true;
				}
			} else {
				if (offset <= area.getStartPos()) {
					offsetOutsideLeft = true;
				} else if (offset >= area.getEndPos()) {
					offsetOutsideRight = true;
				}
				if (endOffset <= area.getStartPos()) {
					endOffsetOutsideLeft = true;
				} else if (endOffset >= area.getEndPos()) {
					endOffsetOutsideRight = true;
				}
			}
			
			if (offsetOutsideLeft) {
				if (endOffsetOutsideLeft) {
					return CLIPPING_OUTSIDE;	
				}	
				if (endOffsetOutsideRight) {
					return CLIPPING_BOTH_SIDES;	
				}
				return CLIPPING_LEFT;				
			}
			if (offsetOutsideRight) {
				return CLIPPING_OUTSIDE;	
			}
			if (endOffsetOutsideRight) {
				return CLIPPING_RIGHT;	
			}
			return CLIPPING_INSIDE;
		}
		
		private String substring(String text, int beginIndex, int endIndex) {
			if (beginIndex < 0) {
				beginIndex = 0;	
			}
			if (endIndex > text.length()) {
				endIndex = text.length();
			}
			return text.substring(beginIndex, endIndex);
		}
	}

	/**
	 * 
	 */
	public MessageDocument() {
		super();
		setDocumentFilter(filter);
	}
	
	/**
	 * 
	 */
	public void addImmutableArea(ImmutableArea newArea) {
		immutableAreas.add(newArea);
	}
	
	/**
	 * 
	 */
	public void removeImmutableArea(ImmutableArea area) {
		immutableAreas.remove(area);
	}

	/**
	 * @param c
	 */
	public MessageDocument(Content c) {
		super(c);
		setDocumentFilter(filter);
	}

}