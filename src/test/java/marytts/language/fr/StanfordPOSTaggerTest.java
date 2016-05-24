package marytts.language.fr;

import java.util.ArrayList;
import java.util.Locale;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

/* testng part */
import org.testng.Assert;
import org.testng.annotations.*;

/* Marytts needed packages */
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.modules.MaryModule;
import marytts.util.MaryUtils;

/* MaryData needed packages */
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;



/**
 * Test class for the stanford POS tagger support for french
 *
 * @author <a href="mailto:slemaguer@coli.uni-saarland.de">Sébastien Le Maguer</a>
 */
public class StanfordPOSTaggerTest
{
    MaryInterface mary;

    @BeforeSuite
	public void setupClass() throws Exception {
        mary = new LocalMaryInterface();
        mary.setOutputType("PARTSOFSPEECH");
        Assert.assertEquals("PARTSOFSPEECH", mary.getOutputType());
    }


	protected String loadResourceIntoString(String resourceName) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(resourceName), "UTF-8"));
		StringBuilder buf = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			buf.append(line);
			buf.append("\n");
		}
		return buf.toString();
	}

    /**
     * Check baseline for POS tagging
     *
     */
	@Test
	public void checkPOSTagging() throws Exception {

        Locale loc = Locale.FRENCH;
        mary.setLocale(loc);
		Assert.assertEquals(loc, mary.getLocale());

		mary.setOutputType("PARTSOFSPEECH");
        String text = loadResourceIntoString("utt1.txt");
        Document doc = mary.generateXML(text);
        Assert.assertNotNull(doc);
    }
}
