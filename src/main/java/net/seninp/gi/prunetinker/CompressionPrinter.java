package net.seninp.gi.prunetinker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import net.seninp.gi.GrammarRuleRecord;
import net.seninp.gi.GrammarRules;
import net.seninp.gi.RuleInterval;
import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.datastructures.SAXRecords;
import net.seninp.jmotif.sax.parallel.ParallelSAXImplementation;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class CompressionPrinter {

  private static final String TEST_DATASET_NAME = "src/resources/test-data/ecg0606_1.csv";

  private static final String COMMA = ",";

  private static final String CR = "\n";

  private static Integer WINDOW_SIZE = 83;
  private static Integer PAA_SIZE = 29;
  private static Integer ALPHABET_SIZE = 8;
  private static final double NORMALIZATION_THRESHOLD = 0.01;
  private static final NumerosityReductionStrategy STRATEGY = NumerosityReductionStrategy.EXACT;

  private static double[] ts1;

  private static SAXProcessor sp = new SAXProcessor();

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(CompressionPrinter.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public static void main(String[] args) throws Exception {

    // read data
    //
    ts1 = TSProcessor.readFileColumn(TEST_DATASET_NAME, 0, 0);

    BufferedWriter bw = new BufferedWriter(new FileWriter(new File("rules_num.txt")));
    bw.write("window,paa,alphabet,rules_num,approx_dist\n");

    for (WINDOW_SIZE = 30; WINDOW_SIZE < 600; WINDOW_SIZE += 10) {
      for (PAA_SIZE = 2; PAA_SIZE < 50; PAA_SIZE += 2) {
        if (PAA_SIZE > WINDOW_SIZE) {
          continue;
        }
        for (ALPHABET_SIZE = 2; ALPHABET_SIZE < 15; ALPHABET_SIZE++) {

          StringBuffer logStr = new StringBuffer();
          logStr.append(WINDOW_SIZE).append(COMMA).append(PAA_SIZE).append(COMMA)
              .append(ALPHABET_SIZE).append(COMMA);
          // convert to SAX
          //
          ParallelSAXImplementation ps = new ParallelSAXImplementation();
          SAXRecords saxData = ps.process(ts1, 2, WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE, STRATEGY,
              NORMALIZATION_THRESHOLD);
          saxData.buildIndex();

          // build a grammar
          //
          String inputString = saxData.getSAXString(" ");
          // System.out.println("Input string:\n" + inputString);

          SAXRule r = SequiturFactory.runSequitur(inputString);
          GrammarRules rules = r.toGrammarRulesData();
          SequiturFactory.updateRuleIntervals(rules, saxData, true, ts1, WINDOW_SIZE, PAA_SIZE);

          Integer size = performCompression(rules, saxData, WINDOW_SIZE);

          double approximationDistance = sp.approximationDistance(ts1, WINDOW_SIZE, PAA_SIZE,
              ALPHABET_SIZE, STRATEGY, NORMALIZATION_THRESHOLD);

          logStr.append(size).append(COMMA).append(approximationDistance).append(CR);

          bw.write(logStr.toString());
          consoleLogger.info(logStr.toString().replace(CR, ""));

        }
      }
    }

    bw.close();

  }

  private static Integer performCompression(GrammarRules grammarRules, SAXRecords saxData,
      Integer winSize) {

    // this is where we keep range coverage
    boolean[] range = new boolean[ts1.length];

    // goes false when some ranges not covered
    boolean isCovered = true;

    // these are rules used in current cover
    HashSet<Integer> usedRules = new HashSet<Integer>();
    usedRules.add(0);
    // do until all ranges are covered
    while (hasEmptyRanges(range, false)) {

      // iterate over rules set finding new optimal cover
      //
      GrammarRuleRecord bestRule = null;
      double bestDelta = Integer.MIN_VALUE;
      for (GrammarRuleRecord rule : grammarRules) {
        int id = rule.getRuleNumber();
        if (usedRules.contains(id)) {
          continue;
        }
        else {
          double delta = getCoverDelta(range, rule);
          if (delta > bestDelta) {
            bestDelta = delta;
            bestRule = rule;
          }
        }
      }
      if (bestDelta < 0) {
        isCovered = false;
        break;
      }

      if (0.0 == bestDelta) {
        break;
      }

      // keep track of cover
      //
      usedRules.add(bestRule.getRuleNumber());
      range = updateRanges(range, bestRule.getRuleIntervals());
    }

    int res = 0;

    if (isCovered) {
      res = SequiturFactory.computeGrammarSize(usedRules);
    }

    return res;

  }

  private static boolean[] updateRanges(boolean[] range, ArrayList<RuleInterval> ruleIntervals) {
    boolean[] res = Arrays.copyOf(range, range.length);
    for (RuleInterval i : ruleIntervals) {
      int start = i.getStartPos();
      int end = i.getEndPos();
      for (int j = start; j <= end; j++) {
        res[j] = true;
      }
    }
    return res;
  }

  private static double getCoverDelta(boolean[] range, GrammarRuleRecord rule) {

    // counts which uncovered points shall be covered
    int cover = 0;

    // counts overlaps with previously covered ranges
    int overlap = 0;

    for (RuleInterval i : rule.getRuleIntervals()) {
      int start = i.getStartPos();
      int end = i.getEndPos();
      for (int j = start; j <= end; j++) {
        if (false == range[j]) {
          cover++;
        }
        else {
          overlap++;
        }
      }
    }
    // if covers nothing, return 0
    if (0 == cover) {
      return 0.0;
    }
    // if zero overlap, return full cover
    if (0 == overlap) {
      return (double) cover;
    }
    // else divide newly covered points mount by the sum of the rule string length and occurrence
    // (i.e. encoding size)
    return ((double) cover / (double) overlap)
        / (double) (rule.getExpandedRuleString().length() + rule.getRuleIntervals().size());
  }

  private static boolean hasEmptyRanges(boolean[] range, boolean verbose) {
    StringBuffer sb = new StringBuffer();
    boolean inUncovered = false;
    int start = 0;
    for (int i = 0; i < range.length; i++) {
      if (false == range[i] && false == inUncovered) {
        start = i;
        inUncovered = true;
      }
      if (true == range[i] && true == inUncovered) {
        sb.append("[" + start + ", " + i + "], ");
        inUncovered = false;
      }
    }
    if (inUncovered) {
      sb.append("[" + start + ", " + range.length + "], ");
    }
    consoleLogger.debug(sb.toString());
    if (verbose) {
      System.out.println(sb.toString());
    }
    for (boolean p : range) {
      if (false == p) {
        return true;
      }
    }
    return false;
  }

}
