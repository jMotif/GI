package net.seninp.gi.sequitur.test;


import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;

public class PaperTest3 {

  private static final String input = "a a b a a a b";

  public static void main(String[] args) throws Exception {
    
    @SuppressWarnings("unused")
    SAXRule r = SequiturFactory.runSequitur(input);

    System.out.println(SAXRule.getRules());

  }

}
