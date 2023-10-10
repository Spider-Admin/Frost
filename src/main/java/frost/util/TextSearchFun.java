/*
  TextSearchFun.java / Frost
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
package frost.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Provides common functions for text search.
 */
public class TextSearchFun {

    private final static SearchStringParser searchStringParser = new SearchStringParser();

    private static final String NOT_IDENT = ">?*NOT*?<";
    
    private static List<String> emptyList = new LinkedList<String>();

    /**
     * Searches text for occurence of any of the provided strings.
     * @param text  text to search into
     * @param notStrings  list of strings
     * @return  true if any string occurs in text, false if no string occurs in text
     */
    public static boolean containsAnyString(String text, List<String> notStrings) {
        if( notStrings != null && !notStrings.isEmpty() && text != null && text.length() > 0 ) {
            for(int x=0; x < notStrings.size(); x++) {
                String notName = notStrings.get(x);
                if( text.indexOf(notName) > -1 ) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Searches text for all occurences of provided strings.
     * @param text  text to search into
     * @param strings  List of strings to search
     * @return true if ALL strings occur in the text, false otherwise
     */
    public static boolean containsEachString(String text, List<String> strings) {
        for(int x=0; x < strings.size(); x++) {
            String string = strings.get(x);
            if( text.indexOf(string) < 0 ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Splits an input search string into search words and NOT search words.
     * @return  List[2] where List[0] is a list of search string and List[1] is a List of NOT search strings
     */
    public static List<String>[] splitStrings(String input, boolean makeLowerCase) {
        
        List<String> strList;

        // we share one instance of the parser
        synchronized(searchStringParser) {
            strList = searchStringParser.parseSearchText(input);
        }

        List<String>[] retVal = new List[2];
        List<String> searchStrings = new ArrayList<String>();
        List<String> notSearchStrings = new ArrayList<String>();
        retVal[0] = searchStrings;
        retVal[1] = notSearchStrings;
        
        // all strings until SearchStringParser.NOT_IDENT are ANDed, all after NOT are the notStrings
        boolean collectNotStrings = false;
        for(Iterator<String> i=strList.iterator(); i.hasNext(); ) {
            String s = i.next();
            if( s.equals(NOT_IDENT) ) {
                collectNotStrings = true;
            } else {
                if( makeLowerCase ) {
                    s = s.toLowerCase();
                }
                if( !collectNotStrings ) {
                    searchStrings.add(s);
                } else {
                    notSearchStrings.add(s);
                }
            }
        }
        return retVal;
    }
    
    /**
     * Splits a String into parts. 
     * First NOT is converted to ">?*NOT*?<", more NOTs are dropped. "NOT" keeps NOT.
     * Input: "mars venus \"milky way\" NOT \"NOT\" NOT sun" 
     * Output: [mars, venus, milky way, >?*NOT*?<, NOT, sun]
     */
    private static class SearchStringParser {

        /**
         * Parse the user's search box input into a Set of String tokens.
         * 
         * @return Set of Strings, one for each word in fSearchText; here "word" is defined as either a lone word
         *         surrounded by whitespace, or as a series of words surrounded by double quotes, "like this".
         */
        public List<String> parseSearchText(String aSearchText) {
            
            if( aSearchText == null ) {
                return emptyList;
            }
            fSearchText = aSearchText;
            notAdded = false;

            List<String> result = new LinkedList<String>();

            boolean returnTokens = true;
            String currentDelims = fWHITESPACE_AND_QUOTES;
            StringTokenizer parser = new StringTokenizer(fSearchText, currentDelims, returnTokens);

            String token = null;
            boolean inQuotes = false;
            while( parser.hasMoreTokens() ) {
                token = parser.nextToken(currentDelims);
                if( !isDoubleQuote(token) ) {
                    addNonTrivialWordToResult(token, result, inQuotes);
                } else {
                    currentDelims = flipDelimiters(currentDelims);
                    inQuotes = !inQuotes;
                }
            }
            return result;
        }

        // PRIVATE //
        private String fSearchText;
        private static final String fDOUBLE_QUOTE = "\"";

        // the parser flips between these two sets of delimiters
        private static final String fWHITESPACE_AND_QUOTES = " \t\r\n\"";
        private static final String fQUOTES_ONLY = "\"";
        
        private boolean notAdded;

        private boolean textHasContent(String aText) {
            return (aText != null) && (aText.trim().length() > 0);
        }

        private void addNonTrivialWordToResult(String aToken, List<String> aResult, boolean inQuotes) {
            if( textHasContent(aToken) ) {
                aToken = aToken.trim();
                if( !inQuotes && aToken.equals("NOT") ) {
                    if( !notAdded ) {
                        // add NOT one time
                        aResult.add(NOT_IDENT);
                        notAdded = true;
                    }
                } else {
                    aResult.add(aToken.trim());
                }
            }
        }

        private boolean isDoubleQuote(String aToken) {
            return aToken.equals(fDOUBLE_QUOTE);
        }

        private String flipDelimiters(String aCurrentDelims) {
            String result = null;
            if( aCurrentDelims.equals(fWHITESPACE_AND_QUOTES) ) {
                result = fQUOTES_ONLY;
            } else {
                result = fWHITESPACE_AND_QUOTES;
            }
            return result;
        }
    }
}
