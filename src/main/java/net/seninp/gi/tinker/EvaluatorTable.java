package net.seninp.gi.tinker;

import java.util.Arrays;
import net.seninp.gi.logic.GIUtils;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.rulepruner.RulePrunerFactory;
import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;

public class EvaluatorTable {

  private static final String[] DATASETS = { "gps_track", "dutch_power_demand", "ecg0606",
      "chfdbchf15", "stdb_308", "mitdbx_108", "300_signal1", "318_signal1", "insect", "nprs43",
      "nprs44", "TEK14", "TEK16", "TEK17", "ann_gun_CentroidA1", "winding_col" };

  private static final String TAB = "\t";

  private static final String CR = "\n";

  private static TSProcessor tp = new TSProcessor();
  private static NormalAlphabet na = new NormalAlphabet();
  private static SAXProcessor sp = new SAXProcessor();

  public static void main(String[] args) throws Exception {

    String dataset = DATASETS[15];
    int w = 120;
    int p = 5;
    int a = 5;

    System.out.println("Sampling " + dataset);

    String header = "dataset\twindow\tpaa\talphabet\tapproximation\t"
        + "rules\tgr_size\tfrequency\tcover\tcoverage\t"
        + "pruned_rules\tpruned_gr_size\tpruned_frequency\tpruned_cover\tpruned_coverage\n";

    double[] series = tp.readTS("src/resources/test-data/" + dataset + ".txt", 0);

    if ("300_signal1".equalsIgnoreCase(dataset) || "318_signal1".equalsIgnoreCase(dataset)) {
      series = Arrays.copyOfRange(series, 0, 30000);
    }

    SAXRecords saxData = sp.ts2saxViaWindow(series, w, p, na.getCuts(a),
        NumerosityReductionStrategy.EXACT, 0.01);

    // commented out repair section
    //
    // RePairGrammar grammar = RePairFactory.buildGrammar(saxData);
    // grammar.expandRules();
    // grammar.buildIntervals(saxData, series, w);
    //
    // GrammarRules rules = grammar.toGrammarRulesData();
    //
    // GrammarRules prunedRules = RulePrunerFactory.performPruning(series, rules);

    // sequitur section
    //
    String discretizedTS = saxData.getSAXString(" ");

    SAXRule grammar = SequiturFactory.runSequitur(discretizedTS);
    GrammarRules rules = grammar.toGrammarRulesData();
    SequiturFactory.updateRuleIntervals(rules, saxData, true, series, w, p);

    GrammarRules prunedRules = RulePrunerFactory.performPruning(series, rules);

    StringBuilder sb = new StringBuilder();

    sb.append(dataset).append(TAB);

    sb.append(w).append(TAB);
    sb.append(p).append(TAB);
    sb.append(a).append(TAB);
    sb.append(sp.approximationDistancePAA(series, w, p, 0.01)
        + sp.approximationDistanceAlphabet(series, w, p, a, 0.01)).append(TAB);

    sb.append(rules.size()).append(TAB);
    sb.append(RulePrunerFactory.computeGrammarSize(rules, p)).append(TAB);
    sb.append(rules.getHighestFrequency()).append(TAB);
    sb.append(GIUtils.getCoverAsFraction(series.length, rules)).append(TAB);
    sb.append(GIUtils.getMeanRuleCoverage(series.length, rules)).append(TAB);

    sb.append(prunedRules.size()).append(TAB);
    sb.append(RulePrunerFactory.computeGrammarSize(prunedRules, p)).append(TAB);
    sb.append(prunedRules.getHighestFrequency()).append(TAB);
    sb.append(GIUtils.getCoverAsFraction(series.length, prunedRules)).append(TAB);
    sb.append(GIUtils.getMeanRuleCoverage(series.length, prunedRules)).append(CR);

    System.out.println(header + CR + sb.toString());

  }

}
