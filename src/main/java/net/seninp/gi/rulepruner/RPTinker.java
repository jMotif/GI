package net.seninp.gi.rulepruner;

import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.TSProcessor;

public class RPTinker {

  public static void main(String[] args) throws Exception {

    double[] ts = TSProcessor.readFileColumn("src/resources/test-data/ecg0606.txt", 0, 0);

    RulePruner rp = new RulePruner(ts);
    SampledPoint p = rp.sample(100, 6, 6, NumerosityReductionStrategy.EXACT, 0.01);
    System.out.println(p);
    
  }
}
