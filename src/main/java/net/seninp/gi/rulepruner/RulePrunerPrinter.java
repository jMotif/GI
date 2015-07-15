package net.seninp.gi.rulepruner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import net.seninp.gi.GIAlgorithm;
import net.seninp.gi.GrammarRuleRecord;
import net.seninp.gi.GrammarRules;
import net.seninp.gi.RuleInterval;
import net.seninp.gi.repair.RePairFactory;
import net.seninp.gi.repair.RePairGrammar;
import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.datastructures.SAXRecords;
import net.seninp.jmotif.sax.parallel.ParallelSAXImplementation;
import net.seninp.util.StackTrace;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.beust.jcommander.JCommander;

public class RulePrunerPrinter {

  private static final String COMMA = ",";
  private static final String CR = "\n";

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(RulePrunerPrinter.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public static void main(String[] args) throws Exception {

    try {

      RulePrunerParameters params = new RulePrunerParameters();
      JCommander jct = new JCommander(params, args);

      if (0 == args.length) {
        jct.usage();
      }
      else {
        // get params printed
        //
        StringBuffer sb = new StringBuffer(1024);
        sb.append("Rule pruner CLI v.1").append(CR);
        sb.append("parameters:").append(CR);

        sb.append("  input file:                  ").append(RulePrunerParameters.IN_FILE).append(CR);
        sb.append("  output file:                 ").append(RulePrunerParameters.OUT_FILE).append(CR);
        sb.append("  SAX numerosity reduction:    ").append(RulePrunerParameters.SAX_NR_STRATEGY).append(CR);
        sb.append("  SAX normalization threshold: ").append(RulePrunerParameters.SAX_NORM_THRESHOLD).append(CR);
        sb.append("  GI Algorithm:                ").append(RulePrunerParameters.GI_ALGORITHM_IMPLEMENTATION).append(CR);
        sb.append("  Grid boundaries:             ").append(RulePrunerParameters.GRID_BOUNDARIES).append(CR);

        String dataFName = RulePrunerParameters.IN_FILE;
        double[] ts = TSProcessor.readFileColumn(dataFName, 0, 0);
        
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(RulePrunerParameters.OUT_FILE)));
        bw.write("window,paa,alphabet,isCovered,grammarsize,compressedGrammarSize,approxDist\n");

        int[] boundaries = toBoundaries(RulePrunerParameters.GRID_BOUNDARIES);

        SAXProcessor sp = new SAXProcessor();

        System.err.println(sb.toString());
        
        for (int WINDOW_SIZE = boundaries[0]; WINDOW_SIZE < boundaries[1]; WINDOW_SIZE += boundaries[2]) {
          for (int PAA_SIZE = boundaries[3]; PAA_SIZE < boundaries[4]; PAA_SIZE += boundaries[5]) {
            if (PAA_SIZE > WINDOW_SIZE) {
              continue;
            }
            for (int ALPHABET_SIZE = boundaries[6]; ALPHABET_SIZE < boundaries[7]; ALPHABET_SIZE += boundaries[8]) {

              StringBuffer logStr = new StringBuffer();
              logStr.append(WINDOW_SIZE).append(COMMA).append(PAA_SIZE).append(COMMA)
                  .append(ALPHABET_SIZE).append(COMMA);

              // convert to SAX
              //
              ParallelSAXImplementation ps = new ParallelSAXImplementation();
              SAXRecords saxData = ps.process(ts, 2, WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE,
                  RulePrunerParameters.SAX_NR_STRATEGY, RulePrunerParameters.SAX_NORM_THRESHOLD);
              saxData.buildIndex();

              // build a grammar
              //
              GrammarRules rules = null;
              if (GIAlgorithm.SEQUITUR.equals(RulePrunerParameters.GI_ALGORITHM_IMPLEMENTATION)) {
                SAXRule r = SequiturFactory.runSequitur(saxData.getSAXString(" "));
                rules = r.toGrammarRulesData();
                SequiturFactory
                    .updateRuleIntervals(rules, saxData, true, ts, WINDOW_SIZE, PAA_SIZE);
              }
              else if (GIAlgorithm.REPAIR.equals(RulePrunerParameters.GI_ALGORITHM_IMPLEMENTATION)) {
                RePairGrammar grammar = RePairFactory.buildGrammar(saxData.getSAXString(" "));
                rules = grammar.toGrammarRulesData();
              }

              Integer size = computeSize(ts, rules, saxData, PAA_SIZE);

              GrammarRules compressedGrammar = performCompression(ts, rules);
              Integer compressedSize = computeSize(ts, compressedGrammar, saxData, PAA_SIZE);

              double approximationDistance = sp.approximationDistance(ts, WINDOW_SIZE, PAA_SIZE,
                  ALPHABET_SIZE, RulePrunerParameters.SAX_NR_STRATEGY,
                  RulePrunerParameters.SAX_NORM_THRESHOLD);

              boolean[] compressedCover = new boolean[ts.length];
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
             
            }
          }
        }
        bw.close();

      }
    }
    catch (Exception e) {
      System.err.println("error occured while parsing parameters " + Arrays.toString(args) + CR
          + StackTrace.toString(e));
      System.exit(-1);
    }

  }

  /**
   * Converts a param string to boundaries array.
   * 
   * @param str
   * @return
   */
  private static int[] toBoundaries(String str) {
    int[] res = new int[9];
    String[] split = str.split("\\s+");
    for (int i = 0; i < 9; i++) {
      res[i] = Integer.valueOf(split[i]);
    }
    return res;
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
  private static Integer computeSize(double[] ts1, GrammarRules rules, SAXRecords saxData,
      Integer paaSize) {

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

  private static GrammarRules performCompression(double[] ts1, GrammarRules grammarRules) {

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
