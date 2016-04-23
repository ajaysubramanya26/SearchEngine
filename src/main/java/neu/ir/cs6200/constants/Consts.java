package neu.ir.cs6200.constants;

public class Consts {
	public static final String IR_SystemName = "ASK_IR";
	public static final int TOPN_QUERY_SEARCH_RES = 100;
	public static final int DOCID_INDEX_QUERY_RESULT = 2;

	public static final int TOPN_QUERY_RES_DOCS_PSEUDO_RELEVANCE = 20;
	public static final int TOPK_QUERY_EXPANDED_TERMS_PSEUDO_RELEVANCE = 10;

	public static final String BM25_FName = "BM25";
	public static final String BM25PseudoRel_Fname = "BM25PseudoRel";
	public static final String BM25_NoStopWords_Fname = "BM25_NoStopWords";

	// BM25 constants
	public static final double k1 = 1.2;
	public static final double b = 0.75;
	public static final double k2 = 100;
}
