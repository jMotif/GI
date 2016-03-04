package net.seninp.gi.repair;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import org.junit.Test;

public class TestRepairPriorityQueue {

  private static final int FREQ1 = 10;
  private static final int FREQ2 = 13;
  private static final int FREQ3 = 13;
  private static final int FREQ4 = 7;
  private static final int FREQ5 = 5;

  private static final String KEY1 = "aaa bbb";
  private static final String KEY2 = "bbb ccc";
  private static final String KEY3 = "ccc eee";
  private static final String KEY4 = "eee fff";
  private static final String KEY5 = "fff ggg";

  private RepairDigramRecord dr1 = new RepairDigramRecord(KEY1, FREQ1);
  private RepairDigramRecord dr2 = new RepairDigramRecord(KEY2, FREQ2);
  private RepairDigramRecord dr3 = new RepairDigramRecord(KEY3, FREQ3);
  private RepairDigramRecord dr4 = new RepairDigramRecord(KEY4, FREQ4);
  private RepairDigramRecord dr5 = new RepairDigramRecord(KEY5, FREQ5);

  @Test
  public void testGenericEnqueueDequeue() {

    RepairPriorityQueue pq = new RepairPriorityQueue();
    assertEquals("testing the enqueue & dequeue operations", 0, pq.size());
    assertNull("testing the enqueue & dequeue operations", pq.peek());

    pq.enqueue(dr1);
    assertEquals("testing the enqueue & dequeue operations", 1, pq.size());
    assertEquals("testing the enqueue & dequeue operations", dr1, pq.peek());

    pq.enqueue(dr2);
    assertEquals("testing the enqueue & dequeue operations", 2, pq.size());
    assertEquals("testing the enqueue & dequeue operations", dr2, pq.peek());

    pq.enqueue(dr3);
    assertEquals("testing the enqueue & dequeue operations", 3, pq.size());
    assertEquals("testing the enqueue & dequeue operations", dr3, pq.peek());

    pq.enqueue(dr4);
    assertEquals("testing the enqueue & dequeue operations", 4, pq.size());
    assertEquals("testing the enqueue & dequeue operations", dr3, pq.peek());

    pq.enqueue(dr5);
    assertEquals("testing the enqueue & dequeue operations", 5, pq.size());
    assertEquals("testing the enqueue & dequeue operations", dr3, pq.peek());

    try {
      RepairDigramRecord dr5Duplicate = new RepairDigramRecord(KEY5, FREQ5);
      pq.enqueue(dr5Duplicate);
      fail("Exception wasn't thrown!");
    }
    catch (IllegalArgumentException e) {
      assert true;
    }

    RepairDigramRecord el = pq.peek();
    assertSame("testing the enqueue & dequeue operations", el, dr3);
    el = pq.dequeue();
    assertSame("testing the enqueue & dequeue operations", el, dr3);
    assertEquals("testing the enqueue & dequeue operations", 4, pq.size());

    el = pq.dequeue();
    assertSame("testing the enqueue & dequeue operations", el, dr2);
    assertEquals("testing the enqueue & dequeue operations", 3, pq.size());

    el = pq.peek();
    assertSame("testing the enqueue & dequeue operations", el, dr1);
    el = pq.dequeue();
    assertSame("testing the enqueue & dequeue operations", el, dr1);
    assertEquals("testing the enqueue & dequeue operations", 2, pq.size());

    el = pq.dequeue();
    assertSame("testing the enqueue & dequeue operations", el, dr4);
    assertEquals("testing the enqueue & dequeue operations", 1, pq.size());

    el = pq.dequeue();
    el = pq.dequeue();
    assertNull("testing the enqueue & dequeue operations", el);

  }

  @Test
  public void testEnqueueFromHead() {
    RepairPriorityQueue pq = new RepairPriorityQueue();
    pq.enqueue(dr5);
    assertEquals("testing the enqueue & dequeue operations", dr5, pq.peek());
    pq.enqueue(dr4);
    assertEquals("testing the enqueue & dequeue operations", dr4, pq.peek());
    pq.enqueue(dr1);
    assertEquals("testing the enqueue & dequeue operations", dr1, pq.peek());
    pq.enqueue(dr3);
    assertEquals("testing the enqueue & dequeue operations", dr3, pq.peek());
    pq.enqueue(dr2);
    assertEquals("testing the enqueue & dequeue operations", dr2, pq.peek());
  }

  @Test
  public void testEnqueueFromTail() {
    RepairPriorityQueue pq = new RepairPriorityQueue();

    pq.enqueue(dr2);
    assertEquals("testing the enqueue & dequeue operations", dr2, pq.peek());

    pq.enqueue(dr3);
    assertEquals("testing the enqueue & dequeue operations", dr3, pq.peek());

    pq.enqueue(dr1);
    assertEquals("testing the enqueue & dequeue operations", dr3, pq.peek());
    ArrayList<RepairDigramRecord> arr = pq.toList();
    RepairDigramRecord el = arr.get(2);
    assertTrue("testing the enqueue & dequeue operations", KEY1.equalsIgnoreCase(el.getDigram()));

    pq.enqueue(dr4);
    assertEquals("testing the enqueue & dequeue operations", dr3, pq.peek());
    arr = pq.toList();
    el = arr.get(3);
    assertTrue("testing the enqueue & dequeue operations", KEY4.equalsIgnoreCase(el.getDigram()));

    pq.enqueue(dr5);
    assertEquals("testing the enqueue & dequeue operations", dr3, pq.peek());
    arr = pq.toList();
    el = arr.get(4);
    assertTrue("testing the enqueue & dequeue operations", KEY5.equalsIgnoreCase(el.getDigram()));
  }

