package net.seninp.gi.repair;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;
import net.seninp.gi.GrammarRuleRecord;
import net.seninp.gi.GrammarRules;
import net.seninp.gi.RuleInterval;
import net.seninp.jmotif.sax.datastructures.SAXRecords;

/**
 * The grammar rule.
 * 
 * @author psenin
 * 
 */
public class RePairRule {

  /** Common prefix. */
  private static final char THE_R = 'R';

  /** The spacer. */
  private static final char SPACE = ' ';

  /** The global rule enumerator counter. */
  protected static AtomicInteger numRules = new AtomicInteger(1);

  /** The global rules table. */
  protected static Hashtable<Integer, RePairRule> theRules = new Hashtable<Integer, RePairRule>();

  /** R0 is important, reserve a var for that. */
  protected static String r0String;
  protected static String r0ExpandedString;

  /** The current rule number. */
  protected int ruleNumber;
  protected String expandedRuleString;

  /** Both symbols, (i.e., pair). */
  protected RePairSymbol first;
  protected RePairSymbol second;
  protected int level;

  /** Occurrences. */
  protected ArrayList<Integer> occurrences;

  /** Which TS interval covered. */
  protected ArrayList<RuleInterval> ruleIntervals;

  /**
   * Constructor, assigns a rule ID using the global counter.
   */
  public RePairRule() {
    // assign a next number to this rule and increment the global counter
    this.ruleNumber = numRules.intValue();
    numRules.incrementAndGet();

    theRules.put(this.ruleNumber, this);

    this.occurrences = new ArrayList<Integer>();
    this.ruleIntervals = new ArrayList<RuleInterval>();
  }

  /**
   * First symbol setter.
   * 
   * @param symbol the symbol to set.
   */
  public void setFirst(RePairSymbol symbol) {
    this.first = symbol;
  }

  /**
   * Second symbol setter.
   * 
   * @param symbol the symbol to set.
   */
  public void setSecond(RePairSymbol symbol) {
    this.second = symbol;
  }

  /**
   * Rule ID getter.
   * 
   * @return the rule ID.
   */
  public int getId() {
    return this.ruleNumber;
  }

  public String toString() {
    return "R" + this.ruleNumber;
  }

  /**
   * Return the prefixed with R rule.
   * 
   * @return rule string.
   */
  public String toRuleString() {
    if (0 == this.ruleNumber) {
      return r0String;
    }
    return this.first.toString() + SPACE + this.second.toString() + SPACE;
  }

  /**
   * Adds a rule occurrence.
   * 
   * @param value the new value.
   */
  public void addOccurrence(int value) {
    if (!this.occurrences.contains(value)) {
      this.occurrences.add(value);
    }
  }

  /**
   * Gets occurrences.
   * 
   * @return all rule's occurrences.
   */
  public int[] getPositions() {
    int[] res = new int[this.occurrences.size()];
    for (int i = 0; i < this.occurrences.size(); i++) {
      res[i] = this.occurrences.get(i);
    }
    return res;
  }

  /**
   * Global method: iterates over all rules expanding them.
   */
  public static void expandRules() {
  
    // iterate over all SAX containers
    for (int currentPositionIndex = 1; currentPositionIndex < theRules.size(); currentPositionIndex++) {
  
      RePairRule rr = theRules.get(currentPositionIndex);
      String resultString = rr.toRuleString();
  
      int currentSearchStart = resultString.indexOf(THE_R);
      while (currentSearchStart >= 0) {
  
        int spaceIdx = resultString.indexOf(SPACE, currentSearchStart);
  
        String ruleName = resultString.substring(currentSearchStart, spaceIdx + 1);
        Integer ruleId = Integer.valueOf(ruleName.substring(1, ruleName.length() - 1));
  
        RePairRule rule = theRules.get(ruleId);
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
    String resultString = r0String;
  
    int currentSearchStart = resultString.indexOf(THE_R);
    while (currentSearchStart >= 0) {
      int spaceIdx = resultString.indexOf(SPACE, currentSearchStart);
      String ruleName = resultString.substring(currentSearchStart, spaceIdx + 1);
      Integer ruleId = Integer.valueOf(ruleName.substring(1, ruleName.length() - 1));
      RePairRule rule = theRules.get(ruleId);
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
    r0ExpandedString = resultString;
  
  }

  /**
   * Recovers the input string using the grammar.
   * 
   * @return the rebuilt input string.
   */
  public static String recoverString() {
    return r0ExpandedString;
  }

  /**
   * Builds a table of intervals corresponding to the grammar rules.
   * 
   * @param records the records to build intervals for.
   * @param originalTimeSeries the timeseries.
   * @param slidingWindowSize the sliding window size.
   */
  public static void buildIntervals(SAXRecords records, double[] originalTimeSeries,
      int slidingWindowSize) {
    records.buildIndex();
    for (int currentPositionIndex = 1; currentPositionIndex < theRules.size(); currentPositionIndex++) {
      RePairRule rr = theRules.get(currentPositionIndex);
      // System.out.println("R" + rr.ruleNumber + ", " + rr.toRuleString() + ", "
      // + rr.expandedRuleString);
      String[] split = rr.expandedRuleString.split(" ");
      for (int pos : rr.getPositions()) {
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

  public static void setRuleString(String stringToDisplay) {
    r0String = stringToDisplay;
  }

  public static GrammarRules toGrammarRulesData() {
  
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

  public ArrayList<RuleInterval> getRuleIntervals() {
    return this.ruleIntervals;
  }

  public void assignLevel() {
    int lvl = Integer.MAX_VALUE;
    lvl = Math.min(first.getLevel() + 1, lvl);
    lvl = Math.min(second.getLevel() + 1, lvl);
    this.level = lvl;
  }

  public int getLevel() {
    return this.level;
  }

  /**
   * Get all the rules as the map.
   * 
   * @return all the rules.
   */
  public Hashtable<Integer, RePairRule> getRules() {
    return theRules;
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

  /**
   * Set the expanded rule string.
   * 
   * @param str the expanded rule value.
   * 
   */
  private void setExpandedRule(String str) {
    this.expandedRuleString = str;
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
