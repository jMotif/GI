package net.seninp.gi.repair;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TestGrammarByDecompressing {

  private static final char THE_R = 'R';

  private static final char SPACE = ' ';

  /**
   * Regression string from {@code jmotif-R/inst/test_data/bugs.R}; RePair must decompress back to
   * the original SAX token sequence.
   */
  private static final String JMOTIF_R_BUGS_SAX_STRING = "dacb bbbd bbcb bdbb cbbc accb ccbc dbba cbbc bbdb bcbb dbbc bbcb adcc ccbc daba cbbc bbdb bcbb dbbc bbcb adcb bdac dcbb cbbd bbcb bdbb cbbc accb cdbb dbbb cbbd bccb bdab cbbc accb cdbb dbbb cbbd bbcb bdbb cbbc accc ccbb cbbd bbcb bdbb cbbc accb";

  @Test
  public void testByDecompressing() {

    RePairGrammar repairGrammar = NewRepair.parse(JMOTIF_R_BUGS_SAX_STRING);

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
        JMOTIF_R_BUGS_SAX_STRING.equalsIgnoreCase(resultString.trim()));
  }

}
