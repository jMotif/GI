package net.seninp.gi.repair;

import com.gs.collections.api.iterator.MutableIntIterator;
import com.gs.collections.impl.set.mutable.primitive.IntHashSet;
import net.seninp.gi.GrammarRuleRecord;
import net.seninp.gi.GrammarRules;
import net.seninp.gi.RuleInterval;
import net.seninp.jmotif.sax.datastructures.SAXRecords;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

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

  /**
   * Constructor.
   */
  public RePairGrammar() {
    super();
    // R0 is reserved
    this.numRules = new AtomicInteger(1);
    this.theRules = new Hashtable<Integer, RePairRule>();
  }

  public void setR0String(String str) {
    this.r0String = str;
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
    for (int currentPositionIndex = 1; currentPositionIndex <= this.theRules.size(); currentPositionIndex++) {

      RePairRule rr = this.theRules.get(currentPositionIndex);
      String resultString = rr.toRuleString();

      int currentSearchStart = resultString.indexOf(THE_R);
      while (currentSearchStart >= 0) {

        int spaceIdx = resultString.indexOf(SPACE, currentSearchStart);

        String ruleName = resultString.substring(currentSearchStart, spaceIdx + 1);
        Integer ruleId = Integer.valueOf(ruleName.substring(1, ruleName.length() - 1));

        RePairRule rule = this.theRules.get(ruleId);
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
    String resultString = this.r0String;

    int currentSearchStart = resultString.indexOf(THE_R);
    while (currentSearchStart >= 0) {
      int spaceIdx = resultString.indexOf(SPACE, currentSearchStart);
      String ruleName = resultString.substring(currentSearchStart, spaceIdx + 1);
      Integer ruleId = Integer.valueOf(ruleName.substring(1, ruleName.length() - 1));
      RePairRule rule = this.theRules.get(ruleId);
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
    this.r0ExpandedString = resultString;

  }

  /**
   * Prints out the grammar as text.
   * 
   * @return textual representation of the grammar.
   */
  public String toGrammarRules() {
    StringBuffer sb = new StringBuffer();
    System.out.println("R0 -> " + this.r0String);
    for (int i = 1; i <= this.theRules.size(); i++) {
      RePairRule r = this.theRules.get(i);
      sb.append(THE_R).append(r.ruleNumber).append(" -> ").append(r.toRuleString()).append(" : ")
          .append(r.expandedRuleString).append(", ").append(r.occurrences).append("\n");
    }
    return sb.toString();
  }

  /**
   * Build a grammarviz-"portable" grammar object.
   * 
   * @return a grammarviz-"portable" grammar object.
   */
  public GrammarRules toGrammarRulesData() {

    GrammarRules res = new GrammarRules();

    GrammarRuleRecord r0 = new GrammarRuleRecord();
    r0.setRuleNumber(0);
    r0.setRuleString(this.r0String);
    r0.setExpandedRuleString(this.r0ExpandedString);
    r0.setOccurrences(new IntHashSet());
    res.addRule(r0);

    for (RePairRule rule : theRules.values()) {

      GrammarRuleRecord rec = new GrammarRuleRecord();

      rec.setRuleNumber(rule.ruleNumber);
      rec.setRuleString(rule.toRuleString());
      rec.setExpandedRuleString(rule.expandedRuleString);
      rec.setRuleYield(countSpaces(rule.expandedRuleString));
      rec.setOccurrences(rule.getOccurrences());
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
    for (int currentPositionIndex = 1; currentPositionIndex < this.theRules.size(); currentPositionIndex++) {
      RePairRule rr = this.theRules.get(currentPositionIndex);
      // System.out.println("R" + rr.ruleNumber + ", " + rr.toRuleString() + ", "
      // + rr.expandedRuleString);
      String[] split = rr.expandedRuleString.split(" ");

      MutableIntIterator ii = rr.getOccurrences().intIterator();
      while (ii.hasNext()) {
        int pos =  ii.next();

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

  /**
   * Count spaces in the string.
   * 
   * @param str the string
   * @return the num of encountered spaces.
   */
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
