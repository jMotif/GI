package net.seninp.gi.performance;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import net.seninp.gi.repair.ParallelGrammarKeeper;
import net.seninp.gi.repair.ParallelRePairImplementation;
import net.seninp.gi.repair.RePairFactory;
import net.seninp.gi.repair.RePairGrammar;
import net.seninp.gi.repair.RePairSymbol;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructures.SAXRecord;
import net.seninp.jmotif.sax.datastructures.SAXRecords;

public class EvaluateParallelRePair {

  private static final String INPUT_DATA_FNAME = "src/resources/test-data/300_signal1.txt.gz";

  private static SAXProcessor sp = new SAXProcessor();
  private static Alphabet a = new NormalAlphabet();

  public static void main(String[] args) throws IOException, SAXException {

    // read the data
    //
    Date start = new Date();
    InputStream fileStream = new FileInputStream(INPUT_DATA_FNAME);
    InputStream gzipStream = new GZIPInputStream(fileStream);
    Reader decoder = new InputStreamReader(gzipStream);
    BufferedReader br = new BufferedReader(decoder);

    ArrayList<Double> preRes = new ArrayList<Double>();
    String line = null;
    while ((line = br.readLine()) != null) {
      Double num = Double.valueOf(line);
      preRes.add(num);
    }
    br.close();
    double[] res = new double[preRes.size()];
    for (int i = 0; i < preRes.size(); i++) {
      res[i] = preRes.get(i);
    }
    Date finish = new Date();
    System.out.println("read " + res.length + " points in "
        + SAXProcessor.timeToString(start.getTime(), finish.getTime()));

    // perform SAX
    //
    start = new Date();
    SAXRecords tokens = sp.ts2saxViaWindow(res, 120, 6, a.getCuts(3),
        NumerosityReductionStrategy.EXACT, 0.01);
    tokens.buildIndex();
    String str = tokens.getSAXString(" ");
    finish = new Date();

    System.out.println("extracted "
        + Integer.valueOf(str.length() - str.replaceAll(" ", "").length()).toString()
        + " tokens in " + SAXProcessor.timeToString(start.getTime(), finish.getTime()));
    System.out.println("# " + Long.valueOf(finish.getTime() - start.getTime()));

    // sequential Re-Pair
    //
    start = new Date();
    RePairGrammar g = RePairFactory.buildGrammar(str);
    finish = new Date();
    System.out.println("inferred " + g.getRules().size() + " RePair rules in "
        + SAXProcessor.timeToString(start.getTime(), finish.getTime()));
    String sequentialStr = g.toGrammarRulesData().get(0).getExpandedRuleString().trim();
    System.out.println("# " + Long.valueOf(finish.getTime() - start.getTime()));

    // the parallel repair
    //
    for (int threadsNum = 2; threadsNum < 16; threadsNum++) {
      start = new Date();
      ParallelGrammarKeeper grammar = toGrammarKeeper(tokens);
      ParallelRePairImplementation pr = new ParallelRePairImplementation();
      ParallelGrammarKeeper pg = pr.buildGrammar(grammar, 2);
      pg.expandRules();
      pg.expandR0();
      finish = new Date();
      System.out.println("inferred " + g.getRules().size() + " RePair rules using " + threadsNum
          + " threads in " + SAXProcessor.timeToString(start.getTime(), finish.getTime()));
      System.out.println("# " + Long.valueOf(finish.getTime() - start.getTime()));
      String parallelString = pg.getR0ExpandedString().trim();
      System.out.println("String equals test:  " + sequentialStr.equalsIgnoreCase(parallelString));
    }

  }

  private static ParallelGrammarKeeper toGrammarKeeper(SAXRecords saxData) {
    ArrayList<RePairSymbol> string = new ArrayList<RePairSymbol>();
    for (int i = 0; i < saxData.size(); i++) {
      SAXRecord r = saxData.getByIndex(saxData.mapStringIndexToTSPosition(i));
      RePairSymbol symbol = new RePairSymbol(r, i);
      string.add(symbol);
    }
    // System.out.println("Converted str: " + stringToDisplay(string));

    ParallelGrammarKeeper gk = new ParallelGrammarKeeper(0);
    gk.setWorkString(string);
    return gk;
  }

}
