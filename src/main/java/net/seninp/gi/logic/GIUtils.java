package net.seninp.gi.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * I use this for temporal fixtures.
 * 
 * @author psenin
 * 
 */
public class GIUtils {

  /**
   * Constructor.
   */
  private GIUtils() {
    assert true;
  }

  /**
   * Computes the mean value.
   * 
   * @param values array of values.
   * @return the mean value.
   */
  public static double mean(int[] values) {
    double sum = 0.0;
    for (int i : values) {
      sum = sum + (double) i;
    }
    return sum / (double) values.length;

  }

  /**
   * Run a quick scan along the time series coverage to find a zeroed intervals.
   * 
   * @param coverageArray the coverage to analyze.
   * @return set of zeroed intervals (if found).
   */
  public static List<RuleInterval> getZeroIntervals(int[] coverageArray) {

    ArrayList<RuleInterval> res = new ArrayList<RuleInterval>();

    int start = -1;
    boolean inInterval = false;
    int intervalsCounter = -1;

    // slide over the array from left to the right
    //
    for (int i = 0; i < coverageArray.length; i++) {

      if (0 == coverageArray[i] && !inInterval) {
        start = i;
        inInterval = true;
      }

      if (coverageArray[i] > 0 && inInterval) {
        res.add(new RuleInterval(intervalsCounter, start, i, 0));
        inInterval = false;
        intervalsCounter--;
      }

    }

    // we need to check for the last interval here
    //
    if (inInterval) {
      res.add(new RuleInterval(intervalsCounter, start, coverageArray.length, 0));
    }

    return res;
  }

}
