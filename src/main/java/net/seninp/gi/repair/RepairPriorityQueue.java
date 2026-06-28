package net.seninp.gi.repair;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implements the priority queue for RePair.
 *
 * <p>
 * Bucketed (count-indexed) design (Larsson-Moffat): {@code buckets[f]} heads a doubly-linked list of
 * every digram whose frequency is exactly {@code f}, and {@code maxCount} tracks the highest non-empty
 * bucket. Max-select ({@link #dequeue()} / {@link #peek()}) is an array index plus a downward scan past
 * empty buckets; every +/-1 frequency change is an O(1) unlink + push-front. This replaces the previous
 * frequency-sorted single linked list whose enqueue / updateDigramFrequency did O(Q) linear band-walks.
 *
 * <p>
 * Order discipline: nodes are pushed to the FRONT of their bucket. This reproduces the previous sorted
 * list's LIFO-within-frequency-band order exactly for the seed pass (pure enqueues), and for every
 * frequency change that occurs on real RePair-over-SAX input (always a demotion that the old list also
 * placed at the band front), so the dequeue order -- and the rule numbering that depends on it -- is
 * unchanged. (Note: the old list's positional insertion-sort could land a demoted node at the BACK of a
 * tail band in degenerate dense single-symbol alphabets; that case does not arise on SAX strings.)
 *
 * @author psenin
 *
 */
public class RepairPriorityQueue {

  // Frequencies at or above this go into a single shared "overflow" bucket (index OVERFLOW) rather
  // than their own array slot, so a pathological frequency (e.g. Integer.MAX_VALUE) cannot blow up the
  // bucket array. Real RePair frequencies are bounded by the token count, so the overflow bucket holds
  // at most a handful of entries in practice and the O(1) behaviour is preserved on real workloads.
  private static final int MAX_BUCKET = 1 << 20; // 1,048,576 distinct exact-frequency buckets
  private static final int OVERFLOW = MAX_BUCKET; // the (single) overflow bucket index

  // buckets[f] -> head of the doubly-linked list of nodes with frequency exactly f
  // (buckets[OVERFLOW] holds, unordered, every node whose frequency >= MAX_BUCKET).
  private ArrayList<RepairQueueNode> buckets = new ArrayList<RepairQueueNode>();

  // highest non-empty bucket index (a hint, scanned downward on dequeue/peek)
  private int maxCount = 0;

  // the "quick" pointers <digram string> -> <node>
  private HashMap<String, RepairQueueNode> elements = new HashMap<String, RepairQueueNode>();

  // the bucket index a frequency maps to (capped to the overflow bucket)
  private static int bucketOf(int freq) {
    return (freq >= MAX_BUCKET) ? OVERFLOW : freq;
  }

  // grow buckets so index b is valid
  private void ensureCapacity(int b) {
    while (b >= this.buckets.size()) {
      this.buckets.add(null);
    }
  }

  // push a node to the FRONT of its frequency bucket
  private void pushFront(RepairQueueNode node, int f) {
    int b = bucketOf(f);
    ensureCapacity(b);
    RepairQueueNode h = this.buckets.get(b);
    node.prev = null;
    node.next = h;
    if (null != h) {
      h.prev = node;
    }
    this.buckets.set(b, node);
    if (b > this.maxCount) {
      this.maxCount = b;
    }
  }

  // detach a node from its bucket list (leaves the elements map alone)
  private void unlink(RepairQueueNode node) {
    int b = bucketOf(node.payload.freq);
    if (null != node.prev) {
      node.prev.next = node.next;
    }
    else if (b >= 0 && b < this.buckets.size() && this.buckets.get(b) == node) {
      this.buckets.set(b, node.next);
    }
    if (null != node.next) {
      node.next.prev = node.prev;
    }
    node.prev = null;
    node.next = null;
  }

  // scan maxCount downward to the highest-priority node (or null). For a normal bucket the head IS the
  // highest (all share the same frequency); for the OVERFLOW bucket -- entries with assorted freqs
  // >= MAX_BUCKET, which real RePair never produces -- pick the genuine max by a linear scan.
  private RepairQueueNode topNode() {
    while (this.maxCount > 0
        && (this.maxCount >= this.buckets.size() || null == this.buckets.get(this.maxCount))) {
      this.maxCount--;
    }
    if (this.maxCount <= 0 || null == this.buckets.get(this.maxCount)) {
      return null;
    }
    RepairQueueNode head = this.buckets.get(this.maxCount);
    if (this.maxCount != OVERFLOW) {
      return head;
    }
    RepairQueueNode best = head;
    for (RepairQueueNode cp = head.next; null != cp; cp = cp.next) {
      if (cp.payload.freq > best.payload.freq) {
        best = cp;
      }
    }
    return best;
  }

  /**
   * Places an element in the queue based on its frequency.
   *
   * @param digramRecord the digram record to place into.
   */
  public void enqueue(RepairDigramRecord digramRecord) {
    if (elements.containsKey(digramRecord.str)) {
      throw new IllegalArgumentException(
          "Element with payload " + digramRecord.str + " already exists in the queue...");
    }
    RepairQueueNode nn = new RepairQueueNode(digramRecord);
    pushFront(nn, digramRecord.freq);
    this.elements.put(nn.payload.str, nn);
  }

  /**
   * Returns the most frequently seen element -- the head of the highest non-empty bucket.
   *
   * @return the digram record from the top of the queue or a null.
   */
  public RepairDigramRecord dequeue() {
    RepairQueueNode node = topNode();
    if (null == node) {
      return null;
    }
    RepairDigramRecord el = node.payload;
    unlink(node); // handles both the normal-bucket head and an interior OVERFLOW-bucket node
    this.elements.remove(el.str);
    return el;
  }

  /**
   * Returns the queue size.
   *
   * @return the number of elements in the queue.
   */
  public int size() {
    return this.elements.size();
  }

  /**
   * Peeks at the head element (doesn't remove it).
   *
   * @return the head element pointer.
   */
  public RepairDigramRecord peek() {
    RepairQueueNode node = topNode();
    return (null == node) ? null : node.payload;
  }

  /**
   * Checks if a digram is in the queue.
   *
   * @param digramStr the digram string.
   * @return true if it is present in the queue.
   */
  public boolean containsDigram(String digramStr) {
    return this.elements.containsKey(digramStr);
  }

  /**
   * Gets an element in the queue given its key.
   *
   * @param key the key to look for.
   * @return the element which corresponds to the key or null.
   */
  public RepairDigramRecord get(String key) {
    RepairQueueNode el = this.elements.get(key);
    return (null != el) ? el.payload : null;
  }

  /**
   * Updates the priority queue according to a digram's frequency change. Below frequency 2 the digram
   * is evicted (it can no longer found a rule).
   *
   * @param digram the digram string.
   * @param newFreq new frequency.
   * @return the pointer onto the updated element, or null if evicted / absent.
   */
  public RepairDigramRecord updateDigramFrequency(String digram, int newFreq) {

    RepairQueueNode alteredNode = this.elements.get(digram);
    if (null == alteredNode) {
      return null;
    }

    // trivial case
    if (newFreq == alteredNode.payload.freq) {
      return alteredNode.payload;
    }

    // evict if the frequency dropped below 2
    if (2 > newFreq) {
      unlink(alteredNode);
      this.elements.remove(alteredNode.payload.str);
      return null;
    }

    // relocate: unlink from the old bucket, set the new frequency, push-front into the new bucket
    // (the elements-map entry is preserved throughout).
    unlink(alteredNode);
    alteredNode.payload.freq = newFreq;
    pushFront(alteredNode, newFreq);
    return alteredNode.payload;
  }

  /**
   * Translates the queue into an array list, highest frequency first (debug / inspection).
   *
   * @return an array list (sorted by priority) of elements (live copy).
   */
  public ArrayList<RepairDigramRecord> toList() {
    ArrayList<RepairDigramRecord> res = new ArrayList<RepairDigramRecord>(this.elements.size());
    int top = Math.min(this.maxCount, this.buckets.size() - 1);
    for (int f = top; f >= 0; f--) {
      for (RepairQueueNode cp = this.buckets.get(f); null != cp; cp = cp.next) {
        res.add(cp.payload);
      }
    }
    return res;
  }

  /*
   * (non-Javadoc) Debug message.
   *
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuffer sb = new StringBuffer("priority queue of ").append(this.elements.size())
        .append(" nodes:\n");
    int nodeCounter = 0;
    int top = Math.min(this.maxCount, this.buckets.size() - 1);
    for (int f = top; f >= 0; f--) {
      for (RepairQueueNode hp = this.buckets.get(f); null != hp; hp = hp.next) {
        sb.append(nodeCounter++).append(": ").append(hp.payload.str).append(" : ")
            .append(hp.payload.freq).append("\n");
      }
    }
    return sb.toString();
  }

  /**
   * The queue node wrapping a digram record in the bucket doubly-linked list.
   */
  private class RepairQueueNode {
    // a pointer onto previous node
    protected RepairQueueNode prev = null;
    // a pointer onto the next node
    protected RepairQueueNode next = null;
    // the node payload
    protected RepairDigramRecord payload = null;

    /**
     * Constructor.
     *
     * @param digramRecord the payload to wrap.
     */
    public RepairQueueNode(RepairDigramRecord digramRecord) {
      this.payload = digramRecord;
    }

    /**
     * The occurrence frequency getter.
     *
     * @return the digram occurrence frequency.
     */
    @SuppressWarnings("unused")
    public int getFrequency() {
      return this.payload.freq;
    }

    private RepairPriorityQueue getOuterType() {
      return RepairPriorityQueue.this;
    }
  }

}
