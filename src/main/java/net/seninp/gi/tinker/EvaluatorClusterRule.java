package net.seninp.gi.tinker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

import net.seninp.gi.clusterrule.ClusterRuleFactory;
import net.seninp.gi.clusterrule.RuleOrganizer;
import net.seninp.gi.logic.GIUtils;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.logic.PackedRuleRecord;
import net.seninp.gi.logic.SAXPointsNumber;
import net.seninp.gi.logic.SameLengthMotifs;
import net.seninp.gi.rulepruner.RulePrunerFactory;
import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;

public class EvaluatorClusterRule {

  private static final String[] DATASETS = { "ann_gun_CentroidA1", "chfdbchf15", "dutch_power_demand", "ecg0606",
      "gps_track", "insect", "mitdbx_108", "nprs43", "nprs44", "stdb_308", "TEK14", "TEK16", "TEK17", "winding_col",
      "300_signal1", "318_signal1" };

  private static final int[] WINDOWS = { 30, 50, 70, 90, 100, 110, 120, 130, 140, 160, 180, 200, 220, 240, 260, 280,
      300, 320, 330, 340, 350, 360, 380, 400, 420, 440, 460 };

  private static final int[] WINDOWS_PD = { 480, 500, 520, 540, 560, 580, 600, 320, 640, 680, 700, 720, 740, 760, 780,
      800, 820, 840, 860, 880, 900 };

  private static final int[] PAAS = { 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30 };
  private static final int[] ALPHABETS = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 12 };

  private static final String TAB = "\t";

  private static final String CR = "\n";

  private static TSProcessor tp = new TSProcessor();
  private static NormalAlphabet na = new NormalAlphabet();
  private static SAXProcessor sp = new SAXProcessor();

  public static void main(String[] args) throws Exception {

    double thresholdLength = 0.1;
    double thresholdCom = 0.5;
    double fractionTopDist = 0.67;

    String dataset = DATASETS[Integer.valueOf(args[0])];

    System.out.println("Sampling " + dataset);

    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dataset + "_repair_grammarsampler_clusterrule.out")));
    bw.write("dataset\twindow\tpaa\talphabet\tapproximation\t");
    bw.write("rules\tgr_size\tfrequency\tcover\tcoverage\t");
    bw.write("packed_rules\tpruned_gr_size\tpacked_frequency\tpruned_cover\tpacked_coverage\n");

    double[] series = tp.readTS("src/resources/test-data/" + dataset + ".txt", 0);

    if ("300_signal1".equalsIgnoreCase(dataset) || "318_signal1".equalsIgnoreCase(dataset)) {
      series = Arrays.copyOfRange(series, 0, 30000);
    }

    ArrayList<Integer> wins = new ArrayList<Integer>();
    for (int i : WINDOWS) {
      wins.add(i);
    }
    if ("dutch_power_demand".equalsIgnoreCase(dataset)) {
      for (int i : WINDOWS_PD) {
        wins.add(i);
      }
    }

    for (int w : wins) {
      for (int p : PAAS) {
        for (int a : ALPHABETS) {

          SAXRecords saxData = sp.ts2saxViaWindow(series, w, p, na.getCuts(a), NumerosityReductionStrategy.EXACT, 0.01);

          // sequitur section
          //
          String discretizedTS = saxData.getSAXString(" ");

          SAXRule grammar = SequiturFactory.runSequitur(discretizedTS);
          GrammarRules rules = grammar.toGrammarRulesData();
          SequiturFactory.updateRuleIntervals(rules, saxData, true, series, w, p);

          ArrayList<SameLengthMotifs> refinedClassifiedMotifs = ClusterRuleFactory.performPruning(series, rules,
              thresholdLength, thresholdCom, fractionTopDist);
          ArrayList<PackedRuleRecord> packedRules = ClusterRuleFactory.getPackedRule(refinedClassifiedMotifs);

          RuleOrganizer ro = new RuleOrganizer();
          SAXPointsNumber[] pointsOccurenceInPackedRule = ro.countPointNumberAfterRemoving(series,
              refinedClassifiedMotifs);

          StringBuilder sb = new StringBuilder();

          sb.append(dataset).append(TAB);

          sb.append(w).append(TAB);
          sb.append(p).append(TAB);
          sb.append(a).append(TAB);
          sb.append(
              sp.approximationDistancePAA(series, w, p, 0.01) + sp.approximationDistanceAlphabet(series, w, p, a, 0.01))
              .append(TAB);

          sb.append(rules.size()).append(TAB);
          sb.append(RulePrunerFactory.computeGrammarSize(rules, p)).append(TAB);
          sb.append(rules.getHighestFrequency()).append(TAB);
          sb.append(GIUtils.getCoverAsFraction(series.length, rules)).append(TAB);
          sb.append(GIUtils.getMeanRuleCoverage(series.length, rules)).append(TAB);

          sb.append(packedRules.size()).append(TAB);
          sb.append("none").append(TAB);
          sb.append(getHighestFrequency(pointsOccurenceInPackedRule)).append(TAB);
          sb.append(GIUtils.getCoverAsFraction(series.length, refinedClassifiedMotifs)).append(TAB);
          sb.append(GIUtils.getMeanRuleCoverage(series.length, refinedClassifiedMotifs)).append(CR);

          System.out.print(sb.toString());
          bw.write(sb.toString());

        }
      }
    }

    bw.close();

  }

  public static int getHighestFrequency(SAXPointsNumber[] pointsOccurenceInPackedRule) {
    int res = 0;
    for (SAXPointsNumber r : pointsOccurenceInPackedRule) {
      if (r.getPointOccurenceNumber() > res) {
        res = r.getPointOccurenceNumber();
      }

    }
    return res;
  }

}
