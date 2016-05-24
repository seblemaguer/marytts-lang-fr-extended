/**
 * Copyright 2007 DFKI GmbH.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * This file is part of MARY TTS.
 *
 * MARY TTS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package marytts.language.fr;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.datatypes.MaryXML;
import marytts.server.MaryProperties;
import marytts.util.MaryUtils;
import marytts.modules.InternalModule;
import marytts.util.dom.MaryDomUtils;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.traversal.TreeWalker;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * Part-of-speech tagger using Stanford.
 *
 * @author Marc Schr&ouml;der
 */

public class StanfordPOSTagger extends InternalModule {
    protected String model_resource;
    private String propertyPrefix;
	private MaxentTagger tagger;
	private Map<String, String> posMapper = null;

	/**
	 * Constructor which can be directly called from init info in the config file. Different
	 * languages can call this code with different settings.
	 *
	 * @param locale
	 *            a locale string, e.g. "en"
	 * @param propertyPrefix
	 *            propertyPrefix
	 * @throws Exception
	 *             Exception
	 */
	public StanfordPOSTagger(String locale, String propertyPrefix)
        throws Exception
    {
		super("StanfordPOSTagger",
              MaryDataType.WORDS,
              MaryDataType.PARTSOFSPEECH,
              MaryUtils.string2locale(locale));
		if (!propertyPrefix.endsWith("."))
			propertyPrefix = propertyPrefix + ".";
		this.propertyPrefix = propertyPrefix;
        this.model_resource = this.getClass().getResource("french.tagger").toString();
    }

	public void startup()
        throws Exception
    {
		super.startup();
        tagger = new MaxentTagger(model_resource);
    }

    @SuppressWarnings("unchecked")
    public MaryData process(MaryData d)
        throws Exception
    {
        Document doc = d.getDocument();
        NodeIterator sentenceIt = MaryDomUtils.createNodeIterator(doc, doc, MaryXML.SENTENCE);
        Element sentence;
        while ((sentence = (Element) sentenceIt.nextNode()) != null)
        {
            TreeWalker tokenIt = MaryDomUtils.createTreeWalker(sentence, MaryXML.TOKEN);

            // Generate the sentence as a word list
            List<String> tokens = new ArrayList<String>();
            String str_sentence = "";
            Element t;
            while ((t = (Element) tokenIt.nextNode()) != null) {
                tokens.add(MaryDomUtils.tokenText(t));
                str_sentence += MaryDomUtils.tokenText(t) + " ";
            }
            if (tokens.size() == 1) {
                str_sentence += MaryDomUtils.tokenText(t) + ".";
                tokens.add(".");
            }

            // Tagging
            List<String> partsOfSpeech = new ArrayList<String>();
            synchronized (this)
            {
                // FIXME: is it more efficient ?
                // String tagstr = tagger.tagString(str_sentence);
                // String[] tagged_items = tagstr.split(" ");
                // for (String item: tagged_items)
                // {
                //     System.out.println("item = " + item);
                //     String[] elts = item.split("_"); // FIXME : separtor should be a constant

                //     System.out.println("item = " + elts[0]);
                //     partsOfSpeech.add(elts[1]);
                // }

                List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(str_sentence));
                for (List<HasWord> s : sentences)
                {
                    List<TaggedWord> tSentence = tagger.tagSentence(s);
                    for (TaggedWord word : tSentence)
                    {
                        System.out.println("word = " + word.word());
                        partsOfSpeech.add(word.tag());
                        System.out.println("tag = " + word.tag());
                    }
                }
                assert(partsOfSpeech.size() == tokens.size());
            }

            // Adding the tag to the current tree
            //   1. Reset the tree walker to enable another walk through
            tokenIt.setCurrentNode(sentence);

            //   2. Annotate
            Iterator<String> posIt = partsOfSpeech.iterator();
            while ((t = (Element) tokenIt.nextNode()) != null) {
                assert posIt.hasNext();
                String pos = posIt.next();
                if (t.hasAttribute("pos")) {
                    continue;
                }
                if (posMapper != null) {
                    String gpos = posMapper.get(pos);
                    if (gpos == null)
                        logger.warn("POS map file incomplete: do not know how to map '" + pos + "'");
                    else
                        pos = gpos;
                }
                t.setAttribute("pos", pos);
            }
        }

        // Return the achieved document
        MaryData output = new MaryData(outputType(), d.getLocale());
        output.setDocument(doc);
        return output;
    }

}
