/*
 SearchStringParser.java / Frost
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
package frost.fileTransfer.search;

import java.util.*;

/**
 * Splits a String into parts. 
 * First NOT is converted to ">?*NOT*?<", more NOTs are dropped. "NOT" keeps NOT.
 * Input: "mars venus \"milky way\" NOT \"NOT\" NOT sun" 
 * Output: [mars, venus, milky way, >?*NOT*?<, NOT, sun]
 */
public class SearchStringParser {
    
    public static final String NOT_IDENT = ">?*NOT*?<";

//    public static void main(String[] args) {
//        SearchStringParser parser = new SearchStringParser();
//        String searchText = "mars venus \"milky way\" NOT \"NOT\" NOT sun";
//        List tokens = parser.parseSearchText(searchText);
//        //display the tokens
//        System.out.println(searchText);
//        System.out.println(tokens);
//      }
    
    /**
     * Parse the user's search box input into a Set of String tokens.
     * 
     * @return Set of Strings, one for each word in fSearchText; here "word" is defined as either a lone word
     *         surrounded by whitespace, or as a series of words surrounded by double quotes, "like this".
     */
    public List parseSearchText(String aSearchText) {
        
        if( aSearchText == null ) {
            return new LinkedList();
        }
        fSearchText = aSearchText;
        notAdded = false;

        List result = new LinkedList();

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

    private void addNonTrivialWordToResult(String aToken, List aResult, boolean inQuotes) {
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
