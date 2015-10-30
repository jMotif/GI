package net.seninp.gi.repair;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.HashSet;
import java.util.StringTokenizer;
import org.junit.Test;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import net.seninp.jmotif.sax.parallel.ParallelSAXImplementation;

public class TestRePairDigramUtilization {

  private static final String TEST_DATASET_NAME = "src/resources/test-data/ecg0606.txt";

  private static final Integer WINDOW_SIZE = 100;
  private static final Integer PAA_SIZE = 3;
  private static final Integer ALPHABET_SIZE = 3;

  private double[] ts1;

  /**
   * Test the simple SAX conversion.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testRePairImplementation() throws Exception {

    // read data
    //
    ts1 = TSProcessor.readFileColumn(TEST_DATASET_NAME, 0, 0);

    // convert to SAX
    //
    ParallelSAXImplementation ps = new ParallelSAXImplementation();
    SAXRecords saxData = ps.process(ts1, 2, WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE,
        NumerosityReductionStrategy.EXACT, 0.01);
    saxData.buildIndex();

    // build a grammar
    //
    String inputString = saxData.getSAXString(" ");
    // System.out.println("Input string:\n" + inputString);
    RePairGrammar grammar = RePairFactory.buildGrammar(saxData);
    grammar.expandRules();

    GrammarRules rulesData = grammar.toGrammarRulesData();

    assertNotNull(grammar);
    assertNotNull(rulesData);

    String recoveredInputString = rulesData.get(0).getExpandedRuleString();

    // System.out.println("RePair grammar:\n" + RePairRule.toGrammarRules());
    // System.out.println("Recovered string:\n" + recoveredString);
    assertTrue(inputString.trim().equalsIgnoreCase(recoveredInputString.trim()));

    // assert the digram use
    //
    String r0String = rulesData.get(0).getRuleString();

    HashSet<String> digrams = new HashSet<String>();
    String oldToken = "";
    StringTokenizer st = new StringTokenizer(r0String, " ");
    while (st.hasMoreTokens()) {

      String cToken = st.nextToken();
      if (oldToken.isEmpty()) {
        oldToken = cToken;
        continue;
      }

      String digram = oldToken + " " + cToken;
      if (digrams.contains(digram)) {
        fail("no digram should be repeated!");
      }
      else {
        digrams.add(digram);
      }

      oldToken = cToken;
    }

  }

}
