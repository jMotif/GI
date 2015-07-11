package net.seninp.gi.repair;

import com.gs.collections.impl.set.mutable.primitive.IntHashSet;
import net.seninp.gi.RuleInterval;

import java.util.ArrayList;

/**
 * The grammar rule.
 * 
 * @author psenin
 * 
 */
public class RePairRule {

  /** The spacer. */
  private static final char SPACE = ' ';

  /** The global rule enumerator counter. */
  // protected static AtomicInteger numRules = new AtomicInteger(1);

  /** The global rules table. */
  // protected static Hashtable<Integer, RePairRule> theRules = new Hashtable<Integer,
  // RePairRule>();

  /** R0 is important, reserve a var for that. */
  // protected String r0String;
  // protected String r0ExpandedString;

  /** The current rule number. */
  protected int ruleNumber;
  protected String expandedRuleString;

  /** Both symbols, (i.e., pair). */
  protected RePairSymbol first;
  protected RePairSymbol second;
  protected int level;

  /** Occurrences. */
  protected IntHashSet occurrences;

  /** Which TS interval covered. */
  protected ArrayList<RuleInterval> ruleIntervals;

  /** A handler on the grammar this rule belongs to. */
  private RePairGrammar grammar;

  /**
   * Constructor, assigns a rule ID using the global counter.
   */
  public RePairRule(RePairGrammar rg, RePairSymbol first, RePairSymbol second) {

    this.grammar = rg;

    // assign a next number to this rule and increment the global counter
    this.ruleNumber = rg.numRules.intValue();
    rg.numRules.incrementAndGet();

    rg.theRules.put(this.ruleNumber, this);

    this.occurrences = new IntHashSet();
    this.ruleIntervals = new ArrayList<RuleInterval>();

    this.first = first; this.second = second;

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

  /**
   * Return the prefixed with R rule.
   * 
   * @return rule string.
   */
  public String toRuleString() {
    if (0 == this.ruleNumber) {
      return this.grammar.r0String;
    }
    return this.first.toString() + SPACE + this.second.toString() + SPACE;
  }

  /**
   * Set the expanded rule string.
   * 
   * @param str the expanded rule value.
   * 
   */
  protected void setExpandedRule(String str) {
    this.expandedRuleString = str;
  }

  /**
   * Return the prefixed with R rule.
   * 
   * @return rule string.
   */
  public String toExpandedRuleString() {
    return this.expandedRuleString;
  }

  /**
   * Adds a rule occurrence.
   * 
   * @param value the new value.
   */
  public void addOccurrence(int value) {

    this.occurrences.add(value);

  }

  /**
   * Gets occurrences.
   * 
   * @return all rule's occurrences.
   */
  public IntHashSet getOccurrences() {
    return occurrences;
//    int[] res = new int[this.occurrences.size()];
//    for (int i = 0; i < this.occurrences.size(); i++) {
//      res[i] = this.occurrences.get(i);
//    }
//    return res;
  }

  public String toString() {
    return "R" + this.ruleNumber;
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

  public ArrayList<RuleInterval> getRuleIntervals() {
    return this.ruleIntervals;
  }

  public int[] getLengths() {
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

}
