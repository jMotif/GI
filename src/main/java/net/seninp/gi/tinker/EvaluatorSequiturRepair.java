package net.seninp.gi.tinker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import net.seninp.gi.logic.GIUtils;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.repair.RePairFactory;
import net.seninp.gi.repair.RePairGrammar;
import net.seninp.gi.rulepruner.RulePrunerFactory;
import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;

public class EvaluatorSequiturRepair {

  private static final String[] DATASETS = { 
//      "ann_gun_CentroidA1", "chfdbchf15",
//      "dutch_power_demand", "ecg0606", "gps_track", "insect", "mitdbx_108", "nprs43", "nprs44",
//      "stdb_308", "TEK14", "TEK16", "TEK17", "winding_col", 
      "300_signal1", "318_signal1" };

  private static final int[] WINDOWS = { 150 };

  private static final int[] PAAS = { 6 };

  private static final int[] ALPHABETS = { 4 };

  private static final String TAB = "\t";

  private static final String CR = "\n";

  // private static final String THE_R = "R";
  //
  // private static final String SPACE = " ";

  private static TSProcessor tp = new TSProcessor();
  private static NormalAlphabet na = new NormalAlphabet();
  private static SAXProcessor sp = new SAXProcessor();

  public static void main(String[] args) throws Exception {

    for (int k = 0; k < DATASETS.length; k++) {
      String dataset = DATASETS[k];

      System.out.println("Sampling " + dataset);

      BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dataset + "evaluator.out")));

      bw.write("dataset\talgorithm\twindow\tpaa\talphabet\tapproximation\t");
      bw.write("rules\tgr_size\tfrequency\tcover\tcoverage\t");
      bw.write(
          "pruned_rules\tpruned_gr_size\tpruned_frequency\tpruned_cover\tpruned_coverage\tmilliseconds\n");

      double[] series = tp.readTS("src/resources/test-data/" + dataset + ".txt", 0);

      ArrayList<Integer> wins = new ArrayList<Integer>();
      for (int i : WINDOWS) {
        wins.add(i);
      }

      for (int w : wins) {
        for (int p : PAAS) {
          for (int a : ALPHABETS) {

            SAXRecords saxData = sp.ts2saxViaWindow(series, w, p, na.getCuts(a),
                NumerosityReductionStrategy.EXACT, 0.01);
            String discretizedTS = saxData.getSAXString(" ");

            // RePair section
            //
            Date start = new Date();
            RePairGrammar grammar = RePairFactory.buildGrammar(discretizedTS);
            // grammar.expandRules();
            grammar.buildIntervals(saxData, series, w);
            GrammarRules rules = grammar.toGrammarRulesData();
            Date end = new Date();
            long milliseconds = end.getTime() - start.getTime();

            GrammarRules prunedRules = RulePrunerFactory.performPruning(series, rules);

            StringBuilder sb = new StringBuilder();

            sb.append(dataset).append(TAB);
            sb.append("re-pair").append(TAB);

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
            sb.append(GIUtils.getMeanRuleCoverage(series.length, prunedRules)).append(TAB);
            sb.append(Long.valueOf(milliseconds).toString()).append(CR);

            System.out.print(sb.toString());
            bw.write(sb.toString());

            // sequitur section
            //
            start = new Date();
            SAXRule grammarSAX = SequiturFactory.runSequitur(discretizedTS);
            rules = grammarSAX.toGrammarRulesData();
            SequiturFactory.updateRuleIntervals(rules, saxData, true, series, w, p);
            end = new Date();
            milliseconds = end.getTime() - start.getTime();

            prunedRules = RulePrunerFactory.performPruning(series, rules);

            sb = new StringBuilder();

            sb.append(dataset).append(TAB);
            sb.append("sequitur").append(TAB);

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
            sb.append(GIUtils.getMeanRuleCoverage(series.length, prunedRules)).append(TAB);
            sb.append(Long.valueOf(milliseconds).toString()).append(CR);

            System.out.print(sb.toString());
            bw.write(sb.toString());

          }
        }
      }
      bw.close();
    }
  }

}
