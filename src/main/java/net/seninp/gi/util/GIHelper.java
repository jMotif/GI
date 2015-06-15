package net.seninp.gi.util;

public class GIHelper {

  public GIHelper() {
    assert true;
  }

  public double mean(int[] values) {
    double sum = 0.0;
    for (int i : values) {
      sum = sum + (double) i;
    }
    return sum / (double) values.length;

  }
}
