package net.seninp.gi.rpr;

import net.seninp.gi.repair.RePairGrammar;

public class TestPaper {

	private static final String TEST_STRING = "abc abc cba cba bac XXX abc abc cba cba bac";

	private static final String TEST_R0 = "R4 XXX R4";

	public static void main(String[] args) {
	  
		RePairGrammar grammar = NewRepair.parse(TEST_STRING);

	}

}
