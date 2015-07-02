package net.seninp.gi.prunetinker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import net.seninp.gi.GrammarRuleRecord;
import net.seninp.gi.GrammarRules;
import net.seninp.gi.RuleInterval;
import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.datastructures.SAXRecords;
import net.seninp.jmotif.sax.parallel.ParallelSAXImplementation;

public class CompressionPrinter {

  private static final String TEST_DATASET_NAME = "src/resources/test-data/ecg0606_1.csv";

  private static final Integer WINDOW_SIZE = 100;
  private static final Integer PAA_SIZE = 3;
  private static final Integer ALPHABET_SIZE = 3;

  private static double[] ts1;

  public static void main(String[] args) throws Exception {

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

    SAXRule r = SequiturFactory.runSequitur(inputString);
    GrammarRules rules = r.toGrammarRulesData();
    SequiturFactory.updateRuleIntervals(rules, saxData, true, ts1, WINDOW_SIZE, PAA_SIZE);

    GrammarRules prunedRules = performPruning(rules);

  }

  private static GrammarRules performPruning(GrammarRules grammarRules) {
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

      if (0.0 == bestDelta) {
        break;
      }

      // keep track of cover
      //
      usedRules.add(bestRule.getRuleNumber());
      range = updateRanges(range, bestRule.getRuleIntervals());
    }

    System.out.println("Best cover "
        + Arrays.toString(usedRules.toArray(new Integer[usedRules.size()])));

    GrammarRules prunedRules = new GrammarRules();
    prunedRules.addRule(grammarRules.get(0));

    for (Integer rId : usedRules) {
      prunedRules.addRule(grammarRules.get(rId));
    }

    return prunedRules;

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
    System.out.println(sb);
    for (boolean p : range) {
      if (false == p) {
        return true;
      }
    }
    return false;
  }

}
