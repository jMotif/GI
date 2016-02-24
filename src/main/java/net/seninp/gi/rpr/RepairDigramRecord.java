package net.seninp.gi.rpr;

public class RepairDigramRecord implements Comparable<RepairDigramRecord> {
  protected String str;
  protected int freq;

  public RepairDigramRecord(String key, int frequency) {
    this.str = key;
    this.freq = frequency;
  }

  @Override
  public int compareTo(RepairDigramRecord o) {
    if (this.freq > o.freq) {
      return 1;
    }
    else if (this.freq < o.freq) {
      return -1;
    }
    return 0;
  }

  /**
   * Get the digram string.
   * 
   * @return the digram string.
   */
  public String getDigram() {
    return this.str;
  }

  /**
   * The frequency getter.
   * 
   * @return the frequency.
   */
  public int getFrequency() {
    return this.freq;
  }

}
