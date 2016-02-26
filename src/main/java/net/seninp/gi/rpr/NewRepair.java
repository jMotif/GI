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
    ArrayList<RePairSymbol> symbolizedString = new ArrayList<RePairSymbol>(
        countSpaces(inputStr) + 1);

    // 2.0. - the priority queue
    PriorityQueue<RepairDigramRecord> digramsQueue = new PriorityQueue<RepairDigramRecord>();

    // 3.0. - the digrams occurrence hashtable: string -> indexes
    HashMap<String, ArrayList<Integer>> digramsTable = new HashMap<String, ArrayList<Integer>>();

    // 4.0. - the inverse index table: index -> digram
    HashMap<Integer, RepairDigramRecord> indexesTable = new HashMap<Integer, RepairDigramRecord>();

    // tokenize the input string
    StringTokenizer st = new StringTokenizer(inputStr, " ");

    int stringPositionCounter = 0;

    // while there are tokens, populate digrams hash and construct the table
    //
    while (st.hasMoreTokens()) {

      String token = st.nextToken();

      RePairSymbol symbol = new RePairSymbol(token, stringPositionCounter);
      consoleLogger.debug("Token @" + stringPositionCounter + ": " + token);

      symbolizedString.add(symbol);

      // and into the index
      // take care about digram frequencies
      if (stringPositionCounter > 0) {

        StringBuffer digramStr = new StringBuffer();
        digramStr.append(symbolizedString.get(stringPositionCounter - 1).toString()).append(SPACE)
            .append(symbolizedString.get(stringPositionCounter).toString());

        ArrayList<Integer> entry = digramsTable.get(digramStr.toString());
        if (null == entry) {
          ArrayList<Integer> arr = new ArrayList<Integer>();
          arr.add(stringPositionCounter - 1);
          digramsTable.put(digramStr.toString(), arr);
          consoleLogger.debug(" .created a digram entry for: " + digramStr.toString());
        }
        else {
          digramsTable.get(digramStr.toString()).add(stringPositionCounter - 1);
          consoleLogger.debug(" .added a digram entry to: " + digramStr.toString());
        }
      }

      // go on
      stringPositionCounter++;
    }

    // populate the priority queue and the index -> digram record map
    //
    for (Entry<String, ArrayList<Integer>> e : digramsTable.entrySet()) {
      if (e.getValue().size() > 1) {
        // create a digram record
        RepairDigramRecord dr = new RepairDigramRecord(e.getKey(), e.getValue().size());
        // put the record into the priority queue
        digramsQueue.add(dr);
        // put its indexes into the index table
        for (int idx : e.getValue()) {
          indexesTable.put(idx, dr);
        }
      }
    }

    RepairDigramRecord entry = null;
    while ((entry = digramsQueue.poll()) != null) {

      // create a new rule
      //
      consoleLogger.debug("Polled a priority queue entry: " + entry.str + " : " + entry.freq);

      ArrayList<Integer> occurrences = digramsTable.get(entry.str);

      RePairRule r = new RePairRule(rg);
      r.setFirst(symbolizedString.get(occurrences.get(0)));
      r.setSecond(symbolizedString.get(occurrences.get(0) + 1));
      r.assignLevel();
      consoleLogger.debug(" .created the rule: " + r.toInfoString());

      // substitute each digram entry with a rule
      //
      consoleLogger.debug(" .substituting the digram at locations: " + occurrences.toString());
      for (Integer currentIndex : occurrences) {

        // create the new guard to insert
        RePairGuard g = new RePairGuard(r);
        g.setStringPosition(symbolizedString.get(currentIndex).getStringPosition());
        r.addOccurrence(symbolizedString.get(currentIndex).getStringPosition());
        symbolizedString.set(currentIndex, g);
        symbolizedString.set(currentIndex + 1, new RePairGuard(null));

        // correct entry at the left
        //
        if (currentIndex > 0) {
          // taking care about immediate left neighbor
//          digramsQueue.
//          removeDigramFrequencyEntry(currentIndex - 1, symbolizedString, digramFrequencies);
        }

        if (currentIndex < symbolizedString.size() - 2) {
          // removeDigramFrequencyEntry(currentIndex + 1, string, digramFrequencies);
        }

        // create the new guard to insert
        // RePairGuard g = new RePairGuard(r);
        g.setStringPosition(symbolizedString.get(currentIndex).getStringPosition());
        r.addOccurrence(symbolizedString.get(currentIndex).getStringPosition());
        // substituteDigramAt(rg, currentIndex, g, string, digramFrequencies);

      }

    }

    // digrams.

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
