package net.seninp.gi.repair;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import net.seninp.gi.logic.GrammarRules;

public class TestRepairPaperGrammar {

  private static final String TEST_STRING = "abc abc cba cba bac XXX abc abc cba cba bac";

  private static final String TEST_R0 = "R4 XXX R4";

  @Test
  public void testGrammarInference() {

    RePairGrammar grammar = NewRepair.parse(TEST_STRING);
    GrammarRules grammarRules = grammar.toGrammarRulesData();
    System.out.println(
        "testing paper's grammar for the input string \"" + TEST_STRING + "\"\n" + grammarRules);
    assertTrue("confirming the grammar in paper",
        grammarRules.getRuleRecord(0).getRuleString().trim().equalsIgnoreCase(TEST_R0));

  }

}
