package neu.ir.cs6200.ir_project;

import static neu.ir.cs6200.constants.Const_FilePaths.CorpusDirLoc;
import static neu.ir.cs6200.constants.Const_FilePaths.DocLenFname;
import static neu.ir.cs6200.constants.Const_FilePaths.DocLenNoStopWordsFname;
import static neu.ir.cs6200.constants.Const_FilePaths.InvertedIndexDirName;
import static neu.ir.cs6200.constants.Const_FilePaths.InvertedIndexFNameNoStpWrds;
import static neu.ir.cs6200.constants.Const_FilePaths.InvertedIndexFName_DF;
import static neu.ir.cs6200.constants.Const_FilePaths.InvertedIndexFName_TF;
import static neu.ir.cs6200.constants.Const_FilePaths.InvertedIndexFName_Uni;
import static neu.ir.cs6200.constants.Const_FilePaths.ParsedDirName;
import static neu.ir.cs6200.constants.Const_FilePaths.ParsedDirNameNoStopWords;
import static neu.ir.cs6200.constants.Const_FilePaths.QueryDataFname;
import static neu.ir.cs6200.constants.Const_FilePaths.StemmedCorpus;
import static neu.ir.cs6200.constants.Const_FilePaths.Task1QueryResults;
import static neu.ir.cs6200.constants.Const_FilePaths.Task2QueryResults;
import static neu.ir.cs6200.constants.Const_FilePaths.Task3QueryStemmedResults;
import static neu.ir.cs6200.constants.Const_FilePaths.Task3QueryStopWordsResults;
import static neu.ir.cs6200.constants.Const_FilePaths.TaskTable7Results;
import static neu.ir.cs6200.constants.Const_FilePaths.TokenizerDirName;
import static neu.ir.cs6200.constants.Const_FilePaths.TokenizerDirNameNoStopWrds;
import static neu.ir.cs6200.constants.Consts.BM25PseudoRel_Fname;
import static neu.ir.cs6200.constants.Consts.BM25_FName;
import static neu.ir.cs6200.constants.Consts.BM25_NoStopWords_Fname;
import static neu.ir.cs6200.constants.Consts.LuceneWithoutStopSyn_Fname;
import static neu.ir.cs6200.constants.Consts.Lucene_Fname;
import static neu.ir.cs6200.constants.Consts.TOPK_QUERY_EXPANDED_TERMS_PSEUDO_RELEVANCE;
import static neu.ir.cs6200.constants.Consts.TOPN_QUERY_RES_DOCS_PSEUDO_RELEVANCE;
import static neu.ir.cs6200.constants.Consts.TOPN_QUERY_SEARCH_RES;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import neu.ir.cs6200.T1.indexer.IndexMode;
import neu.ir.cs6200.T1.indexer.IndexedDataReader;
import neu.ir.cs6200.T1.indexer.Tokenizer;
import neu.ir.cs6200.T1.parser.Parser;
import neu.ir.cs6200.T1.ranking.BM25;
import neu.ir.cs6200.T1.ranking.Lucene_SimpleAnalyzer;
import neu.ir.cs6200.T1.ranking.TF_IDF;
import neu.ir.cs6200.constants.Consts;
import neu.ir.cs6200.evaluator.Mode;
import neu.ir.cs6200.querydata.PseudoRevelance;
import neu.ir.cs6200.querydata.QueryDataReader;
import neu.ir.cs6200.utils.FileUtils;

/**
 * Main class for SKA Information Reterival System
 *
 * @author smitha
 * @author ajay
 * @author kamlendra
 */
public class App {
	public static void main(String[] args) {

		/** Deletes all results from previous run and creates new directories */
		FileUtils.dirFileSetUp();

		String log4jConfPath = "./log4j.properties";
		PropertyConfigurator.configure(log4jConfPath);

		final Logger logger = Logger.getLogger(App.class);

		/** Use CACM corpus folder to parsed document */
		File dirCorpus = new File(CorpusDirLoc);
		if (!dirCorpus.exists()) {
			logger.error("Corpus Directory not found!!" + CorpusDirLoc);
			return;
		}

		FileUtils.deleteFolder(TokenizerDirName);
		FileUtils.createDirectory(TokenizerDirName);
		FileUtils.deleteFolder(TokenizerDirNameNoStopWrds);
		FileUtils.createDirectory(TokenizerDirNameNoStopWrds);
		FileUtils.deleteFolder(InvertedIndexDirName);
		FileUtils.createDirectory(InvertedIndexDirName);

		Parser.setUseStopList(false);
		Parser.parseStore(CorpusDirLoc, ParsedDirName);
		Tokenizer rawTokens = new Tokenizer(InvertedIndexFName_Uni, InvertedIndexFName_TF, InvertedIndexFName_DF,
				IndexMode.NORMAL);
		rawTokens.tokenizeIndex(ParsedDirName, 1);

		QueryDataReader queryReader = new QueryDataReader();
		queryReader.readQueryDocument(QueryDataFname);
		IndexedDataReader indexReader = new IndexedDataReader();
		indexReader.deserializeInvertedIndex(InvertedIndexFName_Uni);
		indexReader.deserializeDocumentsLength(DocLenFname);

		runTask1_RawQueries(queryReader, indexReader);

		runTask2_QueryExpansion(queryReader, indexReader);

		runTask3_RawQueries(queryReader);

		// Produce one more (seventh) run that does one of the following:
		// 1- Combines a query expansion technique with stopping
		// 2- Uses a different base search engine than the one you chose
		// earlier, and adopts either a query expansion technique and/or
		// stopping
		FileUtils.createDirectory(TaskTable7Results);
		Lucene_SimpleAnalyzer.runLucene(queryReader, ParsedDirNameNoStopWords, TaskTable7Results,
				LuceneWithoutStopSyn_Fname);

		// SearchEngineEvaluator eval = new SearchEngineEvaluator();
		// eval.evaluate();

		logger.info("Done");

	}

