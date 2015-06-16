package net.seninp.gi.sequitur;

import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;


public class PaperTest2 {

  private static final String input = "a b c d b c a b c d b c";

  public static void main(String[] args) throws Exception {

    @SuppressWarnings("unused")
    SAXRule r = SequiturFactory.runSequitur(input);

    System.out.println(SAXRule.getRules());

  }

}
