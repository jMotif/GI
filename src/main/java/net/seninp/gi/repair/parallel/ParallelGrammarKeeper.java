package net.seninp.gi.repair.parallel;

import com.gs.collections.api.list.MutableList;
import com.gs.collections.api.tuple.primitive.IntObjectPair;
import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;
import net.seninp.gi.repair.RePairSymbol;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This implements a handler for the Re-Pair grammar built in parallel. This data structure is
 * responsible for enumerating rules and for tracking changes in the R0 of the grammar.
 *
 * @author psenin
 */
public class ParallelGrammarKeeper {

    private static final char SPACE = ' ';
    private static final char THE_R = 'R';

    // rule 0 gets a separate treatment, so we start from 1
    //
    protected AtomicInteger numRules = new AtomicInteger(1);

    // the rules table
    protected IntObjectHashMap<ParallelRePairRule> theRules = new IntObjectHashMap();

    // the grammar id
    private long id;

    // R0 strings
    //
    protected String r0String;
    public String r0ExpandedString;

    // keeps a working string of this grammar
    //
    protected ArrayList<RePairSymbol> workString;
    private MutableList<IntObjectPair<ParallelRePairRule>> keys;

    /**
     * Constructor.
     *
     * @param id The handler id.
     */
    public ParallelGrammarKeeper(long id) {
        super();
        this.id = id;
    }

    /**
     * The id is used to keep track of parallel chunks.
     *
     * @return the current ID.
     */
    public long getId() {
        return this.id;
    }

    /**
     * This is used in parallel.
     *
     * @param string the string we work with in parallel.
     */
    public void setWorkString(ArrayList<RePairSymbol> string) {
        this.workString = string;
    }

    /**
     * Set the R0 string.
     *
     * @param string the R0 string value.
     */
    public void setR0String(String string) {
        this.r0String = string;
    }

    /**
     * Get the expanded R0 out.
     *
     * @return the expanded R0.
     */
    public String getR0ExpandedString() {
        return this.r0ExpandedString;
    }

    /**
     * This adds an existing rule to this grammar. Useful in merging.
     *
     * @param r The rule. It is not yet clear how to treat rules, be careful. This will not set the
     *          rule number, but it will increment the internal rule counter.
     */
    public void addExistingRule(ParallelRePairRule r) {
        r.grammarHandler = this;
        if (this.theRules.containsKey(r.ruleNumber)) {
            // we do override an existing rule
            theRules.put(r.ruleNumber, r);
        } else {
            // plus 1 because the rule 0 has a special treatment
            theRules.put(r.ruleNumber, r);
            numRules.set(theRules.size() + 1);
        }
    }

    /**
     * Expands all rules EXCEPT R0.
     */
    public void expandRules() {
        // iterate over all SAX containers
        //ArrayList<Integer> keys = new ArrayList<Integer>(theRules.keySet());
        //Collections.sort(keys);
        keys = keys();
        for (IntObjectPair<ParallelRePairRule> key : keys) {
            ParallelRePairRule rr = key.getTwo();


            String resultString = rr.toRuleString();

            int currentSearchStart = resultString.indexOf(THE_R);
            while (currentSearchStart >= 0) {
                int spaceIdx = resultString.indexOf(" ", currentSearchStart);
                // if (spaceIdx < 0) {
                // System.out.println("gotcha!");
                // }
                String ruleName = resultString.substring(currentSearchStart, spaceIdx + 1);
                int ruleId = Integer.valueOf(ruleName.substring(1, ruleName.length() - 1));

                ParallelRePairRule rule = theRules.get(ruleId);
                if (rule != null) {
                    if (rule.expandedRuleString.charAt(rule.expandedRuleString.length() - 1) == ' ') {
                        resultString = resultString.replaceAll(ruleName, rule.expandedRuleString);
                    } else {
                        resultString = resultString.replaceAll(ruleName, rule.expandedRuleString + SPACE);
                    }
                }

                currentSearchStart = resultString.indexOf(THE_R, spaceIdx);
            }

            rr.setExpandedRule(resultString.trim());

        }
    }

    public MutableList<IntObjectPair<ParallelRePairRule>> keys() {
        return theRules.keyValuesView().toSortedList();
    }

    /**
     * Expands R0 specifically.
     */
    public void expandR0() {
        // string is immutable it will get copied
        String finalString = this.r0String;
        int currentSearchStart = finalString.indexOf(THE_R);
        while (currentSearchStart >= 0) {

            int spaceIdx = finalString.indexOf(" ", currentSearchStart + 1);

            String ruleName = finalString.substring(currentSearchStart, spaceIdx + 1);
            Integer ruleId = Integer.valueOf(ruleName.substring(1, ruleName.length() - 1));

            ParallelRePairRule rr = theRules.get(ruleId);
            if (null == rr.expandedRuleString) {
                finalString = finalString.replaceAll(ruleName, theRules.get(ruleId).toRuleString());
            } else {
                finalString = finalString.replaceAll(ruleName, theRules.get(ruleId).expandedRuleString
                        + SPACE);
            }

            currentSearchStart = finalString.indexOf(THE_R);
        }
        this.r0ExpandedString = finalString;
    }

    public String toGrammarString() {
        StringBuffer sb = new StringBuffer();
        System.out.println("R0 -> " + r0String);
        for (int i = 1; i < theRules.size(); i++) {
            ParallelRePairRule r = theRules.get(i);
            sb.append("R").append(r.ruleNumber).append(" -> ").append(r.toRuleString()).append(" : ")
                    .append(r.expandedRuleString).append(", ").append(r.positions).append("\n");
        }
        return sb.toString();
    }

}
