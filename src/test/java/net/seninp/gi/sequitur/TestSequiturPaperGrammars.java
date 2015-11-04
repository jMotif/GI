package net.seninp.gi.sequitur;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import net.seninp.gi.logic.GrammarRuleRecord;
import net.seninp.gi.logic.GrammarRules;
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

      final double[] originalTimeSeries = { 1., 0., 1., 0., 1., 0., 1., 0., 1., 0., 1. };
      SAXRecords saxData = new SAXRecords();
      int idx = 0;
      for (String s : TEST1_STRING.split("\\s+")) {
        saxData.add(s.toCharArray(), idx);
        idx += 2;
      }
      SAXRule grammar = SequiturFactory.runSequitur(saxData.getSAXString(" "));
      GrammarRules rules = grammar.toGrammarRulesData();
      SequiturFactory.updateRuleIntervals(rules, saxData, true, originalTimeSeries, 1, 1);

      for (GrammarRuleRecord rec : grammar.getRuleRecords()) {
        System.out.println(rec.getRuleIntervals());
      }

    }
    catch (Exception e) {
      fail("Exception shouldnt be thrown: " + StackTrace.toString(e));
    }

    try {

      final double[] originalTimeSeries = new double[6*5];
      
      SAXRecords saxData = new SAXRecords();
      int idx = 0;
      for (String s : TEST1_STRING.split("\\s+")) {
        saxData.add(s.toCharArray(), idx);
        idx += 5;
      }
      
      SAXRule grammar = SequiturFactory.runSequitur(saxData.getSAXString(" "));
      GrammarRules rules = grammar.toGrammarRulesData();
      SequiturFactory.updateRuleIntervals(rules, saxData, true, originalTimeSeries, 1, 1);

      for (GrammarRuleRecord rec : grammar.getRuleRecords()) {
        System.out.println(rec.getRuleIntervals());
      }

    }
    catch (Exception e) {
      fail("Exception shouldnt be thrown: " + StackTrace.toString(e));
    }

  }
}
