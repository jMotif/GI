package net.seninp.gi.rulepruner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import net.seninp.gi.GrammarRuleRecord;
import net.seninp.gi.GrammarRules;
import net.seninp.gi.RuleInterval;
import net.seninp.jmotif.sax.datastructures.SAXRecords;

/**
 * Pruner methods implementation.
 * 
 * @author psenin
 *
 */
public class RulePrunerFactory {

  /**
   * Performs pruning.
   * 
   * @param ts the input time series.
   * @param grammarRules the grammar.
   * @return pruned ruleset.
   */
  public static GrammarRules performPruning(double[] ts, GrammarRules grammarRules) {

    // this is where we keep range coverage
    boolean[] range = new boolean[ts.length];
    // these are rules used in current cover
    HashSet<Integer> usedRules = new HashSet<Integer>();
    usedRules.add(0);
    HashSet<Integer> removedRules = new HashSet<Integer>();
    // do until all ranges are covered
    while (hasEmptyRanges(range)) {

      // iterate over rules set finding new optimal cover
      //
      GrammarRuleRecord bestRule = null;
      double bestDelta = Integer.MIN_VALUE;
      for (GrammarRuleRecord rule : grammarRules) {
        int id = rule.getRuleNumber();
        if (usedRules.contains(id) || removedRules.contains(id)) {
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
            removedRules.add(rid);
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

    for (Integer rId : usedRules) {
      prunedRules.addRule(grammarRules.get(rId));
    }

    // process the R0 for discrepancies
    //
    // split the rule string onto constituting tokens
    //
    String ruleStr = grammarRules.get(0).getRuleString();
    StringBuffer newRuleString = new StringBuffer();
    String[] tokens = ruleStr.split("\\s+");
    for (String t : tokens) {
      if (t.startsWith("R")) {
        Integer rId = Integer.valueOf(t.substring(1));
        if (usedRules.contains(rId)) {
          newRuleString.append(t).append(" ");
        }
        // else {
        // System.err.println("removed rule " + rId + " from R0");
        // }
      }
    }
    newRuleString.delete(newRuleString.length() - 1, newRuleString.length());
    GrammarRuleRecord newR0 = new GrammarRuleRecord();
    newR0.setRuleNumber(0);
    newR0.setRuleString(newRuleString.toString());

    return prunedRules;

  }

  /**
   * Computes the size of a pruned grammar.
   * 
   * @param ts the input timeseries.
   * @param rules the grammar rules.
   * @param saxData the original SAX data.
   * @param paaSize the SAX transform word size.
   * 
   * @return the grammar size.
   */
  public static Integer computePrunedGrammarSize(double[] ts, GrammarRules rules,
      SAXRecords saxData, Integer paaSize) {

    // res is the final grammar's size
    //
    int res = 0;

    HashSet<Integer> existingRules = new HashSet<Integer>();
    for (GrammarRuleRecord r : rules) {
      existingRules.add(r.getRuleNumber());
    }

    // first we compute the size needed for encoding of rules
    //
    for (GrammarRuleRecord r : rules) {

      int ruleSize = 0;

      if (0 == r.getRuleNumber()) {
        // split the rule string onto constituting tokens
        //
        String ruleStr = r.getRuleString();
        String[] tokens = ruleStr.split("\\s+");
        for (String t : tokens) {
          if (t.startsWith("R")) {
            // it is other rule, so we use a number --> 2 bytes
            // and pointer on its time-series occurrence
            ruleSize = ruleSize + 2 + 2;
          }
          else {
            ruleSize = ruleSize + paaSize + 2;
          }
        }
      }
      else {
        ruleSize = r.getExpandedRuleString().replaceAll("\\s+", "").length();
        String ruleStr = r.getRuleString();
        String[] tokens = ruleStr.split("\\s+");
        for (String t : tokens) {
          if (t.startsWith("R") && existingRules.contains(Integer.valueOf(t.substring(1)))) {
            int expRSize = rules.get(Integer.valueOf(t.substring(1))).getExpandedRuleString()
                .replaceAll("\\s", "").length();
            ruleSize = ruleSize - expRSize + 2;
          }
        }
        ruleSize = ruleSize + r.getOccurrences().size() * 2;
      }

      // the increment is computed as the size in bytes which is the sum of:
      // - the expanded rule string (a letter == byte)
      // - the number of occurrences * 2 (each occurrence index == a word)
      // it is safe to skip a space since a word size is fixed
      //
      // res = res + r.getExpandedRuleString().replaceAll("\\s", "").length()
      // + r.getOccurrences().size() * 2;
      res = res + ruleSize;
    }

    // first we compute the cover by rules
    //
    // boolean[] range = new boolean[ts.length];
    // for (GrammarRuleRecord r : rules) {
    // if (0 == r.getRuleNumber()) {
    // continue;
    // }
    // range = updateRanges(range, r.getRuleIntervals());
    // }

    // if happens that not the whole time series is covered, we add the space needed to encode the
    // gaps
    // each uncovered point corresponds to a word of length PAA and an index
    //
    // if (!(isCovered(range))) {
    //
    // for (int i = 0; i < range.length; i++) {
    // if (false == range[i] && (null != saxData.getByIndex(i))) {
    // // each uncovered by a rule position is actually an individual PAA word
    // // and a position index
    // //
    // res = res + paaSize + 2;
    // }
    // }
    // }

    return res;
  }

  /**
   * Computes the size of a normal, i.e. unpruned grammar.
   * 
   * @param ts the input timeseries.
   * @param rules the grammar rules.
   * @param saxData the original SAX data.
   * @param paaSize the SAX transform word size.
   * 
   * @return the grammar size.
   */
  public static Integer computeValidGrammarSize(double[] ts, GrammarRules rules, SAXRecords saxData,
      Integer paaSize) {

    // res is the final grammar's size
    //
    int res = 0;

    // first we compute the size needed for encoding of rules
    //
    for (GrammarRuleRecord r : rules) {

      int ruleSize = 0;

      // skip the rule zero
      //
      if (0 == r.getRuleNumber()) {
        // split the rule string onto constituting tokens
        //
        String ruleStr = r.getRuleString();
        String[] tokens = ruleStr.split("\\s+");
        for (String t : tokens) {
          if (t.startsWith("R")) {
            // it is other rule, so we use a number --> 2 bytes
            // and pointer on its time-series occurrence
            ruleSize = ruleSize + 2 + 2;
          }
          else {
            ruleSize = ruleSize + paaSize + 2;
          }
        }

      }
      else {
        // split the rule string onto constituting tokens
        //
        String ruleStr = r.getRuleString();
        String[] tokens = ruleStr.split("\\s+");
        for (String t : tokens) {
          if (t.startsWith("R")) {
            // it is other rule, so we use a number --> 2 bytes
            ruleSize = ruleSize + 2;
          }
          else {
            ruleSize = ruleSize + paaSize;
          }
        }
        ruleSize = ruleSize + r.getOccurrences().size() * 2;
      }

      res = res + ruleSize;
    }

    // first we compute the cover by rules
    //
    // boolean[] range = new boolean[ts.length];
    // for (GrammarRuleRecord r : rules) {
    // if (0 == r.getRuleNumber()) {
    // continue;
    // }
    // range = updateRanges(range, r.getRuleIntervals());
    // }

    // if happens that not the whole time series is covered, we add the space needed to encode the
    // gaps
    // each uncovered point corresponds to a word of length PAA and an index
    //
    // if (!(isCovered(range))) {
    //
    // for (int i = 0; i < range.length; i++) {
    // if (false == range[i] && (null != saxData.getByIndex(i))) {
    // // each uncovered by a rule position is actually an individual PAA word
    // // and a position index
    // //
    // res = res + paaSize;
    // }
    // }
    // }

    return res;

  }

  /**
   * Checks if the cover is complete.
   * 
   * @param cover the cover.
   * @param intervals set of rule intervals.
   * @return true if the set complete.
   */
  public static boolean isCompletlyCovered(ArrayList<RuleInterval> cover,
      ArrayList<RuleInterval> intervals) {

    // first we build an array of intervals
    //
    int min = intervals.get(0).getStartPos();
    int max = intervals.get(0).getEndPos();
    for (RuleInterval i : intervals) {
      if (i.getStartPos() < min) {
        min = i.getStartPos();
      }
      if (i.getEndPos() > max) {
        max = i.getEndPos();
      }
    }
    boolean[] intervalsRange = new boolean[max - min];
    for (RuleInterval i : intervals) {
      for (int j = i.getStartPos(); j < i.getEndPos(); j++) {
        intervalsRange[j - min] = true;
      }
    }

    // so now here we have the range where true value correspond to the ranges belonging to
    // "intervals", which we effectively checking for being covered by cover
    //
    // true means uncovered
    //

    for (RuleInterval i : cover) {

      for (int j = i.getStartPos(); j < i.getEndPos(); j++) {

        if (j < min || j >= max) {
          continue;
        }
        else {
          intervalsRange[j - min] = false;
        }

      }

    }

    for (boolean b : intervalsRange) {
      if (true == b) {
        return false;
      }
    }

    return true;
  }

  /**
   * Updating the coverage ranges.
   * 
   * @param range the global range array.
   * @param ruleIntervals The intervals used for this update.
   * @return an updated array.
   */
  public static boolean[] updateRanges(boolean[] range, ArrayList<RuleInterval> ruleIntervals) {
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

  /**
   * Updating the coverage ranges.
   * 
   * @param range the global range array.
   * @param grammar The grammar (i.e. set of rules) used for this update.
   * 
   * @return an updated array.
   */
  public static boolean[] updateRanges(boolean[] range, GrammarRules grammar) {
    boolean[] res = Arrays.copyOf(range, range.length);
    for (GrammarRuleRecord r : grammar) {
      if (0 == r.getRuleNumber()) {
        continue;
      }
      res = updateRanges(res, r.getRuleIntervals());
    }
    return res;
  }

  /**
   * Computes the delta value for the suggested rule candidate.
   * 
   * @param range the range we compute the cover delta for.
   * @param rule the grammatical rule candidate.
   * 
   * @return the delta value.
   */
  public static double getCoverDelta(boolean[] range, GrammarRuleRecord rule) {

    // counts which uncovered points shall be covered
    int new_cover = 0;

    // counts overlaps with previously covered ranges
    int overlapping_cover = 0;

    // perform the sum computation
    for (RuleInterval i : rule.getRuleIntervals()) {
      int start = i.getStartPos();
      int end = i.getEndPos();
      for (int j = start; j <= end; j++) {
        if (range[j]) {
          overlapping_cover++;
        }
        else {
          new_cover++;
        }
      }
    }

    // if covers nothing, return 0
    if (0 == new_cover) {
      return 0.0;
    }

    // if zero overlap, return full weighted cover
    if (0 == overlapping_cover) {
      return (double) new_cover
          / (double) (rule.getExpandedRuleString().length() + rule.getRuleIntervals().size());
    }

    // else divide newly covered points amount by the sum of the rule string length and occurrence
    // (i.e. encoding size)
    return ((double) new_cover / (double) (new_cover + overlapping_cover))
        / (double) (rule.getExpandedRuleString().length() + rule.getRuleIntervals().size());
  }

  /**
   * Compute the covered percentage.
   * 
   * @param cover the cover array.
   * @return coverage percentage.
   */
  public static double computeCover(boolean[] cover) {
    int covered = 0;
    for (boolean i : cover) {
      if (i) {
        covered++;
      }
    }
    return (double) covered / (double) cover.length;
  }

  /**
   * Checks if the range is completely covered.
   * 
   * @param range the range.
   * @return true if covered.
   */
  public static boolean isCovered(boolean[] range) {
    for (boolean i : range) {
      if (false == i) {
        return false;
      }
    }
    return true;
  }

  /**
   * Searches for empty (i.e. uncovered) ranges.
   * 
   * @param range the whole range to analyze.
   * 
   * @return true if uncovered ranges exist.
   */
  public static boolean hasEmptyRanges(boolean[] range) {
    //
    // the visual debugging
    //
    // StringBuffer sb = new StringBuffer();
    // boolean inUncovered = false;
    // int start = 0;
    // for (int i = 0; i < range.length; i++) {
    // if (false == range[i] && false == inUncovered) {
    // start = i;
    // inUncovered = true;
    // }
    // if (true == range[i] && true == inUncovered) {
    // sb.append("[" + start + ", " + i + "], ");
    // inUncovered = false;
    // }
    // }
    // if (inUncovered) {
    // sb.append("[" + start + ", " + range.length + "], ");
    // }
    // System.out.println(sb);
    //
    //
    for (boolean p : range) {
      if (false == p) {
        return true;
      }
    }
    return false;
  }

}
