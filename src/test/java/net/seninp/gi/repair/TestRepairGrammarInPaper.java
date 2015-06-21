package net.seninp.gi.repair;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class TestRepairGrammarInPaper {

  private static final String TEST_STRING = "abc abc cba XXX abc abc cba";
  
  private static final String TEST_R0 = "R2 XXX R2";
  
  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void test() {
    
    RePairRule r = RePairFactory.buildGrammar(TEST_STRING);
    
    assertTrue("assert proper grammar", TEST_R0.equalsIgnoreCase(RePairRule.toGrammarRules()));
    
  }

}
