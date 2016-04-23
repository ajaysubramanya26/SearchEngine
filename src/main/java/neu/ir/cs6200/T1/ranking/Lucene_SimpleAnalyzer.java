package neu.ir.cs6200.T1.ranking;

import static neu.ir.cs6200.constants.Const_FilePaths.Temp_IndexLucene;
import static neu.ir.cs6200.constants.Consts.IR_SystemName;
import static neu.ir.cs6200.constants.Consts.TOPN_QUERY_SEARCH_RES;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * @author smitha
 * @info To create Apache Lucene index in a folder and add files into this index
 *       based on the input of the user.
 */
public class Lucene_SimpleAnalyzer {
	private static Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_47);
	final static Logger logger = Logger.getLogger(Lucene_SimpleAnalyzer.class);

	private IndexWriter writer;
	private ArrayList<File> queue = new ArrayList<File>();

	private String fullPathIndex;
	private String queryResultDir;

	public void searchLucene(String query, int qNum, String sysName, String qFileAppender) {
		try {
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(fullPathIndex)));
			IndexSearcher searcher = new IndexSearcher(reader);
			TopScoreDocCollector collector = TopScoreDocCollector.create(TOPN_QUERY_SEARCH_RES, true);

			Query q = new QueryParser(Version.LUCENE_47, "contents", analyzer).parse(query);
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			// 4. display results
			logger.debug("Found " + hits.length + " hits. for Q" + qNum);
			File fileQResLucene = new File(this.queryResultDir + "/Q" + qNum + "_" + qFileAppender);
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				logger.debug((i + 1) + ". " + d.get("path") + " score=" + hits[i].score);
				Files.append(new StringBuilder(qNum + " " + "Q0" + " " + d.get("filename") + " " + (i + 1) + " "
						+ hits[i].score + " " + sysName + "\n"), fileQResLucene, Charsets.UTF_8);
			}
			// 5. term stats --> watch out for which "version" of the term
			// must be checked here instead!

			String[] queryStr = query.trim().split(" ");
			for (String qr : queryStr) {
				Term termInstance = new Term("contents", qr);
				long termFreq = reader.totalTermFreq(termInstance);
				long docCount = reader.docFreq(termInstance);
				logger.debug(qr + " Term Frequency " + termFreq + " -Document Frequency " + docCount);
			}

		} catch (Exception e) {
			logger.error("Error searching " + query + " : " + e.getMessage());
		}
	}

	/**
	 * Constructor
	 *
	 * @param indexDir
	 *            the name of the folder in which the index should be created
	 * @param queryResultDir
	 * @throws java.io.IOException
	 *             when exception creating index.
	 */
	public Lucene_SimpleAnalyzer(String indexDir, String queryResultDir) throws IOException {
		FSDirectory dir = FSDirectory.open(new File(indexDir));
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
		writer = new IndexWriter(dir, config);
		this.fullPathIndex = indexDir;
		this.queryResultDir = queryResultDir;
	}

	/**
	 * Indexes a file or directory
	 *
	 * @param fileName
	 *            the name of a text file or a folder we wish to add to the
	 *            index
	 * @throws java.io.IOException
	 *             when exception
	 */
	public void indexFileOrDirectory(String fileName) throws IOException {
		// ===================================================
		// gets the list of files in a folder (if user has submitted
		// the name of a folder) or gets a single file name (is user
		// has submitted only the file name)
		// ===================================================
		addFiles(new File(fileName));

		int originalNumDocs = writer.numDocs();
		for (File f : queue) {
			FileReader fr = null;
			try {
				Document doc = new Document();

				// ===================================================
				// add contents of file
				// ===================================================
				fr = new FileReader(f);
				doc.add(new TextField("contents", fr));
				doc.add(new StringField("path", f.getPath(), Field.Store.YES));
				doc.add(new StringField("filename", f.getName(), Field.Store.YES));

				writer.addDocument(doc);
				logger.debug("Added: " + f);
			} catch (Exception e) {
				logger.warn("Could not add: " + f);
			} finally {
				fr.close();
			}
		}

		int newNumDocs = writer.numDocs();
		logger.info("");
		logger.info("************************");
		logger.info((newNumDocs - originalNumDocs) + " documents added.");
		logger.info("************************");

		queue.clear();
	}

	private void addFiles(File file) {

		if (!file.exists()) {
			logger.error(file + " dir does not exist.");
		}
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				addFiles(f);
			}
		} else {
			String filename = file.getName().toLowerCase();
			// ===================================================
			// Only index text files
			// ===================================================
			if (filename.endsWith(".htm") || filename.endsWith(".html") || filename.endsWith(".xml")
					|| filename.endsWith(".txt")) {
				queue.add(file);
			} else {
				System.out.println("Skipped " + filename);
			}
		}
	}

	/**
	 * Close the index.
	 *
	 * @throws java.io.IOException
	 *             when exception closing
	 */
	public void closeIndex() throws IOException {
		writer.close();
	}

	private static Lucene_SimpleAnalyzer createLuceneSimpleIndexer(String indexPath, String filesForIndexPath,
			String queryResultDir) {
		Lucene_SimpleAnalyzer indexer = null;
		try {
			indexer = new Lucene_SimpleAnalyzer(indexPath, queryResultDir);
		} catch (Exception ex) {
			logger.error("Cannot create index..." + ex.getMessage());
			System.exit(-1);
		}

		try {
			// try to add files into the index
			indexer.indexFileOrDirectory(filesForIndexPath);
			/**
			 * after adding, we always have to call the closeIndex, otherwise
			 * the index is not created
			 */
			indexer.closeIndex();
		} catch (Exception e) {
			logger.error("Error indexing " + filesForIndexPath + " : " + e.getMessage());
		}
		return indexer;
	}

	/**
	 * Runs Lucene program on given corpus and raw_queries
	 *
	 * @param synonymExpandedQueries
	 * @param queryResultDir
	 *
	 */
	public static void runLucene(Map<Integer, String> synonymExpandedQueries, String parsedDirName,
			String queryResultDir, String qFileAppender) {

		logger.info("Running Lucene.... Using SimpleAnalyzer");
		Lucene_SimpleAnalyzer indexer = createLuceneSimpleIndexer(Temp_IndexLucene, parsedDirName, queryResultDir);

		for (int qNum : synonymExpandedQueries.keySet()) {
			logger.debug("Running Lucene SimpleAnalyzer ranking for query :" + synonymExpandedQueries.get(qNum));
			indexer.searchLucene(synonymExpandedQueries.get(qNum), qNum, IR_SystemName, qFileAppender);
			logger.debug("");
		}
	}
}