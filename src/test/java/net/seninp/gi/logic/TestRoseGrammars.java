package net.seninp.gi.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import net.seninp.gi.repair.NewRepair;
import net.seninp.gi.repair.RePairGrammar;
import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.util.StackTrace;

public class TestRoseGrammars {

  private static final String TEST_STRING = "a rose is a rose is a rose";

  @Test
  public void testSequitur() {
    try {
      SAXRule r = SequiturFactory.runSequitur(TEST_STRING);
      GrammarRules rules = r.toGrammarRulesData();
      System.out.println("testing SEQUITUR with the string \"" + TEST_STRING + "\":\n\n"
          + SAXRule.printRules() + "\n --end-- \n");

      assertEquals("test hierarchy", 3, rules.size());

    }
    catch (Exception e) {
      fail("Exception shouldnt be thrown: " + StackTrace.toString(e));
    }
  }

  @Test
  public void testRePair() {
    try {
      RePairGrammar grammar = NewRepair.parse(TEST_STRING);
      GrammarRules grammarRules = grammar.toGrammarRulesData();
      System.out.println("testing REPAIR with the string \"" + TEST_STRING + "\":\n\n"
          + grammarRules + "\n --end-- \n");

    }
    catch (Exception e) {
      fail("Exception shouldnt be thrown: " + StackTrace.toString(e));
    }

  }
}
