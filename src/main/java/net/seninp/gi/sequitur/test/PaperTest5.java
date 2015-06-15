package net.seninp.gi.sequitur.test;

import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;

public class PaperTest5 {

  private static final String input = "a a a a a b a b a c a c a d a d";

  public static void main(String[] args) throws Exception {

    @SuppressWarnings("unused")
    SAXRule r = SequiturFactory.runSequitur(input);

    System.out.println(SAXRule.getRules());

  }

}
