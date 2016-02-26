package net.seninp.gi.repair;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import net.seninp.gi.rpr.RepairDigramRecord;

/**
 * Test the digram record.
 * 
 * @author psenin
 *
 */
public class TestRepairDigramRecord {

  private static final int FREQ1 = 10;
  private static final int FREQ2 = 13;
  private static final int FREQ3 = 13;

  private static final String KEY1 = "aaa";
  private static final String KEY2 = "bbb";
  private static final String KEY3 = "ccc";

  /**
   * Test the constructor.
   */
  @Test
  public void testRepairDigramRecord() {
    RepairDigramRecord dr = new RepairDigramRecord(KEY1, FREQ1);
    assertEquals("testing the constructior", KEY1, dr.getDigram());
    assertEquals("testing the constructior", FREQ1, dr.getFrequency());
  }

  /**
   * Test the comparator.
   */
  @Test
  public void testCompareTo() {
    RepairDigramRecord dr1 = new RepairDigramRecord(KEY1, FREQ1);
    RepairDigramRecord dr2 = new RepairDigramRecord(KEY2, FREQ2);
    RepairDigramRecord dr3 = new RepairDigramRecord(KEY3, FREQ3);

    assertEquals("testing the comparator", -1, dr1.compareTo(dr2));
    assertEquals("testing the comparator", 1, dr2.compareTo(dr1));

    assertEquals("testing the comparator", 0, dr2.compareTo(dr3));

  }

}
