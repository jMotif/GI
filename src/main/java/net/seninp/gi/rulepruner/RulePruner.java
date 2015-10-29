package net.seninp.gi.rulepruner;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.seninp.gi.GIAlgorithm;
import net.seninp.gi.logic.GrammarRuleRecord;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.repair.RePairFactory;
import net.seninp.gi.repair.RePairGrammar;
import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import net.seninp.jmotif.sax.parallel.ParallelSAXImplementation;

/**
 * Implements the rule pruner.
 * 
 * @author psenin
 *
 */
public class RulePruner {

  private static final String COMMA = ",";
  private static final String CR = "\n";

  private static final DecimalFormat dfPercent = (new DecimalFormat("0.00"));
  private static final DecimalFormat dfSize = (new DecimalFormat("#.0000"));

  private double[] ts;
  private SAXProcessor sp;

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(RulePruner.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
    dfPercent.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
    dfSize.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
  }

  public RulePruner(double[] ts) {
    this.ts = ts;
    this.sp = new SAXProcessor();
  }

  public SampledPoint sample(int windowSize, int paaSize, int alphabetSize,
      NumerosityReductionStrategy nrStrategy, double nThreshold) throws Exception {

    SampledPoint res = new SampledPoint();

    StringBuffer logStr = new StringBuffer();
    logStr.append(windowSize).append(COMMA).append(paaSize).append(COMMA).append(alphabetSize)
        .append(COMMA);

    res.setWindow(windowSize);
    res.setPAA(paaSize);
    res.setAlphabet(alphabetSize);

    // convert to SAX
    //
    ParallelSAXImplementation ps = new ParallelSAXImplementation();
    SAXRecords saxData = ps.process(ts, 2, windowSize, paaSize, alphabetSize,
        RulePrunerParameters.SAX_NR_STRATEGY, RulePrunerParameters.SAX_NORM_THRESHOLD);
    if (Thread.currentThread().isInterrupted() && null == saxData) {
      System.err.println("Sampler being interrupted, returning NULL!");
      return null;
    }
    saxData.buildIndex();

    // compute SAX approximation distance
    //
    double approximationDistance = sp.approximationDistance(ts, windowSize, paaSize, alphabetSize,
        RulePrunerParameters.SAX_NR_STRATEGY, RulePrunerParameters.SAX_NORM_THRESHOLD);
    logStr.append(dfSize.format(approximationDistance)).append(COMMA);
    res.setApproxDist(approximationDistance);

    // build a grammar
    //
    GrammarRules rules = null;
    if (GIAlgorithm.SEQUITUR.equals(RulePrunerParameters.GI_ALGORITHM_IMPLEMENTATION)) {
      SAXRule r = SequiturFactory.runSequitur(saxData.getSAXString(" "));
      rules = r.toGrammarRulesData();
      SequiturFactory.updateRuleIntervals(rules, saxData, true, ts, windowSize, paaSize);
    }
    else if (GIAlgorithm.REPAIR.equals(RulePrunerParameters.GI_ALGORITHM_IMPLEMENTATION)) {
      RePairGrammar grammar = RePairFactory.buildGrammar(saxData.getSAXString(" "));
      rules = grammar.toGrammarRulesData();
    }

    Integer grammarSize = RulePrunerFactory.computeValidGrammarSize(ts, rules, paaSize);
    logStr.append(grammarSize).append(COMMA);
    logStr.append(rules.size()).append(COMMA);
    res.setGrammarSize(grammarSize);
    res.setGrammarRules(rules.size());

    // prune grammar' rules
    //
    GrammarRules prunedRulesSet = RulePrunerFactory.performPruning(ts, rules);
    Integer compressedSize = RulePrunerFactory.computePrunedGrammarSize(ts, prunedRulesSet,
        paaSize);
    logStr.append(compressedSize).append(COMMA);
    logStr.append(prunedRulesSet.size()).append(COMMA);
    res.setCompressedGrammarSize(compressedSize);
    res.setPrunedRules(prunedRulesSet.size());

    // compute the cover
    //
    boolean[] compressedCover = new boolean[ts.length];
    compressedCover = RulePrunerFactory.updateRanges(compressedCover, prunedRulesSet);
    if (RulePrunerFactory.hasEmptyRanges(compressedCover)) {
      logStr.append("0").append(COMMA);
      res.setCovered(false);
    }
    else {
      logStr.append("1").append(COMMA);
      res.setCovered(true);
    }

    // compute the coverage in percent
    //
    double coverage = RulePrunerFactory.computeCover(compressedCover);
    logStr.append(dfPercent.format(coverage));
    res.setCoverage(coverage);

    res.setReduction((double) compressedSize / (double) grammarSize);

    // get the most frequent rule
    //
    int maxFreq = Integer.MIN_VALUE;
    for (GrammarRuleRecord r : prunedRulesSet) {
      if (r.getOccurrences().size() > maxFreq) {
        maxFreq = r.getOccurrences().size();
      }
    }

    res.setMaxFrequency(maxFreq);

    // wrap it up
    //
    logStr.append(CR);

    // print the output
    //
    // bw.write(logStr.toString());
    consoleLogger.info(logStr.toString().replace(CR, ""));

    return res;
  }

}
