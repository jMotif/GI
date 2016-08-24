package net.seninp.gi.sequitur;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import net.seninp.gi.logic.GrammarRuleRecord;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.logic.RuleInterval;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;

public class TestTSSequiturDecompositionNONE {

  private double[] data;

  NormalAlphabet na = new NormalAlphabet();
  TSProcessor tp = new TSProcessor();
  SAXProcessor sp = new SAXProcessor();

  @Before
  public void initialize() {
    data = new double[30];
    for (int i = 0; i < 10; i++) {
      data[i] = -1;
      data[i + 10] = 0;
      data[i + 20] = 1;
    }
  }

  @Test
  public void test() {
    try {

      SAXRecords sax = sp.ts2saxViaWindow(data, 3, 2, na.getCuts(3),
          NumerosityReductionStrategy.NONE, 0.5);
      String sax_str = sax.getSAXString(" ");

      SAXRule r = SequiturFactory.runSequitur(sax_str);
      // GrammarRules rules = r.toGrammarRulesData();

      GrammarRules rules = SequiturFactory.series2SequiturRules(data, 3, 2, 3,
          NumerosityReductionStrategy.NONE, 0.5);

      // System.out.println(SAXRule.printRules() + "\n ---- \n");
      // Number Name Level Occurr. Usage Yield Rule str Expaneded Indexes
      // 0 R0 0 0 0 0 R1 R1 R2 R3 R3 R2 R4 R4 aa aa aa aa aa aa aa aa ac ac bb bb bb bb bb bb bb bb
      // ac ac cc cc cc cc cc cc cc cc []
      // 1 R1 2 2 2 4 R5 R5 aa aa aa aa [0, 4]
      // 2 R2 1 2 2 2 ac ac ac ac [8, 18]
      // 3 R3 2 2 2 4 R6 R6 bb bb bb bb [10, 14]
      // 4 R4 2 2 2 4 R7 R7 cc cc cc cc [20, 24]
      // 5 R5 1 4 2 2 aa aa aa aa [0, 2, 4, 6]
      // 6 R6 1 4 2 2 bb bb bb bb [10, 12, 14, 16]
      // 7 R7 1 4 2 2 cc cc cc cc [20, 22, 24, 26]

      // for (GrammarRuleRecord rule : rules) {
      // System.out.println(rule.getRuleIntervals());
      // }

      SequiturFactory.updateRuleIntervals(rules, sax, true, data, 3, 2);

      // now rule #1 is the set of two R5 which are "aa aa" --
      // is two points and three points interval -- 4 points total
      // ... two of these "aa aa aa aa" map to 4 points + 3 points interval = makes 6 points

      GrammarRuleRecord r5 = rules.get(5);
      RuleInterval firstR5occurrence = r5.getRuleIntervals().get(0);
      assertEquals(0, firstR5occurrence.getStart());
      assertEquals(4, firstR5occurrence.getEnd());

      ArrayList<RuleInterval> R5positions = SequiturFactory.getRulePositionsByRuleNum(5, r, sax,
          data, 3);
      assertEquals(0, R5positions.get(0).getStart());
      assertEquals(4, R5positions.get(0).getEnd());

      //
      //
      //

      GrammarRuleRecord r1 = rules.get(1);
      RuleInterval secondR1occurrence = r1.getRuleIntervals().get(1);
      assertEquals(4, secondR1occurrence.getStart());
      assertEquals(10, secondR1occurrence.getEnd());

      ArrayList<RuleInterval> R1positions = SequiturFactory.getRulePositionsByRuleNum(1, r, sax,
          data, 3);
      assertEquals(4, R1positions.get(1).getStart());
      assertEquals(10, R1positions.get(1).getEnd());

      // for (GrammarRuleRecord rule : rules) {
      // System.out.println(rule.getRuleIntervals());
      // }

    }
    catch (Exception e) {
      fail("exception shouldn't be thrown");
    }
  }

}
