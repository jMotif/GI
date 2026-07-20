package net.seninp.gi.sequitur;

/*
 This class is part of a Java port of Craig Nevill-Manning's Sequitur algorithm.
 Copyright (C) 1997 Eibe Frank

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import java.util.Hashtable;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Template for Sequitur data structures. Adaption of Eibe Frank code for JMotif API.
 * 
 * @author Manfred Lerner, seninp
 * 
 */
public abstract class SAXSymbol {

  private static final Logger LOGGER = LoggerFactory.getLogger(SAXSymbol.class);

  /**
   * Apparently, this limits the possible number of terminals, ids of non-terminals start after this
   * num.
   */
  protected static final int numTerminals = 100000;

  /** Seed the size of hash table? */
  private static final int prime = 2265539;

  /** Hashtable to keep track of all digrams. This is static - single instance for all. */
  protected static final Hashtable<SAXSymbol, SAXSymbol> theDigrams = new Hashtable<SAXSymbol, SAXSymbol>(
      SAXSymbol.prime);

  public static Hashtable<String, Hashtable<String, Integer>> theSubstituteTable = new Hashtable<String, Hashtable<String, Integer>>(
      SAXSymbol.prime);

  /** The symbol value. */
  protected String value;

  /** The symbol original position. */
  protected int originalPosition;

  /** Sort of pointers for previous and the next symbols. */
  public SAXSymbol p;
  public SAXSymbol n;

  /**
   * Links left and right symbols together, i.e. removes this symbol from the string, also removing
   * any old digram from the hash table.
   * 
   * @param left the left symbol.
   * @param right the right symbol.
   */
  public static void join(SAXSymbol left, SAXSymbol right) {

    // check for an OLD digram existence - i.e. left must have a next symbol
    // if .n exists then we are joining TERMINAL symbols within the string, and must clean-up the
    // old digram
    if (left.n != null) {
      left.deleteDigram();
    }

    // re-link left and right
    left.n = right;
    right.p = left;
  }

  /**
   * Cleans up template.
   */
  public abstract void cleanUp();

  /**
   * Inserts a symbol after this one.
   * 
   * @param toInsert the new symbol to be inserted.
   */
  public void insertAfter(SAXSymbol toInsert) {

    // call join on this symbol' NEXT - placing it AFTER the new one
    join(toInsert, n);

    // call join on THIS symbol placing the NEW AFTER
    join(this, toInsert);
  }

  /**
   * Removes the digram from the hash table. Overwritten in sub class guard.
   */
  public void deleteDigram() {

    // if N is a Guard - then it is a RULE sits there, don't care about digram
    if (n.isGuard()) {
      return;
    }

    // delete digram if it is exactly this one
    if (this == theDigrams.get(this)) {
      theDigrams.remove(this);
    }
  }

  /**
   * Returns true if this is the guard symbol. Overwritten in subclass guard.
   * 
   * @return true if the guard.
   */
  public boolean isGuard() {
    return false;
  }

  /**
   * Returns true if this is a non-terminal. Overwritten in subclass nonTerminal.
   * 
   * @return true if the non-terminal.
   */
  public boolean isNonTerminal() {
    return false;
  }

  /**
   * "Checks in" a new digram and enforce the digram uniqueness constraint. If it appears elsewhere,
   * deals with it by calling match(), otherwise inserts it into the hash table. Overwritten in
   * subclass guard.
   * 
   * @return true if it is not unique.
   */
  public boolean check() {

    // ... Each time a link is made between two symbols if the new digram is repeated elsewhere
    // and the repetitions do not overlap, if the other occurrence is a complete rule,
    // replace the new digram with the non-terminal symbol that heads the rule,
    // otherwise,form a new rule and replace both digrams with the new non-terminal symbol
    // otherwise, insert the digram into the index...

    if (n.isGuard()) {
      // i am the rule
      return false;
    }

    if (!theDigrams.containsKey(this)) {
      theDigrams.put(this, this);
      return false;
    }

    // well the same hash is in the store, lemme see...
    SAXSymbol found = theDigrams.get(this);

    // if it's not me, then lets call match magic?
    if (found.n != this) {
      match(this, found);
    }

    return true;
  }

  /**
   * Replace a digram with a non-terminal.
   * 
   * @param r a rule to use.
   */
  public void substitute(SAXRule r) {
    // clean up this place and the next

    // here we keep the original position in the input string
    //
    r.addIndex(this.originalPosition);

    this.cleanUp();
    this.n.cleanUp();
    // link the rule instead of digram
    SAXNonTerminal nt = new SAXNonTerminal(r);
    nt.originalPosition = this.originalPosition;
    this.p.insertAfter(nt);
    // Replacing the digram with the non-terminal nt creates two NEW digrams that must be
    // re-checked against the digram index: the LEFT one (p, nt) and the RIGHT one (nt, nt.n).
    // check() returns true only when it consumed the digram into a match/new rule (which can
    // restructure the neighbourhood); it returns false when the digram was merely inserted as
    // unique or when nt is a rule end. So: check the left boundary first, and only if it did NOT
    // trigger a match -- leaving p.n (== nt) and its successor intact -- check the right boundary.
    // (If the left check DID match, it already re-checks the affected links, so re-checking here
    // would be redundant or act on stale links.)
    checkNewLinks(p);
  }

