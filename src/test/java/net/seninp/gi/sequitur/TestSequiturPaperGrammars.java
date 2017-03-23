package net.seninp.gi.sequitur;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import org.junit.Test;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.logic.RuleInterval;
import net.seninp.gi.repair.RePairFactory;
import net.seninp.gi.repair.RePairGrammar;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import net.seninp.util.StackTrace;

public class TestSequiturPaperGrammars {

  private static final String TEST1_STRING = "a b c d b c";
  private static final String TEST1_R0 = "a R1 d R1";
  private static final String TEST1_R1 = "b c";

  private static final String TEST2_STRING = "a b c d b c a b c d b c";
  private static final String TEST2_R0 = "R1 R1";
  private static final String TEST2_R1 = "a R2 d R2";
  private static final String TEST2_R2 = "b c";

  private static final String TEST3_STRING = "a b a b c a b c d a b c d e a b c d e f";
  private static final String TEST3_R0 = "R1 R2 R3 R4 R4 f";
  private static final String TEST3_R4 = "R3 e";

  @Test
  public void test3() {
    try {
      SAXRule r = SequiturFactory.runSequitur(TEST3_STRING);
      GrammarRules rules = r.toGrammarRulesData();
      System.out.println(SAXRule.printRules() + "\n ---- \n");

      RePairGrammar rr = RePairFactory.buildGrammar(TEST3_STRING);
      System.out.println(rr.toGrammarRules());

      assertEquals("test hierarchy", 5, rules.size());

      assertTrue("test r0", TEST3_R0.equals(rules.get(0).getRuleString().trim()));

      assertTrue("test r1", TEST3_R4.equals(rules.get(4).getRuleString().trim()));
    }
    catch (Exception e) {
      fail("Exception shouldnt be thrown: " + StackTrace.toString(e));
    }
  }

  @Test
  public void test2() {
    try {
      SAXRule r = SequiturFactory.runSequitur(TEST2_STRING);
      GrammarRules rules = r.toGrammarRulesData();
      // System.out.println(SAXRule.printRules());

      @SuppressWarnings("unused")
      RePairGrammar rr = RePairFactory.buildGrammar(TEST2_STRING);
      // System.out.println(rr.toGrammarRules());

      assertTrue("test r0", TEST2_R0.equals(rules.get(0).getRuleString().trim()));
      assertTrue("test r1", TEST2_R1.equals(rules.get(1).getRuleString().trim()));
      assertTrue("test r1", TEST2_R2.equals(rules.get(2).getRuleString().trim()));
    }
    catch (Exception e) {
      fail("Exception shouldnt be thrown: " + StackTrace.toString(e));
    }
  }

  @Test
  public void test1() {
    try {
      SAXRule r = SequiturFactory.runSequitur(TEST1_STRING);
      GrammarRules rules = r.toGrammarRulesData();

      assertTrue("test r0", TEST1_R0.equals(rules.get(0).getRuleString().trim()));
      assertTrue("test r1", TEST1_R1.equals(rules.get(1).getRuleString().trim()));
    }
    catch (Exception e) {
      fail("Exception shouldnt be thrown: " + StackTrace.toString(e));
    }

  }

  @Test
  public void test1Full() {

    try {

      //
      // here is the string of the size 6: a b c d b c
      //
      // mapped on the 11 points time series
      //
      // a0, b2, c4, d6, b8, c10
      //
      // so each token occupies two positions except the last
      //
      // 0-1, 2-3, 4-5, 6-7, 8-9, 10-19
      //
      final double[] originalTimeSeries = new double[6 * 2 + 7];
      SAXRecords saxData = new SAXRecords();
      int idx = 0;
      for (String s : TEST1_STRING.split("\\s+")) {
        saxData.add(s.toCharArray(), idx);
        idx += 2;
      }

      SAXRule grammar = SequiturFactory.runSequitur(saxData.getSAXString(" "));
      GrammarRules rules = grammar.toGrammarRulesData();
      SequiturFactory.updateRuleIntervals(rules, saxData, true, originalTimeSeries, 1, 1);

      //
      // System.out.println(rules.toString());
      // for (GrammarRuleRecord rec : grammar.getRuleRecords()) {
      // System.out.println(rec + ", " + rec.getRuleIntervals());
      // }
      //
      // R0 -> a R1 d R1 , [[0-11]]
      // R1 -> b c , [[2-6], [8-11]]
      //
      ArrayList<RuleInterval> int0 = grammar.getRuleRecords().get(0).getRuleIntervals();
      assertEquals(1, int0.size());
      assertEquals(new RuleInterval(0, 19), int0.get(0));

      ArrayList<RuleInterval> int1 = grammar.getRuleRecords().get(1).getRuleIntervals();
      assertEquals(2, int1.size());
      assertEquals(new RuleInterval(2, 6), int1.get(0));
      assertEquals(new RuleInterval(8, 19), int1.get(1));

    }
    catch (Exception e) {
      fail("Exception shouldnt be thrown: " + StackTrace.toString(e));
    }

    try {

      //
      // here will be 6x5 = 30 points
      //
      // the string of the size 6: a b c d b c
      //
      // 0-4, 5-9, 10-14, 15-19, 20-24, 25-29

      final double[] originalTimeSeries = new double[6 * 5 - 1];

      SAXRecords saxData = new SAXRecords();
      int idx = 0;
      for (String s : TEST1_STRING.split("\\s+")) {
        saxData.add(s.toCharArray(), idx);
        idx += 5;
      }

      SAXRule grammar = SequiturFactory.runSequitur(saxData.getSAXString(" "));
      GrammarRules rules = grammar.toGrammarRulesData();
      SequiturFactory.updateRuleIntervals(rules, saxData, true, originalTimeSeries, 1, 1);

      ArrayList<RuleInterval> int0 = grammar.getRuleRecords().get(0).getRuleIntervals();
      assertEquals(1, int0.size());
      assertEquals(new RuleInterval(0, 29), int0.get(0));

      ArrayList<RuleInterval> int1 = grammar.getRuleRecords().get(1).getRuleIntervals();
      assertEquals(2, int1.size());
      assertEquals(new RuleInterval(5, 15), int1.get(0));
      assertEquals(new RuleInterval(20, 29), int1.get(1));

    }
    catch (Exception e) {
      fail("Exception shouldnt be thrown: " + StackTrace.toString(e));
    }

  }
}
