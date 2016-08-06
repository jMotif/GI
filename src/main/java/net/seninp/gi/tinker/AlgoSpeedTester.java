package net.seninp.gi.tinker;

import java.util.Date;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.repair.RePairFactory;
import net.seninp.gi.repair.RePairGrammar;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;

public class AlgoSpeedTester {

  private static final String INPUT_FNAME = "/Users/psenin/workspace/grammarviz2_src/data/300_signal1.txt";

  private static final int SAX_WIN_SIZE = 300;
  private static final int SAX_PAA_SIZE = 6;
  private static final int SAX_A_SIZE = 5;

  private static final double SAX_NORM_THRESHOLD = 0.001;

  private static final SAXProcessor sp = new SAXProcessor();

  private static final Alphabet na = new NormalAlphabet();

  public static void main(String[] args) throws Exception {

    Date startTime = new Date();

    double[] ts = TSProcessor.readFileColumn(INPUT_FNAME, 0, 0);
    Date readTime = new Date();
    System.out.println(SAXProcessor.timeToString(startTime.getTime(), readTime.getTime())
        + ":\t read " + ts.length + " points from " + INPUT_FNAME);

    SAXRecords sax = sp.ts2saxViaWindow(ts, SAX_WIN_SIZE, SAX_PAA_SIZE, na.getCuts(SAX_A_SIZE),
        NumerosityReductionStrategy.NONE, SAX_NORM_THRESHOLD);
    Date saxTime = new Date();
    System.out.println(SAXProcessor.timeToString(readTime.getTime(), saxTime.getTime())
        + ":\t discretized " + ts.length + " points into " + sax.getIndexes().size() + " words.");

    String str = sax.getSAXString(" ");

    for (int i = 0; i < 20; i++) {

      System.out.println("Iteration " + i);

      // Sequitur
      // prepareCycle();
      // Date t0 = new Date();
      // SAXRule grammar = SequiturFactory.runSequitur(str);
      // GrammarRules rulesData = grammar.toGrammarRulesData();
      // System.out.println(SAXProcessor.timeToString(t0.getTime(), (new Date()).getTime())
      // + "\t inferred " + rulesData.size() + " Sequitur rules");
      // grammar = null;
      // rulesData = null;

      // New repair
      prepareCycle();
      Date t1 = new Date();
      RePairGrammar grammar1 = RePairFactory.buildGrammar(str);
      GrammarRules rulesData1 = grammar1.toGrammarRulesData();
      System.out.println(SAXProcessor.timeToString(t1.getTime(), (new Date()).getTime())
          + "\t inferred " + rulesData1.size() + " RePair rules..");
      // System.out.println(rulesData1.toString());
      grammar1 = null;

    }

  }

  private static void prepareCycle() {
    System.gc();
    try {
      Thread.sleep(10000); // 1000 milliseconds is one second.
    }
    catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }

}
