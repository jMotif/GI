## Sequitur and RePair Grammatical Inference for time series pattern mining
[![build](https://github.com/jMotif/GI/actions/workflows/maven.yml/badge.svg)](https://github.com/jMotif/GI/actions/workflows/maven.yml)
[![Maven Central](https://img.shields.io/maven-central/v/net.seninp/jmotif-gi)](https://central.sonatype.com/artifact/net.seninp/jmotif-gi)
[![coverage](https://codecov.io/github/jMotif/GI/graph/badge.svg?branch=master)](https://codecov.io/github/jMotif/GI?branch=master)
[![License](https://img.shields.io/github/license/jMotif/GI)](https://www.gnu.org/licenses/gpl-2.0.html)


Implements Sequitur (online) and RePair (off-line) grammar induction algorithms for [Grammarviz 2.0](https://github.com/GrammarViz2/grammarviz2_site) and [SAX-VSM-G](https://github.com/seninp/sax-vsm-g). This code is released under [GPL v.2.0](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html).

Cross-language RePair checks (decompression round-trip and the tie-free paper example) live in [jmotif-conformance](https://github.com/jMotif/jmotif-conformance) alongside the SAX stack tests.

### More about implemented algorithms:
[1] Nevill-Manning, C.G. and Witten, I.H., [*"Identifying Hierarchical Structure in Sequences: A linear-time algorithm"*](https://doi.org/10.1613/jair.374), Journal of Artificial Intelligence Research, 7, 67-82, (1997).

[2] Larsson, N.J.; Moffat, A., [*"Offline dictionary-based compression"*](http://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=755679&isnumber=16375), Data Compression Conference, 1999. Proceedings. DCC '99 , vol., no., pp.296,305, 29-31 Mar 1999.

### Citing this work:
If you are using this implementation for your academic work, please cite our [Grammarviz 2.0 paper](http://link.springer.com/chapter/10.1007/978-3-662-44845-8_37):

[Citation] Senin, P., Lin, J., Wang, X., Oates, T., Gandhi, S., Boedihardjo, A.P., Chen, C., Frankenstein, S., Lerner, M.,  [*GrammarViz 2.0: a tool for grammar-based pattern discovery in time series*](http://www2.hawaii.edu/~senin/assets/papers/grammarviz2.pdf), ECML/PKDD Conference, 2014.

1.0 Building
------------
Requires **JDK 21+**. The code is written in Java and uses Maven to build it:
	
	$ mvn package
	[INFO] Scanning for projects...
	[INFO] ------------------------------------------------------------------------
  	[INFO] Building jmotif-gi
	[INFO]    task-segment: [package]
  	...
	[INFO] Building jar: target/jmotif-gi-2.0.1.jar
	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESSFUL
	[INFO] ------------------------------------------------------------------------

For a runnable jar with dependencies:

	$ mvn package -P single
  
2.0 Sequitur API use
------------
Following the [original Eibe Frank's java implementation](https://github.com/craignm/sequitur) the code is built using global (static) variables:

	String TEST3_STRING = "a b a b c a b c d a b c d e a b c d e f";
  
	SAXRule r = SequiturFactory.runSequitur(TEST3_STRING);

	System.out.println(SAXRule.printRules());

which prints the following output:

	Number	Name	Level	Occurr.	Usage	Yield	Rule str	Expaneded	Indexes
	0	R0	0	0	0	0	R1 R2 R3 R4 R4 f 	a b a b c a b c d a b c d e a b c d e f	[]
	1	R1	1	5	2	2	a b 	a b 	[0, 2, 5, 9, 14]
	2	R2	1	4	2	3	R1 c 	a b c 	[2, 5, 9, 14]
	3	R3	1	3	2	4	R2 d 	a b c d 	[5, 9, 14]
	4	R4	1	2	2	5	R3 e 	a b c d e 	[9, 14]
 
My own addition allows to retrieve the Sequitur rules as an iterable collection of [GrammarRuleRecords](https://github.com/jMotif/GI/blob/master/src/main/java/net/seninp/gi/logic/GrammarRuleRecord.java) and to map them back to the discretized time series:

	GrammarRules rules = r.toGrammarRulesData();
	GrammarRuleRecord rec = rules.get(4);
	ArrayList<RuleInterval> intervals = rec.getRuleIntervals();
	...
  

3.0 RePair API use
------------
I've implemented RePair from scratch and it uses the same [GrammarRules](https://github.com/jMotif/GI/blob/master/src/main/java/net/seninp/gi/logic/GrammarRules.java) / [GrammarRuleRecord](https://github.com/jMotif/GI/blob/master/src/main/java/net/seninp/gi/logic/GrammarRuleRecord.java) data structures as for Sequitur, so it can be plugged into Grammarviz seamlessly: 

	String TEST_STRING = "abc abc cba cba bac xxx abc abc cba cba bac";
	
	RePairGrammar rg = RePairFactory.buildGrammar(TEST_STRING);
	
	System.out.println(rg.toGrammarRules());
	
which yields: 	

	R0 -> R4 xxx R4 
        R1 -> abc cba  : abc cba, [1, 5]
        R2 -> abc R1  : abc abc cba, [0, 4]

Thanks to the algorithm's design, I was able to parallelize RePair. However, the cost of inter-thread communications and synchronization was the major showstopper, so the current *new* implementation (>0.8.5) is single-threaded (but you can still get the parallel one -- it is tagged "old_repair" in the version control).


4.0 Performance comparison
------------
The two implemented GI algorithms, Sequitur and RePair, demonstrate somewhat similar performance with minor differences. Specifically: 
 -   Sequitur implementation is slower than RePair
 -   Sequitur tends to produce more rules, but Sequitur rules are less frequent than RePair rules
 -   Sequitur rule-corresponding subsequences vary in length more
 -   Sequitur rules usually cover more points than RePair
 -   Sequitur rule coverage depth is lower than that of RePair

All these may affect the performance of the upstream time series analysis algorithms such as SAX-VSM-G, Grammarviz, and RRA. Here is the table with some numbers collected by running Sequitur and RePair using sliding window of size 150, PAA 6, and the alphabet 4. I used the EXACT numerosity reduction in these runs.

<table><tr><th rowspan="2">Dataset</th><th rowspan="2">Size</th><th colspan="4">Sequitur</th><th colspan="4">RePair</th></tr><tr><td>rules</td><td>time</td><td>cov.dpth</td><td>max.freq.</td><td>rules</td><td>time</td><td>cov.dpth</td><td>max.freq.</td></tr><tr><td>Daily commute</td><td>17175</td><td>292</td><td>8</td><td>12.8</td><td>45</td><td>362</td><td>4</td><td>18.3</td><td>53</td></tr><tr><td>Dutch power demand</td><td>35040</td><td>916</td><td>38</td><td>26.6</td><td>124</td><td>769</td><td>14</td><td>29.6</td><td>162</td></tr><tr><td>ECG 0606</td><td>2300</td><td>67</td><td>4</td><td>18.4</td><td>11</td><td>74</td><td>1</td><td>37.4</td><td>14</td></tr><tr><td>ECG 108</td><td>21600</td><td>539</td><td>11</td><td>18.3</td><td>44</td><td>472</td><td>9</td><td>20.8</td><td>45</td></tr><tr><td>ECG 15</td><td>15000</td><td>279</td><td>9</td><td>19.2</td><td>58</td><td>239</td><td>5</td><td>25.7</td><td>71</td></tr><tr><td>ECG 300</td><td>536976</td><td>10178</td><td>4458</td><td>34.2</td><td>980</td><td>7649</td><td>2048</td><td>35.7</td><td>1673</td></tr><tr><td>ECG 308</td><td>5400</td><td>131</td><td>4</td><td>13.2</td><td>14</td><td>143</td><td>1</td><td>22.1</td><td>15</td></tr><tr><td>ECG 318</td><td>586086</td><td>7113</td><td>2234</td><td>27.8</td><td>1422</td><td>5112</td><td>1435</td><td>29.1</td><td>2942</td></tr><tr><td>Insect</td><td>18667</td><td>632</td><td>19</td><td>17.1</td><td>25</td><td>584</td><td>10</td><td>18.3</td><td>32</td></tr><tr><td>Respiration, NPRS 43</td><td>4000</td><td>881</td><td>33</td><td>26.5</td><td>29</td><td>813</td><td>12</td><td>27.5</td><td>45</td></tr><tr><td>Respiration, NPRS 44</td><td>24125</td><td>1189</td><td>66</td><td>28.1</td><td>40</td><td>1057</td><td>17</td><td>28.9</td><td>61</td></tr><tr><td>TEK14</td><td>5000</td><td>205</td><td>5</td><td>27.4</td><td>78</td><td>237</td><td>3</td><td>32.6</td><td>130</td></tr><tr><td>TEK16</td><td>5000</td><td>181</td><td>4</td><td>25.8</td><td>100</td><td>210</td><td>2</td><td>31.9</td><td>157</td></tr><tr><td>TEK17</td><td>5000</td><td>190</td><td>7</td><td>26.5</td><td>190</td><td>208</td><td>2</td><td>32</td><td>208</td></tr><tr><td>Video dataset</td><td>11251</td><td>285</td><td>11</td><td>16.9</td><td>29</td><td>301</td><td>7</td><td>21.8</td><td>30</td></tr><tr><td>Winding</td><td>2500</td><td>70</td><td>3</td><td>10.6</td><td>5</td><td>225</td><td>1</td><td>33.5</td><td>5</td></tr></table>

## Made with Aloha!
![Made with Aloha!](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/src/resources/assets/aloha.jpg)


#### Versions:
`2.0.1`
  * depends on **jmotif-sax 2.0.1**; SLF4J 2.0.9 and Logback 1.3.14 (addresses Dependabot serialization advisory on Logback 1.2.x)

`2.0.0`
  * **the build now targets Java 21** (was Java 8); depends on **jmotif-sax 2.0.0**
  * Maven dependency scopes tightened (JUnit test-only, Logback runtime); JaCoCo upgraded to 0.8.15
  * CI runs a Java 21/25 matrix; Codecov upload gated to the Java 21 leg
  * RePair paper golden test aligned with jmotif-R/saxpy (`xxx` token, `R4 xxx R4`)

`1.0.1`
  * Optimized rule pruning algorithm
  * `GrammarRules`, `GrammarRuleRecord`, and `RuleInterval` implement `Serializable`
  
`1.0.0`
  * more tests & [Grammarviz3.0](https://github.com/GrammarViz2/grammarviz2_src) release.
  
`0.8.6`
  * pre-1.0 release with improved RePair implementation.

`0.0.1 - 0.8.5`
  * initial code development, parallel repair implementation lifecycle.
