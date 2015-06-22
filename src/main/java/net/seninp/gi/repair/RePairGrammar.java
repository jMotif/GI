package net.seninp.gi.repair;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;
import net.seninp.gi.GrammarRuleRecord;
import net.seninp.gi.GrammarRules;
import net.seninp.gi.RuleInterval;
import net.seninp.jmotif.sax.datastructures.SAXRecords;

/**
 * A repair grammar container.
 * 
 * @author psenin
 * 
 */
public class RePairGrammar {

  /** Common prefix. */
  private static final char THE_R = 'R';

  /** The spacer. */
  private static final char SPACE = ' ';

  protected AtomicInteger numRules;

  protected Hashtable<Integer, RePairRule> theRules;

  protected String r0String;
  protected String r0ExpandedString;

  public RePairGrammar() {
    super();
    this.numRules = new AtomicInteger(0);
    this.theRules = new Hashtable<Integer, RePairRule>();

  }

  /**
   * Get all the rules as the map.
   * 
   * @return all the rules.
   */
  public Hashtable<Integer, RePairRule> getRules() {
    return theRules;
  }

  /**
   * Global method: iterates over all rules expanding them.
   */
  public void expandRules() {

    // iterate over all SAX containers
    for (int currentPositionIndex = 1; currentPositionIndex < this.grammar.theRules.size(); currentPositionIndex++) {

      RePairRule rr = this.grammar.theRules.get(currentPositionIndex);
      String resultString = rr.toRuleString();

      int currentSearchStart = resultString.indexOf(THE_R);
      while (currentSearchStart >= 0) {

        int spaceIdx = resultString.indexOf(SPACE, currentSearchStart);

        String ruleName = resultString.substring(currentSearchStart, spaceIdx + 1);
        Integer ruleId = Integer.valueOf(ruleName.substring(1, ruleName.length() - 1));

        RePairRule rule = this.grammar.theRules.get(ruleId);
        if (rule != null) {
          if (rule.expandedRuleString.charAt(rule.expandedRuleString.length() - 1) == ' ') {
            resultString = resultString.replaceAll(ruleName, rule.expandedRuleString);
          }
          else {
            resultString = resultString.replaceAll(ruleName, rule.expandedRuleString + SPACE);
          }
        }

        currentSearchStart = resultString.indexOf("R", spaceIdx);
      }

      rr.setExpandedRule(resultString.trim());

    }

    // and the r0, String is immutable in Java
    //
    String resultString = this.grammar.r0String;

    int currentSearchStart = resultString.indexOf(THE_R);
    while (currentSearchStart >= 0) {
      int spaceIdx = resultString.indexOf(SPACE, currentSearchStart);
      String ruleName = resultString.substring(currentSearchStart, spaceIdx + 1);
      Integer ruleId = Integer.valueOf(ruleName.substring(1, ruleName.length() - 1));
      RePairRule rule = this.grammar.theRules.get(ruleId);
      if (rule != null) {
        if (rule.expandedRuleString.charAt(rule.expandedRuleString.length() - 1) == ' ') {
          resultString = resultString.replaceAll(ruleName, rule.expandedRuleString);
        }
        else {
          resultString = resultString.replaceAll(ruleName, rule.expandedRuleString + SPACE);
        }
      }
      currentSearchStart = resultString.indexOf("R", spaceIdx);
    }
    this.grammar.r0ExpandedString = resultString;

  }

  public static String toGrammarRules() {
    StringBuffer sb = new StringBuffer();
    System.out.println("R0 -> " + r0String);
    for (int i = 1; i < theRules.size(); i++) {
      RePairRule r = theRules.get(i);
      sb.append(THE_R).append(r.ruleNumber).append(" -> ").append(r.toRuleString()).append(" : ")
          .append(r.expandedRuleString).append(", ").append(r.occurrences).append("\n");
    }
    return sb.toString();
  }

