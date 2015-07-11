package net.seninp.gi.repair;

import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Implements the digram frequency queue.
 * 
 * @author psenin
 * 
 */
public class DigramFrequencies {

  /** A map of strings to digram frequencies. */
  private final LinkedHashMap<String, DigramFrequencyEntry> digramsToEntries;

  /** A map of buckets, each bucket is the frequency number pointing on the collection of entries. */
  private final IntObjectHashMap<List<DigramFrequencyEntry>> bucketsToEntries;

  /**
   * Constructor. Inits data structures.
   */
  public DigramFrequencies() {
    super();
    digramsToEntries = new LinkedHashMap();
    bucketsToEntries = new IntObjectHashMap();
  }

  /**
   * Puts the digram into collection, it overrides the old entry.
   * 
   * @param digramFrequencyEntry The digram entry.
   */
  public void put(DigramFrequencyEntry digramFrequencyEntry) {
    this.digramsToEntries.put(digramFrequencyEntry.getDigram(), digramFrequencyEntry);
    Integer freq = digramFrequencyEntry.getFrequency();
    List<DigramFrequencyEntry> bucket = this.bucketsToEntries.get(freq);
    if (null == bucket) {
      bucket = new FastList<DigramFrequencyEntry>();
      this.bucketsToEntries.put(freq, bucket);
    }
    bucket.add(digramFrequencyEntry);
  }

  /**
   * get the frequency entry by the digram string key.
   * 
   * @param string the string key.
   * @return the digram frequency entry if exists.
   */
  public DigramFrequencyEntry get(String string) {
    return this.digramsToEntries.get(string);
  }

  /**
   * Increments a frequency counter for a digram.
   * 
   * @param entry the entry.
   * @param increment the increment value.
   */
  public void incrementFrequency(DigramFrequencyEntry entry, int increment) {

    // findout the old bucket and remove this entry
    List<DigramFrequencyEntry> oldBucket = this.bucketsToEntries.get(entry.getFrequency());
    oldBucket.remove(entry);
    if (oldBucket.isEmpty()) {
      this.bucketsToEntries.remove(entry.getFrequency());
    }

    // get the increment added

    int newFreq = entry.add(increment);

    // put into the new bucket
    List<DigramFrequencyEntry> bucket = this.bucketsToEntries.get(newFreq);
    if (null == bucket) {
      bucket = new FastList(1);
      this.bucketsToEntries.put(newFreq, bucket);
    }
    bucket.add(entry);
  }

  /**
   * Gets the most frequent entry.
   * 
   * @return the most frequent entry.
   */
  public DigramFrequencyEntry getTop() {
    // System.out.println("** calling top on collection "
    // + Arrays.toString(bucketsToEntries.keySet().toArray(
    // new Integer[bucketsToEntries.keySet().size()])));
    if (bucketsToEntries.keySet().isEmpty()) {
      return null;
    }
    else {
      // by the default there are no empty buckets
      int maxBucket = bucketsToEntries.keysView().max();
      //Integer maxBucket = Collections.max(bucketsToEntries.keySet());
      return bucketsToEntries.get(maxBucket).get(0);
    }
  }

  /**
   * Removes the digram frequency entry from the collection.
   * 
   * @param digramStr the digram string.
   */
  public void remove(String digramStr) {
    // get the entry
    DigramFrequencyEntry entry = this.digramsToEntries.get(digramStr);
    if (null == entry) {
      return;
    }
    else {
      // get its frequency and the corresponding bucket
      int freq = entry.getFrequency();
      List<DigramFrequencyEntry> bucket = this.bucketsToEntries.get(freq);
      if (!bucket.remove(entry)) {
        throw (new RuntimeException("There was an error!"));
      }
      // check if the bucket left empty after deletion
      if (bucket.isEmpty()) {
        this.bucketsToEntries.remove(freq);
      }
      // now clean-up the second map
      this.digramsToEntries.remove(digramStr);
    }
    // and drop the entry itself
    entry = null;
  }

  public int size() {
    return this.digramsToEntries.size();
  }

  public HashMap<String, DigramFrequencyEntry> getEntries() {
    return digramsToEntries;
  }

}