  @Test
  public void testPriorityQueueUpdate() {

    // create the priority queue using the dqtq
    //
    RepairPriorityQueue pq = new RepairPriorityQueue();
    pq.enqueue(dr1);
    //
    // test trivial update
    RepairDigramRecord el = pq.updateDigramFrequency(KEY1, FREQ1);
    assertEquals("testing queue sorting", el.getFrequency(), FREQ1);

    el = pq.updateDigramFrequency(KEY1, 75);
    assertEquals("testing queue sorting", el.getFrequency(), 75);
    pq.updateDigramFrequency(KEY1, FREQ1);
    //
    pq.enqueue(dr2);
    pq.enqueue(dr3);
    pq.enqueue(dr4);
    pq.enqueue(dr5);

    // element retrieval test
    //
    el = pq.get(KEY3);
    assertSame("testing queue sorting", el, dr3);
    assertNull(pq.get("zhaba baba"));

    // check the trivial update
    //
    el = pq.updateDigramFrequency(KEY3, FREQ3);
    assertSame("testing queue sorting", el, dr3);

    // make the head hit the ceiling
    //
    el = pq.peek();
    assertSame("testing queue sorting", el, dr3);
    el = pq.updateDigramFrequency(KEY3, Integer.MAX_VALUE);

    el = pq.peek();
    assertSame("testing queue sorting", el, dr3);
    el = pq.updateDigramFrequency(KEY3, FREQ3);

    // element with KEY3 goes to head
    //
    el = pq.get(KEY3);
    // System.out.println(pq);
    pq.updateDigramFrequency(el.getDigram(), 18);
    el = pq.peek();
    assertTrue("testing the enqueue & dequeue operations", KEY3.equalsIgnoreCase(el.getDigram()));
    assertEquals("testing the enqueue & dequeue operations", 18, el.getFrequency());
    // System.out.println(pq);

    // element with KEY1 goes to tail
    //
    // System.out.println(pq);
    el = pq.get(KEY1);
    pq.updateDigramFrequency(el.getDigram(), 3);
    ArrayList<RepairDigramRecord> arr = pq.toList();
    el = arr.get(arr.size() - 1);
    assertTrue("testing the enqueue & dequeue operations", KEY1.equalsIgnoreCase(el.getDigram()));
    assertEquals("testing the enqueue & dequeue operations", 3, el.getFrequency());
    // System.out.println(pq);

    // element with KEY5 (fff ggg) go two places up
    //
    // System.out.println(pq);
    el = pq.get(KEY5);
    pq.updateDigramFrequency(el.getDigram(), 17);
    arr = pq.toList();
    el = arr.get(1);
    assertTrue("testing the enqueue & dequeue operations", KEY5.equalsIgnoreCase(el.getDigram()));
    assertEquals("testing the enqueue & dequeue operations", 17, el.getFrequency());
    // System.out.println(pq);

    // element with KEY5 (fff ggg) go two places down
    //
    // System.out.println(pq);
    el = pq.get(KEY5);
    pq.updateDigramFrequency(el.getDigram(), 4);
    arr = pq.toList();
    el = arr.get(3);
    assertTrue("testing the enqueue & dequeue operations", KEY5.equalsIgnoreCase(el.getDigram()));
    assertEquals("testing the enqueue & dequeue operations", 4, el.getFrequency());
    // System.out.println(pq);

    // move a head element down
    //
    el = pq.get(KEY3);
    pq.updateDigramFrequency(el.getDigram(), 3);
    arr = pq.toList();
    el = arr.get(4);
    assertTrue("testing the enqueue & dequeue operations", KEY3.equalsIgnoreCase(el.getDigram()));
    assertEquals("testing the enqueue & dequeue operations", 3, el.getFrequency());
    // System.out.println(pq);

    // move a tail element up
    //
    el = pq.get(KEY1);
    pq.updateDigramFrequency(el.getDigram(), 12);
    arr = pq.toList();
    el = arr.get(1);
    assertTrue("testing the enqueue & dequeue operations", KEY1.equalsIgnoreCase(el.getDigram()));
    assertEquals("testing the enqueue & dequeue operations", 12, el.getFrequency());
    // System.out.println(pq);

    // check that KEY1 gets evicted
    //
    el = pq.get(KEY1);
    el = pq.updateDigramFrequency(el.getDigram(), 0);
    assertNull("testing the enqueue & dequeue operations", el);
    assertNull("testing the enqueue & dequeue operations", pq.get(KEY1));
    assertEquals("testing the enqueue & dequeue operations", 4, pq.size());
    // System.out.println(pq);

  }

}
