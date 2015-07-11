package net.seninp.gi.util;

import net.seninp.gi.repair.RePairFactory;
import net.seninp.gi.repair.RePairGrammar;

import java.util.Scanner;

/**
 * Created by me on 7/11/15.
 */
public class RunLive {

    public static void main(String args[]) {
        while (true) {
            String l = new Scanner(System.in).nextLine();
            RePairGrammar x = RePairFactory.buildGrammar(l);
            System.out.println(x.toGrammarRules());
            System.out.println(x.toGrammarRulesData());
        }
    }
}
