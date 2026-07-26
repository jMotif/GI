package net.seninp.gi.sequitur;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import org.junit.Test;
import net.seninp.gi.logic.GrammarRuleRecord;
import net.seninp.gi.logic.GrammarRules;

/**
 * Pins the observable behavior of the current static-state Sequitur implementation ahead of the
 * SequiturGrammar de-static refactor. Every value asserted here was harvested from the
 * implementation as of GI 2.0.2 — the refactor must keep all of them byte-identical.
 */
public class TestSequiturPinnedBehavior {

  /** Regression string from {@code jmotif-R/inst/test_data/bugs.R}, also used by RePair tests. */
  private static final String JMOTIF_R_BUGS_SAX_STRING = "dacb bbbd bbcb bdbb cbbc accb ccbc dbba cbbc bbdb bcbb dbbc bbcb adcc ccbc daba cbbc bbdb bcbb dbbc bbcb adcb bdac dcbb cbbd bbcb bdbb cbbc accb cdbb dbbb cbbd bccb bdab cbbc accb cdbb dbbb cbbd bbcb bdbb cbbc accc ccbb cbbd bbcb bdbb cbbc accb";

  // ---------------------------------------------------------------------------------------------
  // degenerate inputs
  // ---------------------------------------------------------------------------------------------

  @Test
  public void emptyInputYieldsEmptyR0() throws Exception {
    GrammarRules rules = SequiturFactory.runSequitur("").toGrammarRulesData();
    assertEquals(1, rules.size());
    assertEquals("", rules.get(0).getRuleString().trim());
  }

  @Test
  public void whitespaceOnlyInputYieldsEmptyR0() throws Exception {
    GrammarRules rules = SequiturFactory.runSequitur("   ").toGrammarRulesData();
    assertEquals(1, rules.size());
    assertEquals("", rules.get(0).getRuleString().trim());
  }

  @Test(expected = NullPointerException.class)
  public void nullInputThrowsNPE() throws Exception {
    // Pinned as-is: unlike RePair (empty grammar since 2.0.2), Sequitur throws on null.
    // If the refactor changes this deliberately, update this pin in the same commit.
    SequiturFactory.runSequitur(null);
  }

  @Test
  public void singleTokenYieldsR0Only() throws Exception {
    GrammarRules rules = SequiturFactory.runSequitur("x").toGrammarRulesData();
    assertEquals(1, rules.size());
    assertEquals("x", rules.get(0).getRuleString().trim());
    assertEquals("x", rules.get(0).getExpandedRuleString().trim());
  }

  // ---------------------------------------------------------------------------------------------
  // overlapping digrams — the classic Sequitur trap
  // ---------------------------------------------------------------------------------------------

  @Test
  public void overlappingDigramsInAAADoNotFormARule() throws Exception {
    // "a a" occurs twice in "a a a" but the occurrences overlap — no rule may form.
    GrammarRules rules = SequiturFactory.runSequitur("a a a").toGrammarRulesData();
    assertEquals(1, rules.size());
    assertEquals("a a a", rules.get(0).getRuleString().trim());
  }

  @Test
  public void nonOverlappingDigramsInAAAAFormOneRule() throws Exception {
    GrammarRules rules = SequiturFactory.runSequitur("a a a a").toGrammarRulesData();
    assertEquals(2, rules.size());
    assertEquals("R1 R1", rules.get(0).getRuleString().trim());
    assertEquals("a a", rules.get(1).getRuleString().trim());
    assertEquals("[0, 2]", rules.get(1).getOccurrences().toString());
  }

  // ---------------------------------------------------------------------------------------------
  // rule utility — a rule used once must be expanded away
  // ---------------------------------------------------------------------------------------------

  @Test
  public void ruleUtilityExpandsSingleUseRule() throws Exception {
    // While digesting "a b c a b c", the digram rule for "a b" forms first and is then
    // absorbed when "a b c" becomes repetitive; no single-use rule may survive.
    GrammarRules rules = SequiturFactory.runSequitur("a b c a b c").toGrammarRulesData();
    assertEquals(2, rules.size());
    assertEquals("R1 R1", rules.get(0).getRuleString().trim());
    assertEquals("a b c", rules.get(1).getRuleString().trim());
    assertEquals("[0, 3]", rules.get(1).getOccurrences().toString());
    for (GrammarRuleRecord rec : rules) {
      if (rec.ruleNumber() > 0) {
        assertTrue("rule R" + rec.ruleNumber() + " must be used at least twice",
            rec.getOccurrences().size() >= 2);
      }
    }
  }

