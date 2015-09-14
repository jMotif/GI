package net.seninp.gi.rulepruner;

import static org.junit.Assert.*;
import java.util.ArrayList;
import org.junit.BeforeClass;
import org.junit.Test;
import net.seninp.gi.GrammarRuleRecord;
import net.seninp.gi.GrammarRules;
import net.seninp.gi.RuleInterval;
import net.seninp.jmotif.sax.datastructures.SAXRecords;

/**
 * test the paper's example.
 * 
 * @author psenin
 *
 */
public class TestRulePruner {

  private static double[] ts = { 1., 1., 1., 1., 1., 1., 1. };

  private static SAXRecords recs = new SAXRecords();

  private static GrammarRules grammar = new GrammarRules();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    {
      GrammarRuleRecord r2 = new GrammarRuleRecord();
      r2.setRuleNumber(2);

      r2.setExpandedRuleString("abc abc");
      r2.setRuleString("abc abc");

      ArrayList<RuleInterval> intervals2 = new ArrayList<RuleInterval>();
      intervals2.add(new RuleInterval(0, 1));
      intervals2.add(new RuleInterval(4, 5));
      r2.setRuleIntervals(intervals2);

      r2.setOccurrences(new int[] { 0, 4 });

      grammar.addRule(r2);
    }

    {
      GrammarRuleRecord r1 = new GrammarRuleRecord();
      r1.setRuleNumber(1);

      r1.setExpandedRuleString("abc abc cba");
      r1.setRuleString("R2 abc");

      ArrayList<RuleInterval> intervals1 = new ArrayList<RuleInterval>();
      intervals1.add(new RuleInterval(0, 2));
      intervals1.add(new RuleInterval(4, 6));
      r1.setRuleIntervals(intervals1);

      r1.setOccurrences(new int[] { 0, 4 });

      grammar.addRule(r1);
    }

    {
      GrammarRuleRecord r0 = new GrammarRuleRecord();
      r0.setRuleNumber(0);

      r0.setExpandedRuleString("abc abc cba xxx abc abc cba");
      r0.setRuleString("R1 abc R1");

      ArrayList<RuleInterval> intervals0 = new ArrayList<RuleInterval>();
      intervals0.add(new RuleInterval(0, 6));
      r0.setRuleIntervals(intervals0);

      grammar.addRule(r0);

      r0.setOccurrences(new int[] { 0 });

    }

    recs.add("abc".toCharArray(), 0);
    recs.add("abc".toCharArray(), 1);
    recs.add("cba".toCharArray(), 2);
    recs.add("XXX".toCharArray(), 3);
    recs.add("abc".toCharArray(), 4);
    recs.add("abc".toCharArray(), 5);
    recs.add("cba".toCharArray(), 6);

  }

  @Test
  public void test() {

    assertEquals(new Integer(32), RulePrunerFactory.computeValidGrammarSize(ts, grammar, recs, 3));

    GrammarRules prunedGrammar = RulePrunerFactory.performPruning(ts, grammar);

    assertEquals(new Integer(26),
        RulePrunerFactory.computePrunedGrammarSize(ts, prunedGrammar, recs, 3));
  }

}
