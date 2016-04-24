package neu.ir.cs6200.constants;

public class Const_FilePaths {

	public static final String CorpusDirLoc = "data/cacm";
	public static final String StopListLoc = "data/common_words";

	public static final String Results = "Results";

	public static final String ParsedDirNameNoStopWords = "Results" + "/" + "Parser_Output_NoStopWords";
	public static final String InvertedIndexNoStopWrdsDirName = "Results" + "/" + "Indexer_Output_NoStopWords";
	public static final String TokenizerDirNameNoStopWrds = "Results" + "/" + "Tokenizer_Output_NoStopWords";

	public static final String ParsedDirName = "Results" + "/" + "Parser_Output";

	public static final String TokenizerDirName = "Results" + "/" + "Tokenizer_Output";
	public static final String InvertedIndexDirName = "Results" + "/" + "Indexer_Output";
	public static final String InvertedIndexFName_Uni = InvertedIndexDirName + "/" + "InvertedIndex_N1";
	public static final String InvertedIndexFName_TF = InvertedIndexDirName + "/" + "InvertedIndex_TF";
	public static final String InvertedIndexFName_DF = InvertedIndexDirName + "/" + "InvertedIndex_DF";
	public static final String DocLenFname = InvertedIndexDirName + "/" + "DocumentsLength";

	public static final String InvertedIndexFNameNoStpWrds = InvertedIndexDirName + "/" + "InvertedIndex_NoStopWords";
	public static final String InvertedIndexFName_No_StpWrds_TF = InvertedIndexDirName + "/"
			+ "InvertedIndex_No_StpWrds_TF";
	public static final String InvertedIndexFName_No_StpWrds_DF = InvertedIndexDirName + "/"
			+ "InvertedIndex_No_StpWrds_DF";
	public static final String DocLenNoStopWordsFname = InvertedIndexDirName + "/" + "DocumentsLength_NoStopWords";

	public static final String QueryDataFname = "data/cacm.query";
	public static final String StemmedCorpus = "data/cacm_stem.txt";
	public static final String StemmedQueryDataFname = "data/cacm_stem.query.txt";
	public static final String CACM_RELEVANCE_FILE = "data/cacm.rel";

	public static final String QueryResultsDir = Results + "/" + "Query_Results";
	public static final String Task1QueryResults = QueryResultsDir + "/" + "Task1";
	public static final String Task2QueryResults = QueryResultsDir + "/" + "Task2";
	public static final String Task3QueryResults = QueryResultsDir + "/" + "Task3";

	public static final String Task3QueryStopWordsResults = Task3QueryResults + "/NoStopWords";
	public static final String Task3QueryStemmedResults = Task3QueryResults + "/Stemmed";

	public static final String TaskTable7Results = QueryResultsDir + "/Table7";

	public static final String Temp_IndexLucene = "./Temp_IndexLucene";

	public static final String QUERY_EVALUATION_RESULT_TASK4 = "Results/Evaluation_Results/QueryPrecisionRecall/";
	public static final String QUERY_EVALUATION_RESULT = "Results/Evaluation_Results/";

	public static final String Expanded_QueryInfo = "Results" + "/" + "ExpandedQueries";
	public static final String Pseudo_Relevance = Expanded_QueryInfo + "/" + "Pseudo_Relevance";
	
	public static final String SYNONYM_FILE = "data/wn_s.pl";
}
