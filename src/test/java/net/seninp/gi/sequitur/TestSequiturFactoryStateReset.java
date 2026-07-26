package net.seninp.gi.sequitur;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TestSequiturFactoryStateReset {

  @Test
  public void runSequiturRunsAreIsolated() throws Exception {
    SAXRule first = SequiturFactory.runSequitur("a b c d b c");
    assertTrue("first run should infer rules", first.toGrammarRulesData().size() > 1);

    SAXRule second = SequiturFactory.runSequitur("x");
    assertEquals(1, second.toGrammarRulesData().size());
    assertEquals("x ", second.toGrammarRulesData().get(0).getRuleString());

    // the first grammar must be untouched by the second run (with the old static state a
    // later run clobbered earlier results — this is what the pre-refactor version of this
    // test pinned via SAXRule.arrRuleRecords)
    assertEquals("earlier grammar must survive later runs", 2,
        first.toGrammarRulesData().size());
    assertEquals("a R1 d R1 ", first.toGrammarRulesData().get(0).getRuleString());
  }

  @Test
  public void interleavedGrammarsDoNotInterfere() throws Exception {
    // two inferences built token-by-token, interleaved — impossible with the old static state
    SAXRule left = new SAXRule();
    SAXRule right = new SAXRule();

    String[] leftTokens = "a b c d b c".split("\\s+");
    String[] rightTokens = "x y x y".split("\\s+");

    int li = 0;
    int ri = 0;
    while (li < leftTokens.length || ri < rightTokens.length) {
      if (li < leftTokens.length) {
        left.last().insertAfter(new SAXTerminal(leftTokens[li], li));
        left.last().p.check();
        li++;
      }
      if (ri < rightTokens.length) {
        right.last().insertAfter(new SAXTerminal(rightTokens[ri], ri));
        right.last().p.check();
        ri++;
      }
    }

    assertEquals("a R1 d R1 ", left.toGrammarRulesData().get(0).getRuleString());
    assertEquals("R1 R1 ", right.toGrammarRulesData().get(0).getRuleString());
    assertEquals("b c ", left.toGrammarRulesData().get(1).getRuleString());
    assertEquals("x y ", right.toGrammarRulesData().get(1).getRuleString());
  }

}
