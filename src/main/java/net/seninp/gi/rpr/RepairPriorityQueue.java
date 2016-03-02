package net.seninp.gi.rpr;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implements the priority queue for RePair. Backed by the doubly linked list of custom nodes.
 * 
 * @author psenin
 *
 */
public class RepairPriorityQueue {

  // the head pointer
  private RepairQueueNode head = null;

  // the "quick" pointers <digram string> -> <node>
  private HashMap<String, RepairQueueNode> elements = new HashMap<String, RepairQueueNode>();

  /**
   * Places an element in the queue at the place based on its frequency.
   * 
   * @param digramRecord the digram record to place into.
   */
  public void enqueue(RepairDigramRecord digramRecord) {
    if (elements.containsKey(digramRecord.str)) {
      throw new IllegalArgumentException(
          "Element with payload " + digramRecord.str + " already exists in the queue...");
    }
    else {
      // create a new node
      RepairQueueNode nn = new RepairQueueNode(digramRecord);
      // place it into the queue
      if (this.elements.isEmpty()) {
        this.head = nn;
      }
      else {
        RepairQueueNode hp = head;
        while (null != hp) {
          // see how it fits with head...
          if (nn.getFrequency() > hp.getFrequency()) {
            // its going to be new hp ...
            if (null == hp.prev) {
              // i.e., head
              this.head = nn;
              hp.prev = nn;
              nn.next = hp;
            }
            else {
              RepairQueueNode php = hp.prev;
              php.next = nn;
              nn.prev = php;
              hp.prev = nn;
              nn.next = hp;
            }
            break; // the element has been placed
          }
          else {
            if (null == hp.next) {
              // we hit the tail... going to be the last element
              hp.next = nn;
              nn.prev = hp;
              hp = nn;
            }
            hp = hp.next;
          }
        }
      }
      // also save the element in the index store
      this.elements.put(nn.payload.str, nn);
    }
  }

  /**
   * Returns the most frequently seen element -- the head of the queue.
   * 
   * @return
   */
  public RepairDigramRecord dequeue() {
    if (null != this.head) {
      RepairDigramRecord el = this.head.payload;
      this.head = this.head.next;
      if (null != this.head) {
        this.head.prev = null;
      }
      this.elements.remove(el.str);
      return el;
    }
    return null;
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
   * Peaks onto the head element (doesn't remove it).
   * 
   * @return the head element pointer.
   */
  public RepairDigramRecord peek() {
    if (null != this.head) {
      return this.head.payload;
    }
    return null;
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
    if (null != el) {
      return el.payload;
    }
    return null;
  }

  /**
   * Updates the priority queue according to the change...
   * 
   * @param digram the digram string.
   * @param newFreq new frequency.
   * 
   * @return the pointer onto updated element.
   */
  public RepairDigramRecord updateDigramFrequency(String digram, int newFreq) {
    // if the key exists
    if (this.elements.containsKey(digram)) {

      // point to that node
      RepairQueueNode el = elements.get(digram);

      if (newFreq < 2) {
        // evict the element
        removeNodeFromList(el);
        this.elements.remove(el.payload.str);
        return null;
      }

      // if we need to update frequency
      if (el.payload.freq != newFreq) {

        // update the frequency
        int oldFreq = el.payload.freq;
        el.payload.freq = newFreq;

        // if the list is just too short
        if (1 == this.elements.size()) {
          return el.payload;
        }

        // if we have to push the element up in the list
        if (newFreq > oldFreq) {

          // going up here
          RepairQueueNode cp = el.prev;
          while (null != cp && el.payload.freq > cp.payload.freq) {
            cp = cp.prev;
          }

          if (null == cp) { // we hit the head
            removeNodeFromList(el);
            el.prev = null;
            el.next = this.head;
            this.head.prev = el;
            this.head = el;
          }
          else { // place element just behind of cp
            removeNodeFromList(el);
            cp.next.prev = el;
            el.next = cp.next;
            cp.next = el;
            el.prev = cp;
          }
        }
        else {
          // going down..
          RepairQueueNode cp = el;
          while (null != cp.next && el.payload.freq < cp.next.payload.freq) {
            cp = cp.next;
          }

          if (null == cp.next) { // we hit the tail
            removeNodeFromList(el);
            el.prev = cp;
            el.next = null;
            cp.next = el;
          }
          else { // place element just behind of cp
            removeNodeFromList(el);
            cp.next.prev = el;
            el.next = cp.next;
            cp.next = el;
            el.prev = cp;
          }
        }
      }
      return el.payload;
    }
    return null;
  }

  /**
   * Needed this for debug purpose -- translates the doubly linked list into an array list.
   * 
   * @return an array list (sorted by priority) of elements (live copy).
   */
  public ArrayList<RepairDigramRecord> toList() {
    ArrayList<RepairDigramRecord> res = new ArrayList<RepairDigramRecord>(this.elements.size());
    RepairQueueNode cp = this.head;
    while (null != cp) {
      res.add(cp.payload);
      cp = cp.next;
    }
    return res;
  }

  /**
   * Removes a node from the doubly linked list which backs the queue.
   * 
   * @param el the element pointer.
   */
  private void removeNodeFromList(RepairQueueNode el) {
    // the head case
    //
    if (null == el.prev) {
      if (null != el.next) {
        this.head = el.next;
        el.next.prev = null;
      }
      else {
        // can't happen? yep. if there is only one element exists...
        this.head = null;
      }
    }
    // the tail case
    //
    else if (null == el.next) {
      if (null != el.prev) {
        el.prev.next = null;
      }
      else {
        // can't happen?
        throw new RuntimeException("Unrecognized situation here...");
      }
    }
    // all others
    //
    else {
      el.prev.next = el.next;
      el.next.prev = el.prev;
    }

  }

  /*
   * (non-Javadoc) Debug message.
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuffer sb = new StringBuffer("priority queue of ").append(this.elements.size())
        .append(" nodes:\n");
    RepairQueueNode hp = this.head;
    int nodeCounter = 0;
    while (null != hp) {
      sb.append(nodeCounter).append(": ").append(hp.payload.str).append(", ")
          .append(hp.payload.freq).append("\n");
      hp = hp.next;
      nodeCounter++;
    }
    return sb.delete(sb.length() - 1, sb.length()).toString();
  }

  /**
   * Implements the repair queue node.
   * 
   * @author psenin
   *
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
    public int getFrequency() {
      return this.payload.freq;
    }

  }
}
