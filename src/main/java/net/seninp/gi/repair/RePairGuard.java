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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((rule == null) ? 0 : rule.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    RePairGuard other = (RePairGuard) obj;
    if (rule == null) {
      if (other.rule != null)
        return false;
    }
    else if (!rule.equals(other.rule))
      return false;
    return true;
  }

}
