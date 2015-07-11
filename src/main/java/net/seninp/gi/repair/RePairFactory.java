package net.seninp.gi.repair;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.seninp.jmotif.sax.datastructures.SAXRecord;
import net.seninp.jmotif.sax.datastructures.SAXRecords;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Implements RePair.
 *
 * @author psenin
 */
public final class RePairFactory {

    private static final char SPACE = ' ';

    // logging stuff
    //
    private static Logger consoleLogger;
    private static Level LOGGING_LEVEL = Level.DEBUG;

    static {
        consoleLogger = (Logger) LoggerFactory.getLogger(RePairFactory.class);
        consoleLogger.setLevel(LOGGING_LEVEL);
    }

    /**
     * Disable constructor.
     */
    private RePairFactory() {
        assert true;
    }

    /**
     * Builds a repair grammar given a set of SAX records.
     *
     * @param saxRecords the records to process.
     * @return the grammar.
     */
    public static RePairGrammar buildGrammar(SAXRecords saxRecords) {

        consoleLogger.debug("Starting RePair with an input string of " + saxRecords.getIndexes().size()
                + " words.");

        RePairGrammar rg = new RePairGrammar();

        // get all indexes and sort them
        Set<Integer> index = saxRecords.getIndexes();
        Integer[] sortedIndexes = index.toArray(new Integer[index.size()]);
        Arrays.sort(sortedIndexes);

        // two data structures
        //
        // 1.0. - the string
        ArrayList<RePairSymbol> string = new ArrayList<RePairSymbol>();
        // LinkedList<Symbol> string = new LinkedList<Symbol>();

        //
        // 2.0. - the digram frequency table, digram, frequency, and the first occurrence index
        DigramFrequencies digramFrequencies = new DigramFrequencies();

        // build data structures
        int stringPositionCounter = 0;
        for (Integer saxWordPosition : sortedIndexes) {
            // i is the index of a symbol in the input discretized string
            // counter is the index in the grammar rule R0 string
            SAXRecord r = saxRecords.getByIndex(saxWordPosition);
            RePairSymbol symbol = new RePairSymbol(r, stringPositionCounter);
            // put it into the string
            string.add(symbol);
            // and into the index
            // take care about digram frequencies
            if (stringPositionCounter > 0) {

                StringBuffer digramStr = new StringBuffer();
                digramStr.append(string.get(stringPositionCounter - 1).toString()).append(SPACE)
                        .append(string.get(stringPositionCounter).toString());

                DigramFrequencyEntry entry = digramFrequencies.get(digramStr.toString());
                if (null == entry) {
                    digramFrequencies.put(new DigramFrequencyEntry(digramStr.toString(), 1,
                            stringPositionCounter - 1));
                } else {
                    digramFrequencies.incrementFrequency(entry, 1);
                }
            }
            // go on
            stringPositionCounter++;
        }

        consoleLogger.debug("String length " + string.size() + " unique digrams "
                + digramFrequencies.size());

        DigramFrequencyEntry entry;
        while ((entry = digramFrequencies.getTop()) != null && entry.getFrequency() >= 2) {

            // take the most frequent rule
            //
            // Entry<String, int[]> entry = entries.get(0);
            // DigramFrequencyEntry entry = digramFrequencies.getTop();

      /*
      consoleLogger.info("re-pair iteration, digram \"" + entry.getDigram() + "\", frequency: "
          + entry.getFrequency());


      consoleLogger.debug("Going to substitute the digram " + entry.getDigram()
          + " first occurring at position " + entry.getFirstOccurrence() + " with frequency "
          + entry.getFrequency() + ", '" + string.get(entry.getFirstOccurrence()) + SPACE
          + string.get(entry.getFirstOccurrence() + 1) + "'");
          */

            // create new rule
            //
            RePairRule r = new RePairRule(rg,
                    string.get(entry.getFirstOccurrence()),
                    string.get(entry.getFirstOccurrence() + 1)
            );
            r.assignLevel();

            // substitute each digram entry with a rule
            //
            String digramToSubstitute = entry.getDigram();
            int currentIndex = entry.getFirstOccurrence();
            while (currentIndex < string.size() - 1) {

                StringBuffer currentDigram = new StringBuffer();
                currentDigram.append(string.get(currentIndex).toString()).append(SPACE)
                        .append(string.get(currentIndex + 1).toString());

                if (digramToSubstitute.equalsIgnoreCase(currentDigram.toString())) {
          /*
          consoleLogger.debug(" next digram occurrence is at  " + currentIndex + ", '"
              + string.get(currentIndex) + SPACE + string.get(currentIndex + 1) + "'");
          */

                    // correct entries at left and right
                    if (currentIndex > 0) {
                        // taking care about immediate neighbor
                        removeDigramFrequencyEntry(currentIndex - 1, string, digramFrequencies);
                    }
                    if (currentIndex < string.size() - 2) {
                        removeDigramFrequencyEntry(currentIndex + 1, string, digramFrequencies);
                    }

                    // create the new guard to insert
                    RePairGuard g = new RePairGuard(r);
                    g.setStringPosition(string.get(currentIndex).getStringPosition());
                    r.addOccurrence(string.get(currentIndex).getStringPosition());
                    substituteDigramAt(rg, currentIndex, g, string, digramFrequencies);

                }
                currentIndex++;
            }

            // // sort the entries of digram table by the size of indexes
            // entries = new ArrayList<Entry<String, int[]>>();
            // entries.addAll(digramFrequencies.entrySet());
            // Collections.sort(entries, new Comparator<Entry<String, int[]>>() {
            // @Override
            // public int compare(Entry<String, int[]> o1, Entry<String, int[]> o2) {
            // return -Integer.valueOf(o1.getValue()[0]).compareTo(Integer.valueOf(o2.getValue()[0]));
            // }
            // });

/*
      consoleLogger.debug("*** iteration finished, top count "
          + digramFrequencies.getTop().getFrequency());
          */

        }

        rg.setR0String(stringToDisplay(string));

        return rg;
    }

