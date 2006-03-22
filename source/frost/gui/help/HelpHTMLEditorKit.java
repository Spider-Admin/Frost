/*
 HelpHTMLEditorKit.java / Frost
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
package frost.gui.help;

import java.util.logging.*;

import javax.swing.text.*;
import javax.swing.text.html.*;

/**
 * @author notitaccu
 *
 * This class implements a comparator that is the reverse of the one passed as the parameter of the constructor.
 */
public class HelpHTMLEditorKit extends HTMLEditorKit {

    private static Logger logger = Logger.getLogger(HelpHTMLEditorKit.class.getName());

    /**
     * The factory used to create views for document elements.
     */
//    private helpHTMLFactory viewFactory = new helpHTMLFactory();

    /**
     * Constructs an HTMLEditorKit, creates a StyleContext, and loads the style sheet.
     */
    public HelpHTMLEditorKit() {
        super();
    }

    /**
     * Shared factory for creating HTML Views.
     */
    private static final ViewFactory helpFactory = new helpHTMLFactory();

    public ViewFactory getViewFactory() {
        return helpFactory;
    }

    /**
     * Factory which creates views for elements in the JGloss document.
     */
    public static class helpHTMLFactory extends HTMLEditorKit.HTMLFactory {

        /**
         * Creates a new factory.
         *
         */
        public helpHTMLFactory() {
            // System.out.println("Boom!01Create:");
        }

        /**
         * Creates a view which can render the element.
         *
         * @param elem
         *            The element to create the view for.
         * @return The newly created view.
         */
        public View create(Element elem) {
            // hier koennen unerwuenschte html-tags abgefangen werden
            return super.create(elem);
        }
    }
}
