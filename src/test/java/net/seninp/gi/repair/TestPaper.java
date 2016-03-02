package net.seninp.gi.repair;

import org.junit.Test;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.repair.RePairFactory;
import net.seninp.gi.repair.RePairGrammar;
import net.seninp.gi.rpr.NewRepair;

public class TestPaper {

  private static final String TEST_STRING = "abc abc cba cba bac XXX abc abc cba cba bac";

  private static final String TEST_R0 = "R4 XXX R4";

  @Test
  public void testGrammarInference() {

    RePairGrammar grammar = NewRepair.parse(TEST_STRING);
    GrammarRules grammarRules = grammar.toGrammarRulesData();
    System.out.println(grammarRules);

    // assertTrue(grammar.getRules().get("R0"));

    RePairGrammar grammar2 = RePairFactory.buildGrammar(TEST_STRING);
    GrammarRules grammarRules2 = grammar2.toGrammarRulesData();
    System.out.println(grammarRules2);

  }

}
