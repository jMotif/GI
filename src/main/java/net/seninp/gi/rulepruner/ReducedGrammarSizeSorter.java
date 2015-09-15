package net.seninp.gi.rulepruner;

import java.util.Comparator;

public class ReducedGrammarSizeSorter implements Comparator<SampledPoint> {

  @Override
  public int compare(SampledPoint o1, SampledPoint o2) {
    if (o1.getCompressedGrammarSize() < o2.getCompressedGrammarSize())
      return -1;
    if (o1.getCompressedGrammarSize() > o2.getCompressedGrammarSize())
      return 1;
    return 0;
  }

}