  @Test
  public void ruleUtilityHoldsWithInterleavedTerminals() throws Exception {
    GrammarRules rules = SequiturFactory.runSequitur("x y z x y w x y").toGrammarRulesData();
    assertEquals(2, rules.size());
    assertEquals("R1 z R1 w R1", rules.get(0).getRuleString().trim());
    assertEquals("x y", rules.get(1).getRuleString().trim());
    assertEquals("[0, 3, 6]", rules.get(1).getOccurrences().toString());
  }

  // ---------------------------------------------------------------------------------------------
  // occurrence semantics under rule absorption (the SAXNonTerminal.cleanUp TODO)
  // ---------------------------------------------------------------------------------------------

  @Test
  public void absorbedRuleKeepsAllOccurrenceIndexes() throws Exception {
    // Paper grammar TEST2: R2 ("b c") is referenced from inside R1 only, yet its occurrence
    // list retains ALL FOUR positions in the original token stream — indexes are never pruned
    // when a rule is absorbed into a longer rule. RRA interval mapping depends on this;
    // r.indexes.remove(...) sits commented-out under a TODO in SAXNonTerminal.cleanUp().
    GrammarRules rules = SequiturFactory.runSequitur("a b c d b c a b c d b c")
        .toGrammarRulesData();
    assertEquals(3, rules.size());
    assertEquals("R1 R1", rules.get(0).getRuleString().trim());
    assertEquals("a R2 d R2", rules.get(1).getRuleString().trim());
    assertEquals("b c", rules.get(2).getRuleString().trim());
    assertEquals("[0, 6]", rules.get(1).getOccurrences().toString());
    assertEquals("[1, 4, 7, 10]", rules.get(2).getOccurrences().toString());
  }

  // ---------------------------------------------------------------------------------------------
  // decompression round-trip on a real SAX token stream
  // ---------------------------------------------------------------------------------------------

  @Test
  public void expandedR0MatchesInputOnRealSaxString() throws Exception {
    GrammarRules rules = SequiturFactory.runSequitur(JMOTIF_R_BUGS_SAX_STRING)
        .toGrammarRulesData();
    assertEquals(JMOTIF_R_BUGS_SAX_STRING, rules.get(0).getExpandedRuleString().trim());
    assertEquals(6, rules.size());
    // every non-R0 rule's expansion must occur verbatim in the input at each recorded index
    String[] tokens = JMOTIF_R_BUGS_SAX_STRING.split("\\s+");
    for (GrammarRuleRecord rec : rules) {
      if (0 == rec.ruleNumber()) {
        continue;
      }
      String[] expansion = rec.getExpandedRuleString().trim().split("\\s+");
      for (int occurrence : rec.getOccurrences()) {
        for (int i = 0; i < expansion.length; i++) {
          assertEquals("R" + rec.ruleNumber() + " expansion at token " + (occurrence + i),
              tokens[occurrence + i], expansion[i]);
        }
      }
    }
  }

  // ---------------------------------------------------------------------------------------------
  // repeated-run determinism (guards the divergent static-reset paths)
  // ---------------------------------------------------------------------------------------------

  @Test
  public void repeatedRunsProduceIdenticalGrammars() throws Exception {
    String first = dump(SequiturFactory.runSequitur(JMOTIF_R_BUGS_SAX_STRING)
        .toGrammarRulesData());
    String second = dump(SequiturFactory.runSequitur(JMOTIF_R_BUGS_SAX_STRING)
        .toGrammarRulesData());
    String third = dump(SequiturFactory.runSequitur("a b c d b c a b c d b c")
        .toGrammarRulesData());
    String fourth = dump(SequiturFactory.runSequitur(JMOTIF_R_BUGS_SAX_STRING)
        .toGrammarRulesData());
    assertEquals("same input must yield the same grammar", first, second);
    assertEquals("a different run in between must not perturb the grammar", first, fourth);
    assertEquals(3, SequiturFactory.runSequitur("a b c d b c a b c d b c").toGrammarRulesData()
        .size());
    assertEquals(third, dump(SequiturFactory.runSequitur("a b c d b c a b c d b c")
        .toGrammarRulesData()));
  }

  private static String dump(GrammarRules rules) {
    StringBuilder sb = new StringBuilder();
    ArrayList<Integer> numbers = new ArrayList<Integer>();
    for (GrammarRuleRecord rec : rules) {
      numbers.add(rec.ruleNumber());
      sb.append('R').append(rec.ruleNumber()).append(" -> ").append(rec.getRuleString())
          .append(" exp ").append(rec.getExpandedRuleString()).append(" occ ")
          .append(rec.getOccurrences()).append(';');
    }
    sb.append(numbers);
    return sb.toString();
  }
}
