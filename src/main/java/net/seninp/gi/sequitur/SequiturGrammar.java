package net.seninp.gi.sequitur;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;
import net.seninp.gi.logic.GrammarRuleRecord;

/**
 * Per-inference Sequitur state: the digram index, the rule registry, the rule counter, and the
 * rule-record cache. One instance backs one grammar; independent instances are fully isolated, so
 * grammars can be inferred concurrently (e.g. parallel parameter sweeps).
 *
 * <p>
 * Historically these four structures were JVM-global statics on {@link SAXSymbol} and
 * {@link SAXRule} (one inference per JVM, three divergent reset paths). They now live here; a
 * fresh context is created by {@code new SAXRule()} — the root-rule bootstrap used by
 * {@link SequiturFactory} — and is threaded to every symbol and rule of that grammar.
 * {@code RePairGrammar} in this repo went through the same static-to-instance refactor earlier
 * and served as the template.
 *
 * @author psenin
 */
public class SequiturGrammar {

  /** The digram index enforcing the digram-uniqueness constraint. */
  final Hashtable<SAXSymbol, SAXSymbol> digrams = new Hashtable<SAXSymbol, SAXSymbol>(1024);

  /** The rule enumerator; the root rule takes 0. */
  final AtomicInteger numRules = new AtomicInteger(0);

  /** All rules ever created for this grammar, root first (expanded-away rules included). */
  final ArrayList<SAXRule> rules = new ArrayList<SAXRule>();

  /** Rule records cache, (re)built by {@link SAXRule#getSAXRules()}. */
  final ArrayList<GrammarRuleRecord> ruleRecords = new ArrayList<GrammarRuleRecord>();

}
