package net.seninp.gi.sequitur;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;
import net.seninp.gi.logic.GrammarRuleRecord;
import net.seninp.gi.logic.GrammarRules;

/**
 * Proves that Sequitur inferences are isolated per {@link SequiturGrammar} and can run
 * concurrently — the capability the de-static refactor exists for (parallel parameter sweeps).
 * With the old JVM-global state this test failed with interleaved grammars.
 */
public class TestSequiturConcurrency {

  private static final String[] INPUTS = { "a b c d b c a b c d b c",
      "a b a b c a b c d a b c d e a b c d e f", "x y z x y w x y", "a a a a",
      "dacb bbbd bbcb bdbb cbbc accb ccbc dbba cbbc bbdb bcbb dbbc bbcb adcc",
      "p q r s p q r s p q r s" };

  @Test
  public void concurrentRunsMatchSequentialRuns() throws Exception {

    // sequential reference dumps, one per input
    List<String> expected = new ArrayList<String>();
    for (String input : INPUTS) {
      expected.add(dump(SequiturFactory.runSequitur(input).toGrammarRulesData()));
    }

    // now the same inferences, many at once, several rounds per input
    final int rounds = 8;
    ExecutorService pool = Executors.newFixedThreadPool(INPUTS.length);
    try {
      List<Future<String>> futures = new ArrayList<Future<String>>();
      for (int round = 0; round < rounds; round++) {
        for (final String input : INPUTS) {
          futures.add(pool.submit(new Callable<String>() {
            public String call() throws Exception {
              return dump(SequiturFactory.runSequitur(input).toGrammarRulesData());
            }
          }));
        }
      }
      int i = 0;
      for (Future<String> f : futures) {
        assertEquals("concurrent grammar for input #" + (i % INPUTS.length)
            + " must match the sequential one", expected.get(i % INPUTS.length), f.get());
        i++;
      }
    }
    finally {
      pool.shutdownNow();
    }
  }

  private static String dump(GrammarRules rules) {
    StringBuilder sb = new StringBuilder();
    for (GrammarRuleRecord rec : rules) {
      sb.append('R').append(rec.ruleNumber()).append(" -> ").append(rec.getRuleString())
          .append(" exp ").append(rec.getExpandedRuleString()).append(" occ ")
          .append(rec.getOccurrences()).append(';');
    }
    return sb.toString();
  }
}
