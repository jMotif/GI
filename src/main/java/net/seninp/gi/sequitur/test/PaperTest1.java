package net.seninp.gi.sequitur.test;

import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;

public class PaperTest1 {

  // private static final String input = "aac abc abb bca acd aac abc";

  // private static String input = "a b c a b c a b c";
  private static String input = "a b a b c a b c";

  public static void main(String[] args) throws Exception {

    @SuppressWarnings("unused")
    SAXRule r = SequiturFactory.runSequitur(input);

    System.out.println(SAXRule.getRules());

  }

}
