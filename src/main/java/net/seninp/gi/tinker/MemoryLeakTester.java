package net.seninp.gi.tinker;

import net.seninp.gi.repair.RePairGrammar;
import net.seninp.gi.rpr.NewRepair;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;

public class MemoryLeakTester {

  private static final String INPUT_FNAME = "src/resources/test-data/ecg0606.txt";

  private static final int SAX_WIN_SIZE = 100;
  private static final int SAX_PAA_SIZE = 4;
  private static final int SAX_A_SIZE = 3;

  private static final double SAX_NORM_THRESHOLD = 0.001;

  private static final SAXProcessor sp = new SAXProcessor();

  private static final Alphabet na = new NormalAlphabet();

  public static void main(String[] args) throws Exception {

    try {
      Thread.sleep(10000); // 1000 milliseconds is one second.
    }
    catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }

    double[] ts = TSProcessor.readFileColumn(INPUT_FNAME, 0, 0);
    System.out.println("Read " + ts.length + " points from " + INPUT_FNAME);

    try {
      Thread.sleep(10000); // 1000 milliseconds is one second.
    }
    catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }

    for (int i = 0; i < 20; i++) {

      System.out.println("Iteration " + i);
      System.gc();

      SAXRecords sax = sp.ts2saxViaWindow(ts, SAX_WIN_SIZE, SAX_PAA_SIZE, na.getCuts(SAX_A_SIZE),
          NumerosityReductionStrategy.EXACT, SAX_NORM_THRESHOLD);

      RePairGrammar grammar = NewRepair.parse(sax.getSAXString(" "));

      System.out.println("Inferred " + grammar.getRules().size() + " rules.");

      try {
        Thread.sleep(10000); // 1000 milliseconds is one second.
      }
      catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }

    }

  }

}
