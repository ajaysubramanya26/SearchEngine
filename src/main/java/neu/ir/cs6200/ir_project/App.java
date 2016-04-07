package neu.ir.cs6200.ir_project;

import static neu.ir.cs6200.constants.Const_FilePaths.CorpusDirLoc;
import static neu.ir.cs6200.constants.Const_FilePaths.DocLenFname;
import static neu.ir.cs6200.constants.Const_FilePaths.InvertedIndexFName_Uni;
import static neu.ir.cs6200.constants.Const_FilePaths.ParsedDirName;
import static neu.ir.cs6200.constants.Const_FilePaths.QueryDataFname;
import static neu.ir.cs6200.constants.Const_FilePaths.QueryResultsDir;
import static neu.ir.cs6200.constants.Consts.TOPN_QUERY_SEARCH_RES;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import neu.ir.cs6200.T1.indexer.ReadIndexedData;
import neu.ir.cs6200.T1.indexer.Tokenizer;
import neu.ir.cs6200.T1.parser.Parser;
import neu.ir.cs6200.T1.ranking.BM25;
import neu.ir.cs6200.T1.ranking.Lucene_SimpleAnalyzer;
import neu.ir.cs6200.querydata.ReadQueryData;
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

		Parser.parseStore(CorpusDirLoc, ParsedDirName);
		Tokenizer.tokenizeIndex(ParsedDirName, 1);

		ReadQueryData queryReader = new ReadQueryData();
		queryReader.readQueryDocument(QueryDataFname);
		FileUtils.createDirectory(QueryResultsDir);

		ReadIndexedData indexReader = new ReadIndexedData();
		indexReader.deserializeInvertedIndex(InvertedIndexFName_Uni);
		indexReader.deserializeDocumentsLength(DocLenFname);

		BM25 bm25 = new BM25(1.2, 0.75, 100, TOPN_QUERY_SEARCH_RES);
		bm25.runBM25(queryReader, indexReader);

		Lucene_SimpleAnalyzer.runLucene(queryReader, ParsedDirName);
	}

}
