package net.seninp.gi.logic;

import static org.junit.Assert.assertEquals;
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

}