    /**
     * Builds a grammar given a string of terminals delimeted by space.
     *
     * @param inputString the input string.
     * @return the RePair grammar.
     */
    public static RePairGrammar buildGrammar(String inputString) {

        // consoleLogger.debug("Starting RePair with an input string of " +
        // saxRecords.getIndexes().size()
        // + " words.");

        RePairGrammar rg = new RePairGrammar();

        // two data structures
        //
        // 1.0. - the string
        ArrayList<RePairSymbol> string = new ArrayList();
        // LinkedList<Symbol> string = new LinkedList<Symbol>();

        //
        // 2.0. - the digram frequency table, digram, frequency, and the first occurrence index
        DigramFrequencies digramFrequencies = new DigramFrequencies();

        // build data structures
        // tokenize the input string
        //
        StringTokenizer st = new StringTokenizer(inputString, " ");

        int stringPositionCounter = 0;

        // while there are tokens
        while (st.hasMoreTokens()) {

            String token = st.nextToken();

            RePairSymbol symbol = new RePairSymbol(token, stringPositionCounter);
            // put it into the string
            string.add(symbol);
            // and into the index
            // take care about digram frequencies
            if (stringPositionCounter > 0) {

                StringBuffer digramStr = new StringBuffer();
                digramStr.append(string.get(stringPositionCounter - 1).toString()).append(SPACE)
                        .append(string.get(stringPositionCounter).toString());

                DigramFrequencyEntry entry = digramFrequencies.get(digramStr.toString());
                if (null == entry) {
                    digramFrequencies.put(new DigramFrequencyEntry(digramStr.toString(), 1,
                            stringPositionCounter - 1));
                } else {
                    digramFrequencies.incrementFrequency(entry, 1);
                }
            }
            // go on
            stringPositionCounter++;
        }

        consoleLogger.debug("String length " + string.size() + " unique digrams "
                + digramFrequencies.size());

        DigramFrequencyEntry entry;
        while ((entry = digramFrequencies.getTop()) != null && entry.getFrequency() > 1) {

            // take the most frequent rule
            //
            // Entry<String, int[]> entry = entries.get(0);
            // DigramFrequencyEntry entry = digramFrequencies.getTop();

            /*
            consoleLogger.info("re-pair iteration, digram \"" + entry.getDigram() + "\", frequency: "
                    + entry.getFrequency());

            consoleLogger.debug("Going to substitute the digram " + entry.getDigram()
                    + " first occurring at position " + entry.getFirstOccurrence() + " with frequency "
                    + entry.getFrequency() + ", '" + string.get(entry.getFirstOccurrence()) + SPACE
                    + string.get(entry.getFirstOccurrence() + 1) + "'");
                    */

            // create new rule
            //
            RePairRule r = new RePairRule(rg, string.get(entry.getFirstOccurrence()), string.get(entry.getFirstOccurrence() + 1));
            r.setFirst(string.get(entry.getFirstOccurrence()));
            r.setSecond(string.get(entry.getFirstOccurrence() + 1));
            r.assignLevel();

            // substitute each digram entry with a rule
            //
            String digramToSubstitute = entry.getDigram();
            int currentIndex = entry.getFirstOccurrence();

            StringBuffer currentDigram = new StringBuffer();
            while (currentIndex < string.size() - 1) {


                currentDigram.setLength(0);

                currentDigram.append(string.get(currentIndex)).append(SPACE)
                        .append(string.get(currentIndex + 1));

                //if (digramToSubstitute.equalsIgnoreCase(currentDigram.toString())) {
                if (digramToSubstitute.equals(currentDigram.toString())) {
                    /*consoleLogger.debug(" next digram occurrence is at  " + currentIndex + ", '"
                            + string.get(currentIndex) + SPACE + string.get(currentIndex + 1) + "'");*/

                    // correct entries at left and right
                    if (currentIndex > 0) {
                        // taking care about immediate neighbor
                        removeDigramFrequencyEntry(currentIndex - 1, string, digramFrequencies);
                    }
                    if (currentIndex < string.size() - 2) {
                        removeDigramFrequencyEntry(currentIndex + 1, string, digramFrequencies);
                    }

                    // create the new guard to insert
                    RePairGuard g = new RePairGuard(r);
                    g.setStringPosition(string.get(currentIndex).getStringPosition());
                    r.addOccurrence(string.get(currentIndex).getStringPosition());
                    substituteDigramAt(rg, currentIndex, g, string, digramFrequencies);

                }
                currentIndex++;
            }

            // // sort the entries of digram table by the size of indexes
            // entries = new ArrayList<Entry<String, int[]>>();
            // entries.addAll(digramFrequencies.entrySet());
            // Collections.sort(entries, new Comparator<Entry<String, int[]>>() {
            // @Override
            // public int compare(Entry<String, int[]> o1, Entry<String, int[]> o2) {
            // return -Integer.valueOf(o1.getValue()[0]).compareTo(Integer.valueOf(o2.getValue()[0]));
            // }
            // });

            consoleLogger.debug("*** iteration finished, top count "
                    + digramFrequencies.getTop().getFrequency());
        }

        rg.setR0String(stringToDisplay(string));

        rg.expandRules();

        return rg;

    }

