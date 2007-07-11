/*
  FrostStopwordProvider.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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
package frost.util.baysian;

import java.io.*;
import java.util.*;

import net.sf.classifier4J.*;

public class FrostStopWordProvider implements IStopWordProvider {

    private String[] words;

    /**
     * 
     * @param filename Identifies the name of a textfile on the classpath that contains
     * a list of stop words, one on each line
     */
    public FrostStopWordProvider(String filename) throws IOException {
        init(filename);
    }

    protected void init(String filename) throws IOException {
        ArrayList<String> wordsLst = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        
        String word;
        while ((word = reader.readLine()) != null) {
            wordsLst.add(word.trim());
        }
        
        words = (String[]) wordsLst.toArray(new String[wordsLst.size()]);
        
        Arrays.sort(words);
    }

    /**
     * @see net.sf.classifier4J.IStopWordProvider#isStopWord(java.lang.String)
     */
    public boolean isStopWord(String word) {
        return (Arrays.binarySearch(words, word) >= 0);
    }
}
