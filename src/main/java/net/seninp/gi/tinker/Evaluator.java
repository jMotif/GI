package net.seninp.gi.tinker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

public class Evaluator {

  private static final String[] DATASETS = { "ann_gun_CentroidA1", "chfdbchf15",
      "dutch_power_demand", "ecg0606", "gps_track", "insect", "mitdbx_108", "nprs43", "nprs44",
      "stdb_308", "TEK14", "TEK16", "TEK17", "winding_col", "300_signal1", "318_signal1" };

  private static final int[] WINDOWS = { 100, 120, 140, 160, 180, 200, 220, 240, 260, 280, 300, 320,
      340, 360, 380, 400, 420, 440, 460 };
  private static final int[] PAAS = { 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30 };
  private static final int[] ALPHABETS = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 12 };

  private static final String TAB = "\t";

  private static final String CR = "\n";

  private static TSProcessor tp = new TSProcessor();
  private static NormalAlphabet na = new NormalAlphabet();
  private static SAXProcessor sp = new SAXProcessor();

  public static void main(String[] args) throws Exception {

    String dataset = DATASETS[Integer.valueOf(args[0])];

    System.out.println("Sampling " + dataset);

    BufferedWriter bw = new BufferedWriter(
        new FileWriter(new File(dataset + "_grammarsampler.out")));
    bw.write("dataset\twindow\tpaa\talphabet\tapproximation\t");
    bw.write("rules\tgr_size\tfrequency\tcover\tcoverage\t");
    bw.write("pruned_rules\tpruned_gr_size\tpruned_frequency\tpruned_cover\tpruned_coverage\n");

    double[] series = tp.readTS("src/resources/test-data/" + dataset + ".txt", 0);
    
    if ("300_signal1".equalsIgnoreCase(dataset) || "318_signal1".equalsIgnoreCase(dataset)) {
      series = Arrays.copyOfRange(series, 0, 30000);
    }

    for (int w : WINDOWS) {
      for (int p : PAAS) {
        for (int a : ALPHABETS) {

          SAXRecords saxData = sp.ts2saxViaWindow(series, w, p, na.getCuts(a),
              NumerosityReductionStrategy.EXACT, 0.01);

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

          System.out.print(sb.toString());
          bw.write(sb.toString());

        }
      }
    }

    bw.close();

  }

}