    /**
     * Substitute the digram by a rule.
     *
     * @param currentIndex
     * @param g
     * @param string
     * @param digramFrequencies
     */
    private static void substituteDigramAt(RePairGrammar rg, int currentIndex, RePairGuard g,
                                           ArrayList<RePairSymbol> string, DigramFrequencies digramFrequencies) {

        // create entry for two new digram
        //

        final RePairSymbol digramL = string.get(currentIndex);
        final RePairSymbol digramR = string.get(currentIndex + 1);
        final char[] digramLkey = digramL.key();
        final char[] digramRkey = digramR.key();
        final StringBuffer digram = new StringBuffer(digramLkey.length+1+digramRkey.length);
        digram.append(digramL).append(SPACE).append(digramR);



    /*
    consoleLogger.debug("  substituting the digram " + digram + " at " + currentIndex + " with "
        + g.toString());

    if (currentIndex > 0) {
      consoleLogger.debug("   previous " + string.get(currentIndex - 1).toString());
    }
    if (currentIndex < string.size() - 2) {
      consoleLogger.debug("   next " + string.get(currentIndex + 2).toString());
    }
    */

        // update the new left digram frequency
        //
        if (currentIndex > 0) {
            StringBuffer newDigram = new StringBuffer();
            newDigram.append(string.get(currentIndex - 1).toString()).append(SPACE).append(g.toString());

            //consoleLogger.debug("   updating the frequency entry for digram " + newDigram.toString());

            final String nds = newDigram.toString();
            DigramFrequencyEntry entry = digramFrequencies.get(nds);
            if (null == entry) {
                digramFrequencies.put(new DigramFrequencyEntry(nds, 1, currentIndex - 1));
            } else {
                digramFrequencies.incrementFrequency(entry, 1);
                if (currentIndex - 1 < entry.getFirstOccurrence()) {
                    entry.setFirstOccurrence(currentIndex - 1);
                }
            }
        }

        // update the new right digram frequency
        //
        if (currentIndex < string.size() - 2) {
            StringBuffer newDigram = new StringBuffer();
            newDigram.append(g.toString()).append(SPACE).append(string.get(currentIndex + 2));


            //consoleLogger.debug("   updating the frequency entry for digram " + newDigram.toString());
            final String nds = newDigram.toString();
            DigramFrequencyEntry entry = digramFrequencies.get(nds);
            if (null == entry) {
                digramFrequencies.put(new DigramFrequencyEntry(nds, 1, currentIndex));
            } else {
                digramFrequencies.incrementFrequency(entry, 1);
                if (currentIndex + 1 < entry.getFirstOccurrence()) {
                    entry.setFirstOccurrence(currentIndex);
                }
            }
        }

        // remove and substitute
        //
        // 1. decrease to be substituted digram frequency
        //
        //consoleLogger.debug("   updating the frequency entry for digram " + digram.toString());
        final String ds = digram.toString();
        DigramFrequencyEntry entry = digramFrequencies.get(ds);
        if (1 == entry.getFrequency()) {
            //consoleLogger.debug("    removing the frequency entry");
            digramFrequencies.remove(ds);
        } else {
            /*consoleLogger.debug("    setting the frequency entry to "
                    + Integer.valueOf(entry.getFrequency() - 1));*/
            digramFrequencies.incrementFrequency(entry, -1);
            if (currentIndex == entry.getFirstOccurrence()) {
                //consoleLogger.debug("    this was an index entry, finding another digram index...");
                repairLRFreqMatch(currentIndex, string, digramLkey, digramRkey, entry);
            }
        }
        // 2. substitute
        string.set(currentIndex, g);
        /*consoleLogger.debug("   deleting symbol " + string.get(currentIndex + 1).toString() + " at "
                + Integer.valueOf(currentIndex + 1));*/
        // 3. delete
        string.remove(currentIndex + 1);

        // need to take care about all the indexes
        // as all the indexes above _currentIndex_ shall be shifted by -1
        // NO NEED for TLinkedList<Symbol> string = new TLinkedList<Symbol>();
        // HashMap<String, int[]> digramFrequencies = new HashMap<String, int[]>();
        //
        // traverse the string to the right decreasing indexes
        for (Entry<String, DigramFrequencyEntry> e : digramFrequencies.getEntries().entrySet()) {
            final DigramFrequencyEntry eval = e.getValue();
            int idx = eval.getFirstOccurrence();
            if (idx >= currentIndex + 2) {
                // consoleLogger.debug("   shifting entry for  " + e.getValue().getDigram() + " from "
                // + e.getValue().getFirstOccurrence() + " to " + Integer.valueOf(idx - 1));
                eval.setFirstOccurrence(idx - 1);
            }
        }

    }

