package net.seninp.gi.rpr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.StringTokenizer;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.seninp.gi.repair.RePairGrammar;
import net.seninp.gi.repair.RePairGuard;
import net.seninp.gi.repair.RePairRule;
import net.seninp.gi.repair.RePairSymbol;

/**
 * Improved repair implementation.
 * 
 * @author psenin
 *
 */
public class NewRepair {

  private static final String SPACE = " ";

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.ALL;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(NewRepair.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * Parses the input string into a grammar.
   * 
   * @param inputStr the string to parse.
   * @return
   */
  public static RePairGrammar parse(String inputStr) {

    consoleLogger.debug("Input string: " + inputStr);

    RePairGrammar rg = new RePairGrammar();

    // two data structures
    //
    // 1.0. - the string
    ArrayList<RePairSymbol> string = new ArrayList<RePairSymbol>(countSpaces(inputStr) + 1);

    // 2.0. - the priority queue
    PriorityQueue<RepairDigramRecord> digrams = new PriorityQueue<RepairDigramRecord>();

    // 2.0. - the digrams hashtable
    HashMap<String, ArrayList<Integer>> digramsTable = new HashMap<String, ArrayList<Integer>>();

    // tokenize the input string
    StringTokenizer st = new StringTokenizer(inputStr, " ");

    int stringPositionCounter = 0;

    // while there are tokens, populate digrams hash and construct the table
    //
    while (st.hasMoreTokens()) {

      String token = st.nextToken();

      RePairSymbol symbol = new RePairSymbol(token, stringPositionCounter);
      consoleLogger.debug("Token @" + stringPositionCounter + ": " + token);

      string.add(symbol);

      // and into the index
      // take care about digram frequencies
      if (stringPositionCounter > 0) {

        StringBuffer digramStr = new StringBuffer();
        digramStr.append(string.get(stringPositionCounter - 1).toString()).append(SPACE)
            .append(string.get(stringPositionCounter).toString());

        ArrayList<Integer> entry = digramsTable.get(digramStr.toString());
        if (null == entry) {
          ArrayList<Integer> arr = new ArrayList<Integer>();
          arr.add(stringPositionCounter - 1);
          digramsTable.put(digramStr.toString(), arr);
          consoleLogger.debug("Created a digram entry for: " + digramStr.toString());
        }
        else {
          digramsTable.get(digramStr.toString()).add(stringPositionCounter - 1);
          consoleLogger.debug("Added a digram entry to: " + digramStr.toString());
        }
      }

      // go on
      stringPositionCounter++;
    }

    // populate the priority queue
    //
    for (Entry<String, ArrayList<Integer>> e : digramsTable.entrySet()) {
      if (e.getValue().size() > 1) {
        digrams.add(new RepairDigramRecord(e.getKey(), e.getValue().size()));
      }
    }

    RepairDigramRecord entry = null;
    while ((entry = digrams.poll()) != null) {

      // create a new rule
      //
      consoleLogger.debug("Polled an entry: " + entry.str + " : " + entry.freq);

      ArrayList<Integer> occurrences = digramsTable.get(entry.str);

      RePairRule r = new RePairRule(rg);
      r.setFirst(string.get(occurrences.get(0)));
      r.setSecond(string.get(occurrences.get(0) + 1));
      r.assignLevel();

      // substitute each digram entry with a rule
      //
      for (Integer currentIndex : occurrences) {

        // create the new guard to insert
        RePairGuard g = new RePairGuard(r);
        g.setStringPosition(string.get(currentIndex).getStringPosition());
        r.addOccurrence(string.get(currentIndex).getStringPosition());
        string.set(currentIndex, g);
        string.set(currentIndex+1, new RePairGuard(null));
        

        // correct entry at the left
        //
        if (currentIndex > 0) {
          // taking care about immediate neighbor
//          removeDigramFrequencyEntry(currentIndex - 1, string, digramFrequencies);
        }

        if (currentIndex < string.size() - 2) {
//          removeDigramFrequencyEntry(currentIndex + 1, string, digramFrequencies);
        }

        // create the new guard to insert
//        RePairGuard g = new RePairGuard(r);
        g.setStringPosition(string.get(currentIndex).getStringPosition());
        r.addOccurrence(string.get(currentIndex).getStringPosition());
//        substituteDigramAt(rg, currentIndex, g, string, digramFrequencies);

      }

    }
    
//    digrams.

    return null;

  }

  /**
   * Counts spaces in the string.
   * 
   * @param str the string to process.
   * @return number of spaces found.
   */
  private static int countSpaces(String str) {
    if (null == str) {
      return -1;
    }
    int counter = 0;
    for (int i = 0; i < str.length(); i++) {
      if (str.charAt(i) == ' ') {
        counter++;
      }
    }
    return counter;
  }
}
