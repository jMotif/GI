package net.seninp.gi.rulepruner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import org.slf4j.LoggerFactory;
import com.beust.jcommander.JCommander;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.seninp.gi.GIAlgorithm;
import net.seninp.gi.GrammarRules;
import net.seninp.gi.repair.RePairFactory;
import net.seninp.gi.repair.RePairGrammar;
import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.datastructures.SAXRecords;
import net.seninp.jmotif.sax.parallel.ParallelSAXImplementation;
import net.seninp.util.StackTrace;

/**
 * Rule pruner experimentation.
 * 
 * @author psenin
 * 
 */
public class RulePrunerPrinter {

  //
  //
  // -b "10 200 10 2 10 1 2 10 1" -d
  // /media/Stock/tmp/ydata-labeled-time-series-anomalies-v1_0/A1Benchmark/real_22.csv.column -o
  // /media/Stock/tmp/test.out
  //
  //

  // constants and formatter
  //
  private static final String COMMA = ",";
  private static final String CR = "\n";
  private static final DecimalFormat dfPercent = (new DecimalFormat("0.00"));
  private static final DecimalFormat dfSize = (new DecimalFormat("#.0000"));

  private static final String OUTPUT_HEADER = "window,paa,alphabet,approxDist,grammarSize,grammarRules,"
      + "compressedGrammarSize,prunedRules,isCovered,coverage\n";

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(RulePrunerPrinter.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
    dfPercent.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
    dfSize.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
  }

