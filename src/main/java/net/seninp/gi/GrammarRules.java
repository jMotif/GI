package net.seninp.gi;

import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.util.Iterator;
import java.util.stream.Collectors;

public class GrammarRules implements Iterable<GrammarRuleRecord> {

  //private SortedMap<Integer, GrammarRuleRecord> rules;

  final IntObjectHashMap<GrammarRuleRecord> rules = new IntObjectHashMap();

  public GrammarRules() {
    super();
    //this.rules = new TreeMap<Integer, GrammarRuleRecord>();
  }

  @Override
  public String toString() {
    return rules.values().stream().map(x -> x.toString()).collect(Collectors.joining(", ")).toString();
  }

  public void addRule(GrammarRuleRecord arrRule) {
    int key = arrRule.getRuleNumber();
    this.rules.put(key, arrRule);
  }

  public GrammarRuleRecord getRuleRecord(Integer ruleIdx) {
    return this.rules.get(ruleIdx);
  }

  public Iterator<GrammarRuleRecord> iterator() {
    return rules.values().iterator();
  }

  public GrammarRuleRecord get(Integer ruleIndex) {
    return rules.get(ruleIndex);
  }

  public int size() {
    return this.rules.size();
  }

}
