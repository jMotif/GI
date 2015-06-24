# Grammatical Inference
Implements Sequtur (online) and Re-Pair (off-line) grammar induction algorithms for [Grammarviz 2.0](https://github.com/GrammarViz2/grammarviz2_site) and [SAX-VSM-G](https://github.com/seninp/sax-vsm-g). 

The library is **[available through Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cjmotif-gi)** and built by TravisCI: [![Build Status](https://travis-ci.org/jMotif/GI.svg?branch=master)](https://travis-ci.org/jMotif/GI).

##### More about implemented algorithms:
[1] Nevill-Manning, C.G. and Witten, I.H., [*"Identifying Hierarchical Structure in Sequences: A linear-time algorithm"*](http://www.jair.org/media/374/live-374-1630-jair.pdf), Journal of Artificial Intelligence Research, 7, 67-82, (1997).

[2] Larsson, N.J.; Moffat, A., [*"Offline dictionary-based compression"*](http://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=755679&isnumber=16375), Data Compression Conference, 1999. Proceedings. DCC '99 , vol., no., pp.296,305, 29-31 Mar 1999.

##### Citing this work:
If you are using this implementation for you academic work, please cite our [Grammarviz 2.0 paper](http://link.springer.com/chapter/10.1007/978-3-662-44845-8_37):

[Citation] Senin, P., Lin, J., Wang, X., Oates, T., Gandhi, S., Boedihardjo, A.P., Chen, C., Frankenstein, S., Lerner, M.,  [*GrammarViz 2.0: a tool for grammar-based pattern discovery in time series*](http://www2.hawaii.edu/~senin/assets/papers/grammarviz2.pdf), ECML/PKDD Conference, 2014.

1.0 Building
------------
The code is written in Java and I use maven to build it:
	
	$ mvn package
	[INFO] Scanning for projects...
	[INFO] ------------------------------------------------------------------------
  	[INFO] Building GI
	[INFO]    task-segment: [package]
  	...
	[INFO] Building jar: /media/Stock/git/jmotif-GI.git/target/jmotif-gi-0.3.1-SNAPSHOT.jar
	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESSFUL
	[INFO] ------------------------------------------------------------------------
  
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
 
My own addition allows to retrieve the Sequitur rules as an iterable collection of [GrammaRuleRecords](https://github.com/jMotif/GI/blob/master/src/main/java/net/seninp/gi/GrammarRuleRecord.java) and to map them back to the discretized time series:

	GrammarRules rules = r.toGrammarRulesData();
	GrammarRuleRecord rec = rules.get(4);
	ArrayList<RuleInterval> intervals = rec.getRuleIntervals();
	...
  

2.0 Sequitur API use
------------

	
![Threaded RePair performance](https://raw.githubusercontent.com/jMotif/GI/master/src/RCode/profiling.png)
