/*
  FrostWordsDataSource.java / Frost
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
  
  Derived from Classifier4J's SimpleWordsDataSource.java
*/
package frost.util.baysian;

import java.io.*;

import net.sf.classifier4J.bayesian.*;

import org.garret.perst.*;

import frost.storage.perst.*;

/**
 * A datastore for Classifier4J
 */
public class FrostWordsDataSource  implements IWordsDataSource, Serializable {

    private final BayesianWordsStorage storage;
    
    public FrostWordsDataSource(BayesianWordsStorage storage) {
        this.storage = storage;
    }

    /**
     * @see net.sf.classifier4J.bayesian.IWordsDataSource#getWordProbability(java.lang.String)
     */
    public WordProbability getWordProbability(String word) {
        if (storage.getBayesianWordsIndex().contains(new Key(word))) {
            return (WordProbability) storage.getBayesianWordsIndex().get(word);
        } else {
            return null;
        }
    }

    /**
     * @see net.sf.classifier4J.bayesian.IWordsDataSource#addMatch(java.lang.String)
     */
    public void addMatch(String word, boolean removeNonMatch) {
        WordProbability wp = (WordProbability) storage.getBayesianWordsIndex().get(word);
        if (wp == null) {
            wp = new WordProbability(word, 1, 0);
            wp.makePersistent(storage.getStorage());
            storage.getBayesianWordsIndex().put(wp.getWord(), wp);
        } else {
            wp.setMatchingCount(wp.getMatchingCount() + 1);
            if( removeNonMatch && wp.getNonMatchingCount() > 0 ) {
                wp.setNonMatchingCount(wp.getNonMatchingCount() - 1);
            }
            wp.modify();
        }
    }

    /**
     * @see net.sf.classifier4J.bayesian.IWordsDataSource#addNonMatch(java.lang.String)
     */
    public void addNonMatch(String word, boolean removeMatch) {
        WordProbability wp = (WordProbability) storage.getBayesianWordsIndex().get(word);
        if (wp == null) {
            wp = new WordProbability(word, 0, 1);
            wp.makePersistent(storage.getStorage());
            storage.getBayesianWordsIndex().put(wp.getWord(), wp);
        } else {
            wp.setNonMatchingCount(wp.getNonMatchingCount() + 1);
            if( removeMatch && wp.getMatchingCount() > 0 ) {
                wp.setMatchingCount(wp.getMatchingCount() - 1);
            }
            wp.modify();
        }
    }

    /**
     * 
     */
    public void normalize() {
        // FIXME: reset values in such a way that the probability does not change
    }
}