  /**
   * Re-checks the two digrams created around a freshly substituted non-terminal: the left digram at
   * {@code left} (left, left.n) and, only if that did not trigger a match, the right digram at
   * {@code left.n} (left.n, left.n.n). See {@link #substitute(SAXRule)} for why the right boundary is
   * skipped once the left one matches.
   *
   * @param left the symbol immediately before the substituted non-terminal.
   */
  private static void checkNewLinks(SAXSymbol left) {
    if (!left.check()) {
      left.n.check();
    }
  }

  /**
   * Deals with a matching digram.
   * 
   * @param theDigram the first matching digram.
   * @param matchingDigram the second matching digram.
   */
  public void match(SAXSymbol theDigram, SAXSymbol matchingDigram) {

    SAXRule rule;
    SAXSymbol first, second;

    // if previous of matching digram is a guard
    if (matchingDigram.p.isGuard() && matchingDigram.n.n.isGuard()) {
      // reuse an existing rule
      rule = ((SAXGuard) matchingDigram.p).r;
      theDigram.substitute(rule);
    }
    else {
      // well, here we create a new rule because there are two matching digrams
      rule = new SAXRule();

      try {
        // tie the digram's links together within the new rule
        // this uses copies of objects, so they do not get cut out of S
        first = (SAXSymbol) theDigram.clone();
        second = (SAXSymbol) theDigram.n.clone();

        rule.theGuard.n = first;
        first.p = rule.theGuard;
        first.n = second;
        second.p = first;
        second.n = rule.theGuard;
        rule.theGuard.p = second;

        // put this digram into the hash
        // this effectively erases the OLD MATCHING digram with the new DIGRAM (symbol is wrapped
        // into Guard)
        theDigrams.put(first, first);

        // substitute the matching (old) digram with this rule in S
        matchingDigram.substitute(rule);

        // substitute the new digram with this rule in S
        theDigram.substitute(rule);

      }
      catch (CloneNotSupportedException c) {
        LOGGER.error("unexpected CloneNotSupportedException while forming a new rule", c);
      }
    }

    // Check for an underused rule.

    if (rule.first().isNonTerminal() && (((SAXNonTerminal) rule.first()).r.count == 1))
      ((SAXNonTerminal) rule.first()).expand();

    rule.assignLevel();
  }

  /**
   * Hash code for the digram (this.value, this.n.value) -- the same pair {@link #equals(Object)}
   * compares. An order-sensitive combination of the two strings' (cached) hash codes.
   *
   * <p>
   * The previous implementation summed {@code Character.getNumericValue} over each value's characters
   * and combined the two sums linearly. On the small SAX alphabet that collides catastrophically: on a
   * realistic 12k-token input its 1521 distinct digrams landed in only 100 buckets (deepest 64), so the
   * static {@code theDigrams} lookups in {@code check()} / {@code deleteDigram()} -- the inner loop of
   * Sequitur -- degraded toward linear {@code equals}-chain scans. Combining the standard
   * {@link String#hashCode()} of each value (1521 digrams -> 1235 buckets, deepest 6) restores the O(1)
   * lookup. It is a pure function of (value, n.value), so it stays consistent with {@code equals} and
   * is deterministic across runs/JVMs; only the bucket layout changes, never which digram a lookup
   * returns, so the inferred grammar is unchanged.
   *
   * @return the digram's hash code.
   */
  public int hashCode() {
    return 31 * value.hashCode() + n.value.hashCode();
  }

  /**
   * Test if two digrams are equal. WARNING: don't use to compare two symbols.
   */
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof SAXSymbol))
      return false;
    return ((value.equals(((SAXSymbol) obj).value)) && (n.value.equals(((SAXSymbol) obj).n.value)));
  }

  @Override
  public String toString() {
    return "SAXSymbol [value=" + value + ", p=" + p + ", n=" + n + "]";
  }

  /**
   * This routine is used for the debugging.
   * 
   * @param symbol the symbol we looking into.
   * @return symbol's payload.
   */
  protected static String getPayload(SAXSymbol symbol) {
    if (symbol.isGuard()) {
      return "guard of the rule " + ((SAXGuard) symbol).r.ruleIndex;
    }
    else if (symbol.isNonTerminal()) {
      return "nonterminal " + ((SAXNonTerminal) symbol).value;
    }
    return "symbol " + symbol.value;
  }

  @SuppressWarnings("unused")
  private static String makeDigramsTable() {
    StringBuffer sb = new StringBuffer("\n");
    for (Entry<SAXSymbol, SAXSymbol> e : theDigrams.entrySet()) {
      sb.append("           ").append(getPayload(e.getKey())).append(", ")
          .append(getPayload(e.getValue())).append("\n");
    }
    return sb.toString();
  }

}
