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
        for (ALPHABET_SIZE = 2; ALPHABET_SIZE < 13; ALPHABET_SIZE++) {

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

          Integer size = computeSize(rules, saxData, PAA_SIZE);

          GrammarRules compressedGrammar = performCompression(rules);
          Integer compressedSize = computeSize(compressedGrammar, saxData, PAA_SIZE);

          double approximationDistance = sp.approximationDistance(ts1, WINDOW_SIZE, PAA_SIZE,
              ALPHABET_SIZE, STRATEGY, NORMALIZATION_THRESHOLD);

          boolean[] compressedCover = new boolean[ts1.length];
          compressedCover = updateRanges(compressedCover, compressedGrammar);

          if (hasEmptyRanges(compressedCover)) {
            logStr.append("0").append(COMMA);
          }
          else {
            logStr.append("1").append(COMMA);
          }

          logStr.append(size).append(COMMA);
          logStr.append(compressedSize).append(COMMA);
          logStr.append(approximationDistance).append(CR);

          bw.write(logStr.toString());
          consoleLogger.info(logStr.toString().replace(CR, ""));

          // GrammarRules prunedRules = performPruning(rules);
          //
          // consoleLogger.info(logStr.toString());
          //
          // if (null == prunedRules) {
          // bw.write(logStr.toString() + Integer.MAX_VALUE + CR);
          // }
          // else {
          // ArrayList<Integer> prunedRuleNums = new ArrayList<Integer>();
          // for (GrammarRuleRecord rule : prunedRules) {
          // prunedRuleNums.add(rule.getRuleNumber());
          // }
          // // logStr
          // // .append(Arrays.toString(prunedRuleNums.toArray(new
          // Integer[prunedRuleNums.size()])));
          // logStr.append(prunedRuleNums.size());
          // consoleLogger.info(logStr.toString());
          // }
          // bw.write(logStr.toString() + CR);
        }
      }
    }
    bw.close();

  }

  /**
   * Computes the grammar size.
   * 
   * @param rules the grammar rules.
   * @param saxData the original SAX data.
   * @param paaSize the SAX transform word size.
   * 
   * @return the grammar size.
   */
  private static Integer computeSize(GrammarRules rules, SAXRecords saxData, Integer paaSize) {

    // first we compute the cover by rules
    //
    boolean[] range = new boolean[ts1.length];

    for (GrammarRuleRecord r : rules) {
      if (0 == r.getRuleNumber()) {
        continue;
      }
      updateRanges(range, r.getRuleIntervals());
    }

    int res = 0;

    if (isCovered(range)) {

      // if all is covered
      //
      for (GrammarRuleRecord r : rules) {
        if (0 == r.getRuleNumber()) {
          // skip the rule zero
          continue;
        }
        // the increment is computed as the size of the expanded rule string (bytes)
        // plus the number of occurrences * 2 (a word per each occurrence)
        //
        res = res + r.getExpandedRuleString().replaceAll("\\s", "").length()
            + r.getOccurrences().size() * 2;
      }

    }
    else {

      for (GrammarRuleRecord r : rules) {
        if (0 == r.getRuleNumber()) {
          // skip the rule zero
          continue;
        }
        // the increment is computed as the size of the expanded rule string (bytes)
        // plus the number of occurrences * 2 (a word per each occurrence)
        //
        res = res + r.getExpandedRuleString().replaceAll("\\s", "").length()
            + r.getOccurrences().size() * 2;
      }

      for (int i = 0; i < range.length; i++) {
        if (false == range[i] && (null != saxData.getByIndex(i))) {
          // each uncovered by a rule position is actually an individual PAA word
          // and a position index
          //
          res = res + paaSize + 2;
        }
      }
    }
    return res;
  }

  private static boolean isCovered(boolean[] range) {
    for (boolean i : range) {
      if (false == i) {
        return false;
      }
    }
    return true;
  }

  private static GrammarRules performCompression(GrammarRules grammarRules) {

    // this is where we keep range coverage
    boolean[] range = new boolean[ts1.length];
    // these are rules used in current cover
    HashSet<Integer> usedRules = new HashSet<Integer>();
    usedRules.add(0);
    // do until all ranges are covered
    while (hasEmptyRanges(range)) {

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

      if (null == bestRule) {
        break;
      }

      if (0.0 == bestDelta) {
        break;
      }

      // System.out.println("Adding the best rule: " + bestRule.getRuleNumber());
      usedRules.add(bestRule.getRuleNumber());
      // System.out.println("Pruning set by overlaps...");

      // check for overlap artifacts
      //
      boolean continueSearch = true;
      while (continueSearch) {

        continueSearch = false;

        for (int rid : usedRules) {

          if (0 == rid) {
            continue;
          }

          ArrayList<RuleInterval> intervalsA = grammarRules.get(rid).getRuleIntervals();

          ArrayList<RuleInterval> intervalsB = new ArrayList<RuleInterval>();

          for (int ridB : usedRules) {
            if (0 == ridB || rid == ridB) {
              continue;
            }
            intervalsB.addAll(grammarRules.get(ridB).getRuleIntervals());
          }

          if (intervalsB.isEmpty()) {
            break;
          }
          else if (isCompletlyCovered(intervalsB, intervalsA)) {
            // System.out.println("Going to remove rule: " + grammarRules.get(rid).getRuleName());
            usedRules.remove(rid);
            continueSearch = true;
            break;
          }

        }
      }

      // add the new candidate and keep the track of cover
      //
      range = updateRanges(range, bestRule.getRuleIntervals());
    }

    // System.out.println("Best cover "
    // + Arrays.toString(usedRules.toArray(new Integer[usedRules.size()])));

    GrammarRules prunedRules = new GrammarRules();
    prunedRules.addRule(grammarRules.get(0));

    for (Integer rId : usedRules) {
      prunedRules.addRule(grammarRules.get(rId));
    }

    return prunedRules;

  }

  private static boolean isCompletlyCovered(ArrayList<RuleInterval> cover,
      ArrayList<RuleInterval> intervals) {

    int min = cover.get(0).getStartPos();
    int max = cover.get(0).getEndPos();
    for (RuleInterval i : cover) {
      if (i.getStartPos() < min) {
        min = i.getStartPos();
      }
      if (i.getEndPos() > max) {
        max = i.getEndPos();
      }
    }

    boolean[] coverrange = new boolean[max - min];

    for (RuleInterval i : cover) {
      for (int j = i.getStartPos(); j < i.getEndPos(); j++) {
        coverrange[j - min] = true;
      }
    }

    boolean covered = true;
    for (RuleInterval i : intervals) {
      for (int j = i.getStartPos(); j < i.getEndPos(); j++) {
        if (j < min || j >= max) {
          covered = false;
          break;
        }
        if (coverrange[j - min]) {
          continue;
        }
        else {
          covered = false;
          break;
        }
      }
    }

    return covered;
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

  private static boolean[] updateRanges(boolean[] range, GrammarRules grammar) {
    boolean[] res = Arrays.copyOf(range, range.length);
    for (GrammarRuleRecord r : grammar) {
      if (0 == r.getRuleNumber()) {
        continue;
      }
      res = updateRanges(res, r.getRuleIntervals());
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
      return (double) cover
          / (double) (rule.getExpandedRuleString().length() + rule.getRuleIntervals().size());
    }
    // else divide newly covered points mount by the sum of the rule string length and occurrence
    // (i.e. encoding size)
    return ((double) cover / (double) (cover + overlap))
        / (double) (rule.getExpandedRuleString().length() + rule.getRuleIntervals().size());
  }

  private static boolean hasEmptyRanges(boolean[] range) {
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
    // System.out.println(sb);
    for (boolean p : range) {
      if (false == p) {
        return true;
      }
    }
    return false;
  }
}
