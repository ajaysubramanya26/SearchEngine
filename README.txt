Authors : Smitha Bangalore Naresh, Kamlendra Kumar, Ajay Subramanya
emails : bangalorenaresh.s@husky.neu.edu 
	 kumar.ka@husky.neu.edu
	 subramanya.a@husky.neu.edu

Using Java and Maven. 

Below are the instructions about the document structure, design, build and running.

Given CACM is found under folder data.


Document structure:
.
├── IR_Project_cs6200.pdf
├── README.txt
├── Results
│   ├── Evaluation_Results
│   ├── ExpandedQueries
│   ├── Indexer_Output
│   ├── Parser_Output
│   ├── Parser_Output_NoStopWords
│   ├── Query_Results
│   ├── Tokenizer_Output
│   └── Tokenizer_Output_NoStopWords
├── data
│   ├── cacm
│   ├── cacm.query
│   ├── cacm.rel
│   ├── cacm.tar.gz
│   ├── cacm_stem.query.txt
│   ├── cacm_stem.txt
│   ├── common_words
│   └── wn_s.pl
├── log4j.properties
├── makefile
├── pom.xml
└── src
    ├── main
    └── test

14 directories, 12 files


neu.ir.cs6200.ir_project.App.java is the main class which setup and runs everything.

logs are written in under ./log/ask_ir.log folder

#############################################################################################
To BUILD and RUN use the following make command(it builds and runs):
make run_ask_ir

Approximate run time is around 2~3mins

#############################################################################################
RESULTS:

View in Results go to “Results” folder. 

NOTE : Results folder will be overwritten with each run.

Directory structure of Results are as follows.
Parser_Output, Indexer_Output, Temp_IndexLucene,Tokenizer_Output, 
ExpandedQueries, Parser_Output_NoStopWords, 
Tokenizer_Output_NoStopWords are results of each step r.p.t

The 7 tables can be found under:

Results/Query_Results/Task1/<QNUM>_BM25
Results/Query_Results/Task1/<QNUM>_Lucene
Results/Query_Results/Task1/<QNUM>_TFIDF

Results/Query_Results/Task2/<QNUM>_BM25PseudoRel
Results/Query_Results/Task2/<QNUM>_ BM25SynRel

Results/Query_Results/Task3/<QNUM>_BM25Stopping
Results/Query_Results/Task3/<QNUM>_T7


The Results for the Evaluation are found under    
Results/Evaluation_Results which contains 7 files along with Task4 folder
BM25
Lucene
TFIDF
BM25PseudoRel
BM25SynRel
BM25Stopping
T7

