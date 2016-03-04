package net.seninp.gi.repair;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.seninp.jmotif.sax.datastructure.SAXRecords;

/**
 * Implements RePair.
 * 
 * @author psenin
 * 
 */
public final class RePairFactory {

  private static final String SPACE = " ";

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.WARN;
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
   * 
   * @return the grammar.
   */
  public static RePairGrammar buildGrammar(SAXRecords saxRecords) {

    RePairGrammar grammar = NewRepair.parse(saxRecords.getSAXString(SPACE));

    return grammar;

  }

  /**
   * Builds a grammar given a string of terminals delimeted by space.
   * 
   * @param inputString the input string.
   * @return the RePair grammar.
   */
  public static RePairGrammar buildGrammar(String inputString) {

    RePairGrammar grammar = NewRepair.parse(inputString);

    return grammar;

  }

}