  public GrammarRules toGrammarRulesData() {
    private static int mean(ArrayList<RuleInterval> arrayList) {
      if (null == arrayList || arrayList.isEmpty()) {
        return 0;
      }
      int res = 0;
      int count = 0;
      for (RuleInterval ri : arrayList) {
        res = res + (ri.getEndPos() - ri.getStartPos());
        count++;
      }
      return res / count;
    }

    private int[] getLengths() {
      if (this.ruleIntervals.isEmpty()) {
        return new int[1];
      }
      int[] res = new int[this.ruleIntervals.size()];
      int count = 0;
      for (RuleInterval ri : this.ruleIntervals) {
        res[count] = ri.getEndPos() - ri.getStartPos();
        count++;
      }
      return res;
    }

    private static int countSpaces(String str) {
      if (null == str) {
        return -1;
      }
      int counter = 0;
      for (int i = 0; i < str.length(); i++) {
        if (str.charAt(i) == ' ') {
          counter++;
        }
      }
      return counter;
    }

    GrammarRules res = new GrammarRules();

    GrammarRuleRecord r0 = new GrammarRuleRecord();
    r0.setRuleNumber(0);
    r0.setRuleString(theRules.get(0).toRuleString());
    r0.setExpandedRuleString(theRules.get(0).expandedRuleString);
    r0.setOccurrences(new int[1]);
    res.addRule(r0);

    for (RePairRule rule : theRules.values()) {

      GrammarRuleRecord rec = new GrammarRuleRecord();

      rec.setRuleNumber(rule.ruleNumber);
      rec.setRuleString(rule.toRuleString());
      rec.setExpandedRuleString(rule.expandedRuleString);
      rec.setRuleYield(countSpaces(rule.expandedRuleString));
      rec.setOccurrences(rule.getPositions());
      rec.setRuleIntervals(rule.getRuleIntervals());
      rec.setRuleLevel(rule.getLevel());
      rec.setMinMaxLength(rule.getLengths());
      rec.setMeanLength(mean(rule.getRuleIntervals()));

      res.addRule(rec);
    }

    return res;
  }
  
  /**
   * Builds a table of intervals corresponding to the grammar rules.
   * 
   * @param records the records to build intervals for.
   * @param originalTimeSeries the timeseries.
   * @param slidingWindowSize the sliding window size.
   */
  public void buildIntervals(SAXRecords records, double[] originalTimeSeries, int slidingWindowSize) {
    records.buildIndex();
    for (int currentPositionIndex = 1; currentPositionIndex < this.grammar.theRules.size(); currentPositionIndex++) {
      RePairRule rr = this.grammar.theRules.get(currentPositionIndex);
      // System.out.println("R" + rr.ruleNumber + ", " + rr.toRuleString() + ", "
      // + rr.expandedRuleString);
      String[] split = rr.expandedRuleString.split(" ");
      for (int pos : rr.getOccurrences()) {
        Integer p2 = records.mapStringIndexToTSPosition(pos + split.length - 1);
        if (null == p2) {
          rr.ruleIntervals.add(new RuleInterval(records.mapStringIndexToTSPosition(pos),
              originalTimeSeries.length));
        }
        else {
          rr.ruleIntervals.add(new RuleInterval(records.mapStringIndexToTSPosition(pos), records
              .mapStringIndexToTSPosition(pos + split.length - 1) + slidingWindowSize));
        }
      }
    }
  }

  public ArrayList<RuleInterval> getRuleIntervals() {
    return this.ruleIntervals;
  }

  private static int mean(ArrayList<RuleInterval> arrayList) {
    if (null == arrayList || arrayList.isEmpty()) {
      return 0;
    }
    int res = 0;
    int count = 0;
    for (RuleInterval ri : arrayList) {
      res = res + (ri.getEndPos() - ri.getStartPos());
      count++;
    }
    return res / count;
  }

  private int[] getLengths() {
    if (this.ruleIntervals.isEmpty()) {
      return new int[1];
    }
    int[] res = new int[this.ruleIntervals.size()];
    int count = 0;
    for (RuleInterval ri : this.ruleIntervals) {
      res[count] = ri.getEndPos() - ri.getStartPos();
      count++;
    }
    return res;
  }

  private static int countSpaces(String str) {
    if (null == str) {
      return -1;
    }
    int counter = 0;
    for (int i = 0; i < str.length(); i++) {
      if (str.charAt(i) == ' ') {
        counter++;
      }
    }
    return counter;
  }

}
