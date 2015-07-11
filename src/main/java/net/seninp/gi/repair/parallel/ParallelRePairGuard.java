package net.seninp.gi.repair.parallel;

import net.seninp.gi.repair.RePairSymbol;

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

}
