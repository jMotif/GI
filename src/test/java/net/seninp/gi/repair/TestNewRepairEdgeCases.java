package net.seninp.gi.repair;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TestNewRepairEdgeCases {

  @Test
  public void parseEmptyStringReturnsEmptyGrammar() {
    RePairGrammar grammar = NewRepair.parse("");
    assertEquals("", grammar.getR0CompressedString());
    assertTrue(grammar.getRules().isEmpty());
  }

  @Test
  public void parseWhitespaceOnlyReturnsEmptyGrammar() {
    RePairGrammar grammar = NewRepair.parse("   ");
    assertEquals("", grammar.getR0CompressedString());
    assertTrue(grammar.getRules().isEmpty());
  }

  @Test
  public void parseNullReturnsEmptyGrammar() {
    RePairGrammar grammar = NewRepair.parse(null);
    assertEquals("", grammar.getR0CompressedString());
    assertTrue(grammar.getRules().isEmpty());
  }

}
