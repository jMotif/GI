package net.seninp.gi.logic;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class TestGIUtils {

  private static final int[] series = { 1, 2, 3, 4, 5, 6, 7 };

  @Test
  public void testMean() {

    // mean
    double sum = 0.;
    for (int i = 1; i < 8; i++) {
      sum = sum + (double) i;
    }
    double testMean = sum / 7;

    assertEquals("testing the int mean", testMean, GIUtils.mean(series), 0.000001);
  }

  @Test
  public void testGetZeroIntervals() {

    int[] testCoverageArray = new int[71];

    for (int i = 3; i < 23; i++) {
      testCoverageArray[i] = 1;
    }
    for (int i = 60; i < 69; i++) {
      testCoverageArray[i] = 1;
    }

    //
    // these leave [0-2], [23-59], and [69-70] uncovered
    //

    List<RuleInterval> zeroIntervals = GIUtils.getZeroIntervals(testCoverageArray);

    assertEquals("test zero intervals", 3, zeroIntervals.size());

    // [0-2]
    assertEquals("test zero intervals", 2 - 0 + 1, zeroIntervals.get(0).getLength());

    // [23-59]
    assertEquals("test zero intervals", 59 - 23 + 1, zeroIntervals.get(1).getLength());

    // [69-70]
    assertEquals("test zero intervals", 70 - 69 + 1, zeroIntervals.get(2).getLength());

  }

  @Test
  public void testCoverFraction() {

    GrammarRules rules = new GrammarRules();

    GrammarRuleRecord r0 = new GrammarRuleRecord();
    ArrayList<RuleInterval> arrPos = new ArrayList<RuleInterval>();
    arrPos.add(new RuleInterval(0, 50));
    arrPos.add(new RuleInterval(51, 120));
    r0.setRuleIntervals(arrPos);

    rules.addRule(r0);

    double coverage = GIUtils.getCoverAsFraction(200, rules);
    assertEquals(0.0, coverage, 0.01);

    rules = new GrammarRules();
    r0.setRuleNumber(1);
    rules.addRule(r0);
    coverage = GIUtils.getCoverAsFraction(200, rules);
    assertEquals(0.595, coverage, 0.001);

  }

  @Test
  public void testMeanCover() {

    GrammarRules rules = new GrammarRules();

    GrammarRuleRecord r1 = new GrammarRuleRecord();
    r1.setRuleNumber(1);
    ArrayList<RuleInterval> arrPos = new ArrayList<RuleInterval>();
    arrPos.add(new RuleInterval(1, 0, 50, 1.0));
    arrPos.add(new RuleInterval(1, 51, 120, 2.0));
    r1.setRuleIntervals(arrPos);
    rules.addRule(r1);

    double meanCoverage = GIUtils.getMeanRuleCoverage(120, rules);
    // 119/120 ..
    assertEquals(0.9916667, meanCoverage, 0.0001);
    //
    //
    GrammarRuleRecord r2 = new GrammarRuleRecord();
    r2.setRuleNumber(2);
    ArrayList<RuleInterval> arrPos2 = new ArrayList<RuleInterval>();
    arrPos2.add(new RuleInterval(1, 51, 120, 2.0));
    r2.setRuleIntervals(arrPos2);

    rules.addRule(r2);

    meanCoverage = GIUtils.getMeanRuleCoverage(120, rules);
    // 119/120 ..
    assertEquals(1.566667, meanCoverage, 0.0001);

  }

}
