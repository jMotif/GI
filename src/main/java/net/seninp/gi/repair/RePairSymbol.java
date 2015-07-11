package net.seninp.gi.repair;

import java.util.Arrays;
import net.seninp.jmotif.sax.datastructures.SAXRecord;

/**
 * The symbol -- which essentially is a token.
 * 
 * @author psenin
 * 
 */
public class RePairSymbol {

  /**
   * Payload.
   */
  final private char[] string;

  /**
   * Position of the symbol in the string.
   */
  private int stringPosition;

  final static char[] blank = new char[0];

  /**
   * Constructor.
   */
  public RePairSymbol() {
    super();
    this.stringPosition = -1;
    this.string = blank;
  }

  /**
   * Constructor.
   * 
   * @param r the SAX record to use for the symbol construction.
   * @param stringPosition the position of the symbol in the string.
   */
  public RePairSymbol(SAXRecord r, Integer stringPosition) {
    super();
    this.string = Arrays.copyOf(r.getPayload(), r.getPayload().length);
    this.stringPosition = stringPosition;
  }

  /**
   * Constructor.
   * 
   * @param token the payload.
   * @param stringPosition the position of the symbol in the string.
   */
  public RePairSymbol(String token, int stringPosition) {
    super();
    this.string = token.toCharArray();
    this.stringPosition = stringPosition;
  }

  /**
   * This is overridden in Guard.
   * 
   * @return true if the symbol is the guard.
   */
  public boolean isGuard() {
    return false;
  }

  /**
   * The position getter.
   * 
   * @return The symbol position in the string.
   */
  public int getStringPosition() {
    return this.stringPosition;
  }

  /**
   * The position setter.
   * 
   * @param saxStringPosition the position to set.
   */
  public void setStringPosition(int saxStringPosition) {
    this.stringPosition = saxStringPosition;
  }

  /**
   * This will be overridden in the non-Terminal symbol, i.e. guard.
   * 
   * @return The rule hierarchy level.
   */
  public int getLevel() {
    return 0;
  }


  public char[] key() { return string; }

  public String toString() {
    return String.valueOf(this.string);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(string);
    result = prime * result + ((stringPosition == -1) ? -1 : stringPosition);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof RePairSymbol))
      return false;

    RePairSymbol other = (RePairSymbol) obj;

    if (stringPosition == -1) {
      if (other.stringPosition != -1)
        return false;
    }
    else if (stringPosition!=other.stringPosition)
      return false;

    if (!Arrays.equals(string, other.string))
      return false;


    return true;
  }

}
