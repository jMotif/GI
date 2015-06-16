package net.seninp.gi.sequitur;


import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;

public class PaperTest4 {

  private static final String input = "a b a b c a b c d a b c d e a b c d e f";

  public static void main(String[] args) throws Exception {
    
    @SuppressWarnings("unused")
    SAXRule r = SequiturFactory.runSequitur(input);

    System.out.println(SAXRule.getRules());

    
  }

}
