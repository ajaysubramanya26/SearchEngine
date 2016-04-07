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
	public static final String QueryResultsDir = Results + "/" + "QueryResults";
}
