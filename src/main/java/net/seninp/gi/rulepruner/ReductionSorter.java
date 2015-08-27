package net.seninp.gi.rulepruner;

import java.util.Comparator;

public class ReductionSorter implements Comparator<SampledPoint> {

  @Override
  public int compare(SampledPoint o1, SampledPoint o2) {
    if (o1.getReduction() < o2.getReduction())
      return -1;
    if (o1.getReduction() > o2.getReduction())
      return 1;
    return 0;
  }

}
