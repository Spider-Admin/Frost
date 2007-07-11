/*
  BayesianFilter.java / Frost
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
import net.sf.classifier4J.bayesian.*;
import frost.*;
import frost.storage.perst.*;

public class FrostBayesianFilter {
    
    private static FrostBayesianFilter instance = new FrostBayesianFilter();

    private BayesianClassifier classifier = null;
    private SimpleWordsDataSource wds = null;
    
    protected FrostBayesianFilter() {}
    
    public static FrostBayesianFilter inst() {
        return instance;
    }
    
    public void initialize(BayesianWordsStorage storage) {
        // training data
        wds = new SimpleWordsDataSource();
        
        storage.loadBayesianWords(wds);
        
        // ignored words
        IStopWordProvider stopWordProvider;
        try {
            String stopWordsFileName = Core.frostSettings.getValue(SettingsClass.DIR_CONFIG) + "defaultStopWords.txt"; 
            stopWordProvider = new FrostStopWordProvider(stopWordsFileName);
        } catch (IOException e) {
            e.printStackTrace();
            stopWordProvider = new DefaultStopWordsProvider();
        }

        // message word splitter
        ITokenizer tokenizer = new DefaultTokenizer(DefaultTokenizer.BREAK_ON_WORD_BREAKS);
        
        classifier = new BayesianClassifier(wds, tokenizer, stopWordProvider);
    }
    
    public Collection<WordProbability> getWords() {
        return wds.getAll();
    }
    
    public void teachIsSpam(String text) {
        try {
            classifier.teachMatch(ICategorisedClassifier.DEFAULT_CATEGORY, text);
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    public void teachIsNotSpam(String text) {
        try {
            classifier.teachNonMatch(ICategorisedClassifier.DEFAULT_CATEGORY, text);
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }
    
    public boolean checkIsSpam(String text) {
        try {
            return classifier.isMatch(ICategorisedClassifier.DEFAULT_CATEGORY, text);
        } catch(Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
}
