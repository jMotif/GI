package net.seninp.gi.sequitur;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TestSequiturFactoryStateReset {

  @Test
  public void runSequiturResetsRuleRecordsBetweenRuns() throws Exception {
    SAXRule first = SequiturFactory.runSequitur("a b c d b c");
    first.toGrammarRulesData();
    assertTrue("first run should infer rules", SAXRule.arrRuleRecords.size() > 1);

    SAXRule second = SequiturFactory.runSequitur("x");
    assertEquals("stale rule records must not leak into the next run", 0,
        SAXRule.arrRuleRecords.size());

    assertEquals(1, second.toGrammarRulesData().size());
    assertEquals("x ", second.toGrammarRulesData().get(0).getRuleString());
  }

}