  /**
   * Main runnable.
   * 
   * @param args parameters used.
   * @throws Exception if error occurs.
   */
  public static void main(String[] args) throws Exception {

    try {

      RulePrunerParameters params = new RulePrunerParameters();
      JCommander jct = new JCommander(params, args);

      if (0 == args.length) {
        jct.usage();
      }
      else {

        // get params printed
        //
        StringBuffer sb = new StringBuffer(1024);
        sb.append("Rule pruner CLI v.1").append(CR);
        sb.append("parameters:").append(CR);

        sb.append("  input file:           ").append(RulePrunerParameters.IN_FILE).append(CR);
        sb.append("  output file:          ").append(RulePrunerParameters.OUT_FILE).append(CR);
        sb.append("  SAX num. reduction:   ").append(RulePrunerParameters.SAX_NR_STRATEGY)
            .append(CR);
        sb.append("  SAX norm. threshold:  ").append(RulePrunerParameters.SAX_NORM_THRESHOLD)
            .append(CR);
        sb.append("  GI Algorithm:         ")
            .append(RulePrunerParameters.GI_ALGORITHM_IMPLEMENTATION).append(CR);
        sb.append("  Grid boundaries:      ").append(RulePrunerParameters.GRID_BOUNDARIES)
            .append(CR);

        if (!(Double.isNaN(RulePrunerParameters.SUBSAMPLING_FRACTION))) {
          sb.append("  Subsampling fraction: ").append(RulePrunerParameters.SUBSAMPLING_FRACTION)
              .append(CR);
        }

        // printer out the params before starting
        System.err.print(sb.toString());

        // read the data in
        String dataFName = RulePrunerParameters.IN_FILE;
        double[] ts = TSProcessor.readFileColumn(dataFName, 0, 0);
        if (!(Double.isNaN(RulePrunerParameters.SUBSAMPLING_FRACTION))) {
          ts = Arrays.copyOfRange(ts, 0,
              (int) Math.round((double) ts.length * RulePrunerParameters.SUBSAMPLING_FRACTION));
        }

        // printer out the params before starting
        System.err.println("  working with series of " + ts.length + " points ... " + CR);

        // parse the boundaries params
        int[] boundaries = toBoundaries(RulePrunerParameters.GRID_BOUNDARIES);

        // create the output file
        BufferedWriter bw = new BufferedWriter(
            new FileWriter(new File(RulePrunerParameters.OUT_FILE)));
        bw.write(OUTPUT_HEADER);

        ArrayList<SampledPoint> res = new ArrayList<SampledPoint>();

        // we need to use this in the loop
        SAXProcessor sp = new SAXProcessor();

        // iterate over the grid evaluating the grammar
        //
        for (int WINDOW_SIZE = boundaries[0]; WINDOW_SIZE < boundaries[1]; WINDOW_SIZE += boundaries[2]) {
          for (int PAA_SIZE = boundaries[3]; PAA_SIZE < boundaries[4]; PAA_SIZE += boundaries[5]) {

            // check for invalid cases
            if (PAA_SIZE > WINDOW_SIZE) {
              continue;
            }

            for (int ALPHABET_SIZE = boundaries[6]; ALPHABET_SIZE < boundaries[7]; ALPHABET_SIZE += boundaries[8]) {

              SampledPoint p = new SampledPoint();

              StringBuffer logStr = new StringBuffer();
              logStr.append(WINDOW_SIZE).append(COMMA).append(PAA_SIZE).append(COMMA)
                  .append(ALPHABET_SIZE).append(COMMA);

              p.setWindow(WINDOW_SIZE);
              p.setPAA(PAA_SIZE);
              p.setAlphabet(ALPHABET_SIZE);

              // convert to SAX
              //
              ParallelSAXImplementation ps = new ParallelSAXImplementation();
              SAXRecords saxData = ps.process(ts, 2, WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE,
                  RulePrunerParameters.SAX_NR_STRATEGY, RulePrunerParameters.SAX_NORM_THRESHOLD);
              saxData.buildIndex();

              // compute SAX approximation distance
              //
              double approximationDistance = sp.approximationDistance(ts, WINDOW_SIZE, PAA_SIZE,
                  ALPHABET_SIZE, RulePrunerParameters.SAX_NR_STRATEGY,
                  RulePrunerParameters.SAX_NORM_THRESHOLD);
              logStr.append(dfSize.format(approximationDistance)).append(COMMA);
              p.setApproxDist(approximationDistance);

              // build a grammar
              //
              GrammarRules rules = null;
              if (GIAlgorithm.SEQUITUR.equals(RulePrunerParameters.GI_ALGORITHM_IMPLEMENTATION)) {
                SAXRule r = SequiturFactory.runSequitur(saxData.getSAXString(" "));
                rules = r.toGrammarRulesData();
                SequiturFactory.updateRuleIntervals(rules, saxData, true, ts, WINDOW_SIZE,
                    PAA_SIZE);
              }
              else
                if (GIAlgorithm.REPAIR.equals(RulePrunerParameters.GI_ALGORITHM_IMPLEMENTATION)) {
                RePairGrammar grammar = RePairFactory.buildGrammar(saxData.getSAXString(" "));
                rules = grammar.toGrammarRulesData();
              }

              Integer grammarSize = RulePrunerFactory.computeGrammarSize(ts, rules, saxData,
                  PAA_SIZE);
              logStr.append(grammarSize).append(COMMA);
              logStr.append(rules.size()).append(COMMA);
              p.setGrammarSize(grammarSize);
              p.setGrammarRules(rules.size());

              // prune grammar' rules
              //
              GrammarRules prunedRulesSet = RulePrunerFactory.performPruning(ts, rules);
              Integer compressedSize = RulePrunerFactory.computeGrammarSize(ts, prunedRulesSet,
                  saxData, PAA_SIZE);
              logStr.append(compressedSize).append(COMMA);
              logStr.append(prunedRulesSet.size()).append(COMMA);
              p.setCompressedGrammarSize(compressedSize);
              p.setPrunedRules(prunedRulesSet.size());

              // compute the cover
              //
              boolean[] compressedCover = new boolean[ts.length];
              compressedCover = RulePrunerFactory.updateRanges(compressedCover, prunedRulesSet);
              if (RulePrunerFactory.hasEmptyRanges(compressedCover)) {
                logStr.append("0").append(COMMA);
                p.setCovered(false);
              }
              else {
                logStr.append("1").append(COMMA);
                p.setCovered(true);
              }

              // compute the coverage in percent
              //
              double coverage = RulePrunerFactory.computeCover(compressedCover);
              logStr.append(dfPercent.format(coverage));
              p.setCoverage(coverage);

              p.setReduction((double) compressedSize / (double) grammarSize);

              // wrap it up
              //
              logStr.append(CR);

              // print the output
              //
              bw.write(logStr.toString());
              consoleLogger.info(logStr.toString().replace(CR, ""));

              res.add(p);
            }
          }
        }
        bw.close();

        Collections.sort(res, new ReductionSorter());
      }
    }
    catch (Exception e) {
      System.err.println("error occured while parsing parameters " + Arrays.toString(args) + CR
          + StackTrace.toString(e));
      System.exit(-1);
    }

  }

  /**
   * Converts a param string to boundaries array.
   * 
   * @param str
   * @return
   */
  private static int[] toBoundaries(String str) {
    int[] res = new int[9];
    String[] split = str.split("\\s+");
    for (int i = 0; i < 9; i++) {
      res[i] = Integer.valueOf(split[i]);
    }
    return res;
  }

}
