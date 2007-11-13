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

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.text.*;

import frost.fcp.*;

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

    private void processFreenetKeys(final String message, final TreeSet<MessageElement> targetElements) {
        final String[] FREENETKEYS = FreenetKeys.getFreenetKeyTypes();

		try { // don't die here for any reason
            for (int i = 0; i < FREENETKEYS.length; i++) {
            	int offset = 0;
            	String testMessage = new String(message);
            	while(true) {
            		final int pos = testMessage.indexOf(FREENETKEYS[i]);
            		if(pos > -1) {
                        int length = testMessage.indexOf("\n", pos);
                        if( length < 0 ) {
                            // at end of string
                            length = testMessage.length() - pos;
                        } else {
                            length -= pos;
                        }

                        final String aFileLink = testMessage.substring(pos, pos+length);
                        if( FreenetKeys.isValidKey(aFileLink) ) {
                            // we add all file links (last char of link must not be a '/' or similar) to list of links;
                            // file links and freesite links will be hyperlinked
                            targetElements.add(new MessageElement(new Integer(pos + offset),FREENETKEY, i, length));

                            if( Character.isLetterOrDigit(testMessage.charAt(pos+length-1)) ) {
                                // file link must contain at least one '/'
                                if( aFileLink.indexOf("/") > 0 ) {
                                    hyperlinkedKeys.add(aFileLink);
                                }
                            }
                        }
            			offset += pos + length;
            			testMessage = testMessage.substring(pos + length); // TODO: no substring, remember pos?!
            		} else {
            			break;
            		}
            	}
            }
        } catch (final Throwable e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Excption in processFreenetKeys", e);
        }
	}

	private void processSmileys(final String message, final TreeSet<MessageElement> targetElements) {
		// Find all smileys in message
		for (int i = 0; i < SMILEYS.length; i++) {
			for (int j = 0; j < SMILEYS[i].length; j++) {
				int offset = 0;
				String testMessage = new String(message);
				while(true) {
					final int pos = testMessage.indexOf(SMILEYS[i][j]);
					if(pos > -1) {
                        if (isSmiley(pos, testMessage, SMILEYS[i][j])) {
                            targetElements.add(new MessageElement(new Integer(pos + offset),SMILEY, i, SMILEYS[i][j].length()));
                        }
                        offset += pos + SMILEYS[i][j].length();
                        testMessage = testMessage.substring(pos + SMILEYS[i][j].length());
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
        return getCachedSmiley(i, getClass().getClassLoader());
	}

    protected static Hashtable<String,ImageIcon> smileyCache = new Hashtable<String,ImageIcon>();

    protected static synchronized ImageIcon getCachedSmiley(final int i, final ClassLoader cl) {
        final String si = Integer.toString(i);
        ImageIcon ii = smileyCache.get(si);
        if( ii == null ) {
            ii = new ImageIcon(cl.getResource("data/smileys/"+i+".gif"));
            smileyCache.put(si, ii);
        }
        return ii;
    }

    public List<String> getHyperlinkedKeys() {
        return hyperlinkedKeys;
    }
}
