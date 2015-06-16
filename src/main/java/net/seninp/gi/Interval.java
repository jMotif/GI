package net.seninp.gi;

/**
 * Implements an interval. Start inclusive, end exclusive.
 * 
 * @author psenin
 * 
 */
public class Interval {

  private int start;
  private int end;
  private double coverage;

  /**
   * Constructor; start inclusive, end exclusive.
   * 
   * @param start the interval's start.
   * @param end the interval's end.
   * @param coverage the interval's coverage.
   */
  public Interval(int start, int end, double coverage) {
    this.start = start;
    this.end = end;
    this.coverage = coverage;
  }

  public double getCoverage() {
    return coverage;
  }

  public void setCoverage(double coverage) {
    this.coverage = coverage;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public int getStart() {
    return this.start;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public int getEnd() {
    return this.end;
  }

  public int getLength() {
    return Math.abs(this.end - this.start);
  }

  /**
   * True if two intervals overlap.
   * 
   * @param other interval to compare with.
   * @return true if there is an overlap.
   */
  public boolean overlaps(Interval other) {
    if (null == other) {
      return false;
    }
    if ((this.start < other.getEnd()) && (this.end > other.getStart())) {
      return true;
    }
    return false;
  }

}
