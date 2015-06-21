package net.seninp.gi.repair;


public class ParallelRePairGuard extends RePairSymbol {

  protected ParallelRePairRule rule;

  public ParallelRePairGuard(ParallelRePairRule r) {
    super();
    this.rule = r;
    r.setGuard(this);
  }

  public String toString() {
    return this.rule.toString();
  }

  public boolean isGuard() {
    return true;
  }

}
