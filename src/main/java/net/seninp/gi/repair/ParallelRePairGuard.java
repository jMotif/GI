package net.seninp.gi.repair;

/**
 * The guard used for non-terminals.
 * 
 * @author psenin
 * 
 */
public class ParallelRePairGuard extends RePairSymbol {

  /** The payload. */
  protected ParallelRePairRule rule;

  /**
   * Constructor.
   * 
   * @param rule the rule to guard.
   */
  public ParallelRePairGuard(ParallelRePairRule rule) {
    super();
    this.rule = rule;
  }

  public String toString() {
    return this.rule.toString();
  }

  public boolean isGuard() {
    return true;
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
    ParallelRePairGuard other = (ParallelRePairGuard) obj;
    if (rule == null) {
      if (other.rule != null)
        return false;
    }
    else if (!rule.equals(other.rule))
      return false;
    return true;
  }

}
