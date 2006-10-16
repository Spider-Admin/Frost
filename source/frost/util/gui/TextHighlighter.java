package frost.util.gui;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.text.*;

// Derived from "The Java Developers Almanac 1.4"

/**
 * This is a Highlighter for JtextComponents. It can be used to highlight
 * text in the component with a colored background.  
 */
public class TextHighlighter {
    
    private Color color;
    private boolean matchAnyCase;
    // An instance of the private subclass of the default highlight painter
    Highlighter.HighlightPainter myHighlightPainter;

    /**
     * @param col  Background color for highlighted text
     */
    public TextHighlighter(Color col) {
       color = col;
       myHighlightPainter = new MyHighlightPainter(color);
       this.matchAnyCase = true;
    }

    /**
     * @param col  Background color for highlighted text
     * @param matchAnyCase  Should HUGO be highlighted if the highlight word is hugo? true means yes
     */
    public TextHighlighter(Color col, boolean matchAnyCase) {
       color = col;
       myHighlightPainter = new MyHighlightPainter(color);
       this.matchAnyCase = matchAnyCase;
    }
    
    public void highlight(JTextComponent textComp, List patterns, boolean removeOldHighlights) {

        for(Iterator i=patterns.iterator(); i.hasNext(); ) {
            String p = (String)i.next();
            highlight(textComp, p, removeOldHighlights);
            if( removeOldHighlights ) {
                removeOldHighlights = false; // call with remove only first time
            }
        }
    }    
    
//  Creates highlights around all occurrences of pattern in textComp
    public void highlight(JTextComponent textComp, String pattern, boolean removeOldHighlights) {
        if( removeOldHighlights ) {
            // First remove all old highlights
            removeHighlights(textComp);
        }
    
        try {
            Highlighter hilite = textComp.getHighlighter();
            Document doc = textComp.getDocument();
            String text = doc.getText(0, doc.getLength());
            if( matchAnyCase ) {
                text = text.toLowerCase();
            }
            int pos = 0;
    
            // Search for pattern
            while ((pos = text.indexOf(pattern, pos)) >= 0) {
                // Create highlighter using private painter and apply around pattern
                hilite.addHighlight(pos, pos+pattern.length(), myHighlightPainter);
                pos += pattern.length();
            }
        } catch (BadLocationException e) {
        }
    }

    public void highlight(JTextComponent textComp, int pos, int len, boolean removeOldHighlights) {
        if( removeOldHighlights ) {
            // First remove all old highlights
            removeHighlights(textComp);
        }
    
        try {
            Highlighter hilite = textComp.getHighlighter();
            hilite.addHighlight(pos, pos+len, myHighlightPainter);
        } catch (BadLocationException e) {
        }
    }

    // Removes only our private highlights
    public void removeHighlights(JTextComponent textComp) {
        Highlighter hilite = textComp.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();
    
        for (int i=0; i<hilites.length; i++) {
            if (hilites[i].getPainter() instanceof MyHighlightPainter) {
                hilite.removeHighlight(hilites[i]);
            }
        }
    }
    
    // A private subclass of the default highlight painter
    private class MyHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
        public MyHighlightPainter(Color color) {
            super(color);
        }
    }
}
