package net.seninp.gi.repair;

/**
 * Guard holds a non-terminal symbol. Named following Sequitur convention.
 * 
 * @author psenin
 * 
 */
public class RePairGuard extends RePairSymbol {

  protected RePairRule rule;

  /**
   * Constructor.
   * 
   * @param rule the rule to wrap.
   */
  public RePairGuard(RePairRule rule) {
    super();
    this.rule = rule;
  }

  public String toString() {
    return this.rule.toString();
  }

  public boolean isGuard() {
    return true;
  }

  public int getLevel() {
    return rule.getLevel();
  }

}
