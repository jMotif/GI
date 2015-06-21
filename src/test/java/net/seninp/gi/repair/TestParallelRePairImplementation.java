package net.seninp.gi.repair;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.datastructures.SAXRecord;
import net.seninp.jmotif.sax.datastructures.SAXRecords;
import net.seninp.jmotif.sax.parallel.ParallelSAXImplementation;
import org.junit.Test;

public class TestParallelRePairImplementation {

  private static final String TEST_DATASET_NAME = "src/resources/test-data/ecg0606_1.csv";

  private static final Integer WINDOW_SIZE = 120;
  private static final Integer PAA_SIZE = 4;
  private static final Integer ALPHABET_SIZE = 3;

  private static final int THREADS_NUM = 3;

  private double[] ts1;

  /**
   * Test the simple SAX conversion.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testParallelRePairFullRun() throws Exception {

    // read the data
    //
    ts1 = TSProcessor.readFileColumn(TEST_DATASET_NAME, 0, 0);

    ParallelSAXImplementation ps = new ParallelSAXImplementation();
    SAXRecords saxData = ps.process(ts1, 3, WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE,
        NumerosityReductionStrategy.EXACT, 0.05);

    String inputString = saxData.getSAXString(" ");
    // System.out.println("Input string:\n" + inputString);
    saxData.buildIndex();

    // Date start = new Date();
    ParallelGrammarKeeper grammar = toGrammarKeeper(saxData);
    ParallelRePairImplementation pr = new ParallelRePairImplementation();
    ParallelGrammarKeeper res = pr.buildGrammar(grammar, THREADS_NUM);
    // Date grammarEnd = new Date();

    // System.out.println("RePair grammar:\n" + res.toGrammarRules());
    // System.out.println("Recovered string:\n" + res.r0ExpandedString);

    // System.out.println("Grammar built in  "
    // + SAXFactory.timeToString(start.getTime(), grammarEnd.getTime()));

    assertNotNull(res);
    res.expandR0();
    assertTrue(inputString.trim().equalsIgnoreCase(res.r0ExpandedString.trim()));
    
  }

  private ParallelGrammarKeeper toGrammarKeeper(SAXRecords saxData) {
    ArrayList<Symbol> string = new ArrayList<Symbol>();
    for (int i = 0; i < saxData.size(); i++) {
      SAXRecord r = saxData.getByIndex(saxData.mapStringIndexToTSPosition(i));
      Symbol symbol = new Symbol(r, i);
      string.add(symbol);
    }
    // System.out.println("Converted str: " + stringToDisplay(string));

    ParallelGrammarKeeper gk = new ParallelGrammarKeeper(0);
    gk.setWorkString(string);
    return gk;
  }

  @SuppressWarnings("unused")
  private static String stringToDisplay(ArrayList<Symbol> string) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < string.size(); i++) {
      sb.append(string.get(i).toString()).append(" ");
    }
    return sb.toString();
  }
}
