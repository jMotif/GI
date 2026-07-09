package net.seninp.gi.repair;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import net.seninp.gi.logic.GrammarRules;

public class TestRepairPaperGrammar {

  // Lowercase xxx matches the jmotif-R and saxpy reference tests.
  private static final String TEST_STRING = "abc abc cba cba bac xxx abc abc cba cba bac";

  private static final String TEST_R0 = "R4 xxx R4";

  @Test
  public void testGrammarInference() {

    RePairGrammar grammar = NewRepair.parse(TEST_STRING);
    GrammarRules grammarRules = grammar.toGrammarRulesData();
    assertEquals(TEST_R0, grammarRules.getRuleRecord(0).getRuleString().trim());
  }

}
