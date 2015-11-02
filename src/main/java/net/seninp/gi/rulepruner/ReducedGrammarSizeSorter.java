package net.seninp.gi.rulepruner;

import java.util.Comparator;

/**
 * Sorts sampled points according to the reduction in the Grammar size.
 * 
 * @author psenin
 *
 */
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
