package net.seninp.gi.repair;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.HashSet;
import java.util.StringTokenizer;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.datastructures.SAXRecords;
import net.seninp.jmotif.sax.parallel.ParallelSAXImplementation;
import org.junit.Test;

public class TestRePairDigramUtilization {

  private static final String TEST_DATASET_NAME = "src/resources/test-data/ecg0606_1.csv";

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
    RePairRule grammar = RePairFactory.buildGrammar(saxData);
    RePairRule.expandRules();

    // rebuild the input string using the grammar
    //
    String recoveredString = RePairRule.recoverString();

    // System.out.println("RePair grammar:\n" + RePairRule.toGrammarRules());

    // System.out.println("Recovered string:\n" + recoveredString);

    assertNotNull(grammar);
    assertTrue(inputString.trim().equalsIgnoreCase(recoveredString.trim()));

    // assert the digram use
    //
    RePairRule R0 = grammar.getRules().get(0);
    String r0String = R0.toRuleString();

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
