package net.seninp.gi.repair;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;

public class TestGrammarByDecompressing {

  private static final char THE_R = 'R';

  private static final char SPACE = ' ';

  private static final String INPUT_FNAME = "src/resources/test-data/ecg0606.txt";

  private static final int SAX_WIN_SIZE = 160;
  private static final int SAX_PAA_SIZE = 5;
  private static final int SAX_A_SIZE = 4;
  private static final double SAX_NORM_THRESHOLD = 0.001;

  private static final SAXProcessor sp = new SAXProcessor();
  private static final Alphabet na = new NormalAlphabet();

  private String inputSAXString;

  @Before
  public void initialize() throws IOException, SAXException {

    double[] ts = TSProcessor.readFileColumn(INPUT_FNAME, 0, 0);

    SAXRecords sax = sp.ts2saxViaWindow(ts, SAX_WIN_SIZE, SAX_PAA_SIZE, na.getCuts(SAX_A_SIZE),
        NumerosityReductionStrategy.EXACT, SAX_NORM_THRESHOLD);

    inputSAXString = sax.getSAXString(" ");

    inputSAXString = "dacb bbbd bbcb bdbb cbbc accb ccbc dbba cbbc bbdb bcbb dbbc bbcb adcc ccbc daba cbbc bbdb bcbb dbbc bbcb adcb bdac dcbb cbbd bbcb bdbb cbbc accb cdbb dbbb cbbd bccb bdab cbbc accb cdbb dbbb cbbd bbcb bdbb cbbc accc ccbb cbbd bbcb bdbb cbbc accb";

    // System.out.println(this.inputSAXString);

  }

  @Test
  public void testByDecompressing() {

    RePairGrammar repairGrammar = NewRepair.parse(inputSAXString);

    String resultString = new String(repairGrammar.r0String);

    int currentSearchStart = resultString.indexOf(THE_R);
    while (currentSearchStart >= 0) {
      int spaceIdx = resultString.indexOf(SPACE, currentSearchStart);
      String ruleName = resultString.substring(currentSearchStart, spaceIdx + 1);
      Integer ruleId = Integer.valueOf(ruleName.substring(1, ruleName.length() - 1));
      RePairRule rule = repairGrammar.getRules().get(ruleId);
      if (rule != null) {
        if (rule.expandedRuleString.charAt(rule.expandedRuleString.length() - 1) == ' ') {
          resultString = resultString.replaceAll(ruleName, rule.expandedRuleString);
        }
        else {
          resultString = resultString.replaceAll(ruleName, rule.expandedRuleString + SPACE);
        }
      }
      currentSearchStart = resultString.indexOf("R", spaceIdx);
    }

    assertTrue("asserting new implementation correctness",
        inputSAXString.equalsIgnoreCase(resultString.trim()));
    //
    // System.out.println(repairGrammar.r0String + "\n" + resultString);

  }

}