	/**
	 * Three baseline runs (from the three search engines described below),
	 * namely: Your search engine with BM25 as a retrieval model, your search
	 * engine with tf-idf as a retrieval model, and Lucene’s default retrieval
	 * model. Only the top 100 retrieved ranked lists (one list per run/search
	 * engine) are to be reported.
	 *
	 * @param queryReader
	 * @param indexReader
	 */
	public static void runTask1_RawQueries(QueryDataReader queryReader, IndexedDataReader indexReader) {

		FileUtils.createDirectory(Task1QueryResults);

		// Table1
		BM25 bm25 = new BM25(Consts.k1, Consts.b, Consts.k2, TOPN_QUERY_SEARCH_RES, Task1QueryResults);
		bm25.runBM25(queryReader.getRaw_queries(), indexReader, BM25_FName);

		// Table2
		Lucene_SimpleAnalyzer.runLucene(queryReader, ParsedDirName, Task1QueryResults, Lucene_Fname);

		// Table3
		TF_IDF tfidf = new TF_IDF(TOPN_QUERY_SEARCH_RES, Task1QueryResults);
		tfidf.runTFIDF(queryReader.getRaw_queries(), indexReader);
	}

	/**
	 * Use Query Expansion techniques to get expanded query terms. <br>
	 * Then use BM25 to get rank on expanded query.<br>
	 *
	 *
	 * @param queryReader
	 * @param indexReader
	 */
	public static void runTask2_QueryExpansion(QueryDataReader queryReader, IndexedDataReader indexReader) {
		FileUtils.createDirectory(Task2QueryResults);

		PseudoRevelance pseudoRel = new PseudoRevelance(TOPK_QUERY_EXPANDED_TERMS_PSEUDO_RELEVANCE);
		pseudoRel.createExpandedQueries(Mode.BM25, queryReader, TOPN_QUERY_RES_DOCS_PSEUDO_RELEVANCE);

		BM25 bm25 = new BM25(Consts.k1, Consts.b, Consts.k2, TOPN_QUERY_SEARCH_RES, Task2QueryResults);
		bm25.runBM25(pseudoRel.getExpPseudoRel_queries(), indexReader, BM25PseudoRel_Fname);

	}

	/**
	 * TASK 3a, 3b
	 *
	 * @param queryReader
	 * @param indexReader
	 */
	public static void runTask3_RawQueries(QueryDataReader queryReader) {
		System.out.println("running task 3");

		FileUtils.createDirectory(Task3QueryStopWordsResults);
		FileUtils.createDirectory(Task3QueryStemmedResults);

		// TASK 3a
		Parser.setUseStopList(true);
		Parser.parseStore(CorpusDirLoc, ParsedDirNameNoStopWords);
		Tokenizer noStopTokenizer = new Tokenizer(InvertedIndexFNameNoStpWrds, InvertedIndexFName_TF + "_NoStopWords",
				InvertedIndexFName_DF + "_NoStopWords", IndexMode.STOP);
		noStopTokenizer.tokenizeIndex(ParsedDirNameNoStopWords, 1);

		IndexedDataReader indexReaderNoStopWords = new IndexedDataReader();
		indexReaderNoStopWords.deserializeInvertedIndex(InvertedIndexFNameNoStpWrds);
		indexReaderNoStopWords.deserializeDocumentsLength(DocLenNoStopWordsFname);

		BM25 bm25 = new BM25(Consts.k1, Consts.b, Consts.k2, TOPN_QUERY_SEARCH_RES, Task3QueryStopWordsResults);
		bm25.runBM25(queryReader.getRaw_queries(), indexReaderNoStopWords, BM25_NoStopWords_Fname);

		// Task 3b
		Parser.setUseStopList(false);
		int numberOfStemmedDocs = 3204;
		Parser.stemAndRunBm25(StemmedCorpus, numberOfStemmedDocs);

	}
}
