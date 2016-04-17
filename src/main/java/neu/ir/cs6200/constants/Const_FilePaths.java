package neu.ir.cs6200.constants;

public class Const_FilePaths {

	public static final String CorpusDirLoc = "data/cacm";

	public static final String Results = "Results";
	public static final String InvertedIndexDirName = "Results" + "/" + "Indexer_Output";
	public static final String TokenizerDirName = "Results" + "/" + "Tokenizer_Output";
	public static final String ParsedDirName = "Results" + "/" + "Parser_Output";
	public static final String InvertedIndexFName_Uni = InvertedIndexDirName + "/" + "InvertedIndex_N1";
	public static final String InvertedIndexFName_TF = InvertedIndexDirName + "/" + "InvertedIndex_TF";
	public static final String InvertedIndexFName_DF = InvertedIndexDirName + "/" + "InvertedIndex_DF";
	public static final String DocLenFname = InvertedIndexDirName + "/" + "DocumentsLength";

	public static final String QueryDataFname = "data/cacm.query";
	public static final String QueryResultsDir = Results + "/" + "Query_Results";
	public static final String Task1QueryResults = QueryResultsDir + "/" + "Task1";
	public static final String Task2QueryResults = QueryResultsDir + "/" + "Task2";
	public static final String Task3QueryResults = QueryResultsDir + "/" + "Task3";

	public static final String Temp_IndexLucene = "./Temp_IndexLucene";
	
	public static final String CACM_RELEVANCE_FILE = "data/cacm.rel";
	public static final String QUERY_RESULT_FILE_PATH = "Results/Query_Results/Task1/";

}
