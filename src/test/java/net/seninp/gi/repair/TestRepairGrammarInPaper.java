package net.seninp.gi.repair;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TestRepairGrammarInPaper {

  private static final String TEST_STRING = "abc abc cba cba bac XXX abc abc cba cba bac";

  private static final String TEST_R0 = "R4 XXX R4";

  @Test
  public void test() throws Exception {

    RePairGrammar rg = RePairFactory.buildGrammar(TEST_STRING);
    assertNotNull("grammar exists", rg);

    assertTrue("assert proper grammar",
        TEST_R0.equals(rg.toGrammarRulesData().get(0).getRuleString().trim()));

    // System.out.println(rg.toGrammarRules() + "\n\n----\n\n");
    // SAXRule sr = SequiturFactory.runSequitur(TEST_STRING);
    // System.out.println(SAXRule.printRules());
    //
    // GrammarRuleRecord r0 = rg.toGrammarRulesData().get(0);
    //
    // assertTrue("assert proper grammar", TEST_R0.equals(r0.getRuleString().trim()));

  }

}
