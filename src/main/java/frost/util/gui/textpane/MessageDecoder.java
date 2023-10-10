/*
 MessageDecoder.java / Frost
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

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;

import frost.fcp.FreenetKeys;
import frost.util.gui.SmileyCache;

/**
 * Message decoder for search freenet keys and smileys,
 * append message to JeditorPane document.
 * @author ET
 */
public class MessageDecoder extends Decoder implements Smileys, MessageTypes {

    private final Logger logger = Logger.getLogger(MessageDecoder.class.getName());

	private boolean smileys = true;
	private boolean freenetKeys = true;

    private final List<String> hyperlinkedKeys = new LinkedList<String>();
    private final TreeSet<MessageElement> elements = new TreeSet<MessageElement>();

    public MessageDecoder() {
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
    public void decode(final String message, final JEditorPane parent) {

	    if (message == null) {
	        return;
	    }

		elements.clear();
        hyperlinkedKeys.clear();

		if (smileys) {
			// Verify EditorKit for correct render set EditorKit if not good
			if (!(parent.getEditorKit() instanceof StyledEditorKit)) {
				parent.setEditorKit(new StyledEditorKit());
			}
			processSmileys(message, elements);
		}
		if (freenetKeys) {
			// Verify EditorKit for correct render set EditorKit if not good
			// LinkEditorKit extends StyledEditorKit for render smileys
			if (!(parent.getEditorKit() instanceof LinkEditorKit)) {
				parent.setEditorKit(new LinkEditorKit());
			}
			processFreenetKeys(message, elements);
		}

        final Document doc = new DefaultStyledDocument();
        int begin = 0;
		try {
            final Iterator<MessageElement> it = elements.iterator();
			while(it.hasNext()) {
				final MessageElement me = it.next();
				final String s = message.substring(me.getPosition().intValue(), me.getPosition().intValue() + me.getLength());
				final SimpleAttributeSet at = new SimpleAttributeSet();

				if (me.getPosition().intValue() > begin) { // if smileys are confused with freenetkeys not display
					// insert text before element
					doc.insertString(doc.getLength(),message.substring(begin, me.getPosition().intValue()), new SimpleAttributeSet());
					if(me.getType() == SMILEY) {
						StyleConstants.setIcon(at,getSmiley(me.getTypeIndex()));
					} else if (me.getType() == FREENETKEY) {
				        at.addAttribute(LinkEditorKit.LINK, s);
				        at.addAttribute(StyleConstants.Underline, Boolean.TRUE);
				        at.addAttribute(StyleConstants.Foreground, Color.BLUE);
					}

					// insert element
			        doc.insertString(doc.getLength(), s, at);
					begin = me.getPosition().intValue() + me.getLength();
				}
			}

			// insert text after last element
			doc.insertString(doc.getLength(),message.substring(begin), new SimpleAttributeSet());
		} catch (final BadLocationException e) {
            logger.log(Level.SEVERE, "Excpetion during construction of message", e);
		}

        // set constructed doc to view
        parent.setDocument(doc);
	}

	/**
	 * Set freenet's keys decoder acitve or not
	 * @param value
	 */
	public void setFreenetKeysDecode(final boolean value) {
		freenetKeys = value;
	}

	/**
	 * Get status of freenet's keys decoder
	 * @return true if active or false is not active
	 */
	public boolean getFreenetKeysDecode() {
		return freenetKeys;
	}

	/**
	 * Set smileys decoder acitve or not
	 * @param value
	 */
	public void setSmileyDecode(final boolean value) {
		smileys = value;
	}

	/**
	 * Get status of smileys decoder
	 * @return true if active or false is not active
	 */
	public boolean getSmileyDecode() {
		return smileys;
	}

    /**
     * Parses the message received on the first parameter, identifying those hyperlinks
     * contained in it and returning them on the second parameter
     *
     * @param message
     *            the contents of the message to be parsed
     * @param targetElements
     *            the hyperlinks found within the parsed message
     */
    private void processFreenetKeys(final String message, final TreeSet<MessageElement> targetElements) {
        final String[] FREENETKEYS = FreenetKeys.getFreenetKeyTypes();

		try { // don't die here for any reason
            for (int i = 0; i < FREENETKEYS.length; i++) {
            	int offset = 0;
            	while(true) {
            		final int pos = message.indexOf(FREENETKEYS[i], offset);
            		if(pos > -1) {
                        int length = message.indexOf("\n", pos);
                        if( length < 0 ) {
                            // at end of string
                            length = message.length() - pos;
                        } else {
                            length -= pos;
                        }

                        // Trim trailing white spaces from the link
                        while (Character.isWhitespace(message.charAt((pos + length) - 1))) {
                            --length;
                        }

                        final String aFileLink = message.substring(pos, pos+length);
                        if( FreenetKeys.isValidKey(aFileLink) ) {
                            // we add all file links (last char of link must not be a '/' or similar) to list of links;
                            // file links and freesite links will be hyperlinked
                            targetElements.add(new MessageElement(new Integer(pos),FREENETKEY, i, length));

                            if( Character.isLetterOrDigit(message.charAt((pos+length)-1)) ) {
                                // file link must contain at least one '/'
                                if( aFileLink.indexOf("/") > 0 ) {
                                    hyperlinkedKeys.add(aFileLink);
                                }
                            }
                        }
            			offset = pos + length;
            		} else {
            			break;
            		}
            	}
            }
        } catch (final Throwable e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Exception in processFreenetKeys", e);
        }
	}

    /**
     * Parses the message received on the first parameter, identifying those smileys
     * contained in it and returning them on the second parameter
     *
     * @param message
     *            the contents of the message to be parsed
     * @param targetElements
     *            the smileys found within the parsed message
     */
	private void processSmileys(final String message, final TreeSet<MessageElement> targetElements) {
		// Find all smileys in message
		for (int i = 0; i < SMILEYS.length; i++) {
			for (int j = 0; j < SMILEYS[i].length; j++) {
				int offset = 0;
				while(true) {
					final int pos = message.indexOf(SMILEYS[i][j], offset);
					if(pos > -1) {
                        if (isSmiley(pos, message, SMILEYS[i][j])) {
                            targetElements.add(new MessageElement(new Integer(pos),SMILEY, i, SMILEYS[i][j].length()));
                        }
                        offset = pos + SMILEYS[i][j].length();
					} else {
						break;
					}
				}
			}
		}
	}

    /**
     * A smiley is only recognized if there is a whitespace before and after it (or begin/end of line)
     */
    private boolean isSmiley(final int pos, final String message, final String smiley) {
        boolean bol = (pos == 0);
        boolean eol = (message.length() == (smiley.length() + pos));
        char c;
        if (!bol) {
            c = message.charAt(pos-1);
            bol = Character.isWhitespace(c);
        }
        if (!eol) {
            c = message.charAt(pos+smiley.length());
            eol = Character.isWhitespace(c);
        }
        return (bol && eol);
    }

	private Icon getSmiley(final int i) {
        return SmileyCache.getCachedSmiley(i);
	}

    public List<String> getHyperlinkedKeys() {
        return hyperlinkedKeys;
    }
}