    private static void repairLRFreqMatch(int currentIndex, ArrayList<RePairSymbol> string, char[] digramLkey, char[] digramRkey, DigramFrequencyEntry entry) {
        for (int i = currentIndex + 1; i < string.size() - 1; i++) {


            if (Arrays.equals(digramLkey, string.get(i).key()) &&
                    Arrays.equals(digramRkey, string.get(i + 1).key())) {

                //cDigram.setLength(0);
                //cDigram.append(string.get(i)).append(SPACE).append(string.get(i + 1));

                //consoleLogger.debug("   for digram " + cDigram.toString() + " new index " + i);
                entry.setFirstOccurrence(i);
                break;

            }
        }
    }

    private static void removeDigramFrequencyEntry(int index, ArrayList<RePairSymbol> string,
                                                   DigramFrequencies digramFrequencies) {

        StringBuffer digramToRemove = new StringBuffer();
        digramToRemove.append(string.get(index)).append(SPACE)
                .append(string.get(index + 1));

        DigramFrequencyEntry digramEntry = digramFrequencies.get(digramToRemove.toString());

        if (digramEntry.getFrequency() == 1) {
            digramFrequencies.remove(digramToRemove.toString());
            /*consoleLogger.debug("  completely removing the frequency entry for digram "
                    + digramToRemove.toString() + " at position " + index);*/
        } else {
            /*consoleLogger.debug("  decreasing the frequency entry for digram "
                    + digramToRemove.toString() + " at position " + index + " from "
                    + digramEntry.getFrequency() + " to " + Integer.valueOf(digramEntry.getFrequency() - 1));*/
            digramFrequencies.incrementFrequency(digramEntry, -1);
            if (index == digramEntry.getFirstOccurrence()) {
                //consoleLogger.debug("  this was an index entry, finding another digram index...");
                for (int i = index + 1; i < string.size() - 1; i++) {
                    StringBuffer cDigram = new StringBuffer();
                    cDigram.append(string.get(i).toString()).append(SPACE)
                            .append(string.get(i + 1).toString());
                    if (digramToRemove.toString().equals(cDigram.toString())) {
                        //consoleLogger.debug("   for digram " + cDigram.toString() + " new index " + i);
                        digramEntry.setFirstOccurrence(i);
                        break;
                    }
                }
            }
        }

    }

    private static String stringToDisplay(ArrayList<RePairSymbol> string) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < string.size(); i++) {
            sb.append(string.get(i).toString()).append(SPACE);
        }
        return sb.toString();
    }
}
