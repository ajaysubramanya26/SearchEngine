package neu.ir.cs6200.querydata;

import static neu.ir.cs6200.constants.Const_FilePaths.InvertedIndexFName_TF;
import static neu.ir.cs6200.constants.Const_FilePaths.ParsedDirName;
import static neu.ir.cs6200.constants.Const_FilePaths.Pseudo_Relevance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import neu.ir.cs6200.T1.indexer.IndexedDataReader;
import neu.ir.cs6200.T1.parser.Parser;
import neu.ir.cs6200.constants.Const_FilePaths;
import neu.ir.cs6200.evaluator.Mode;
import neu.ir.cs6200.utils.FileUtils;
import neu.ir.cs6200.utils.SortUtils;

public class PseudoRevelance {
	private static Logger logger = Logger.getLogger(PseudoRevelance.class);

	private Map<Integer, String> expPseudoRel_queries;
	int numExpandedQueryTerms;

	List<String> stopWords;

	public PseudoRevelance(int numExpandedQueryTerms) {
		expPseudoRel_queries = new HashMap<>();
		this.numExpandedQueryTerms = numExpandedQueryTerms;

		stopWords = new ArrayList<String>();
		// this is the stop word list provided in data
		stopWords.addAll(Arrays.asList(Parser.getStopWords()));
		// merge top 50 stop words found from our parsing and indexing
		List<String> stopList = IndexedDataReader.getStopWords_TermFrequencyFile(InvertedIndexFName_TF + "_N1", 50);
		for (String word : stopList) {
			if (!stopWords.contains(word)) stopWords.add(word);
		}
	}

	/**
	 * Get HashMap of Expanded Queries
	 *
	 * @return
	 */
	public Map<Integer, String> getExpPseudoRel_queries() {
		return expPseudoRel_queries;
	}

	/**
	 * Returns top N Query Results out of Query Results
	 *
	 * @param queryId
	 * @param mode
	 * @param topN
	 * @return
	 */
	private List<String> getTopNQueryResultDocs(int queryId, Mode mode, int topNQueryDocs) {

		List<String> queryResTopN = new ArrayList<>();
		List<String> queryRes = QueryResultReader.loadQuerySearchResult(queryId, mode);
		if (queryRes == null || queryRes.size() == 0) {
			logger.error("No query Results found for query " + queryId + " mode " + mode);
			return null;
		}
		int i = 0;
		for (i = 0; i < queryRes.size(); i++) {
			queryResTopN.add(queryRes.get(i));
			if (i == topNQueryDocs) {
				break;
			}
		}
		if (i < topNQueryDocs) {
			logger.warn("Not enough documents for query " + queryId + " to perform pseudo revelance");
		}
		return queryResTopN;
	}

	/**
	 * Runs pseudo relevance on each of the raw query to get expanded queries
	 *
	 * @param mode
	 * @param queryReader
	 * @param topnQueryResDocsPseudoRelevance
	 */
	public void createExpandedQueries(Mode mode, QueryDataReader queryReader, int topnQueryResDocsPseudoRelevance) {
		FileUtils.deleteFolder(Const_FilePaths.Pseudo_Relevance);
		FileUtils.createDirectory(Const_FilePaths.Pseudo_Relevance);
		for (int i = 0; i < queryReader.getRaw_queries().size(); i++) {
			getExpandedQueryTerms(i + 1, mode, topnQueryResDocsPseudoRelevance, true, queryReader);
		}
	}

	/**
	 * Build the map of terms found in top N chosen query result documents
	 *
	 * @param docIds
	 * @param dfHmRelevanceDocs
	 * @param tfHmRelevanceDocs
	 */
	private boolean buildTermFrequencyRevelanceDocs(List<String> docIds, boolean removeStopWords,
			Map<String, Integer> tfHmRelevanceDocs, Map<String, Integer> dfHmRelevanceDocs) {

		if (docIds == null || docIds.size() == 0) {
			logger.error("buildTermFrequencyRevelanceDocs not formed");
			return false;
		}

		// combine it with the top 50 frequent terms found form our parsing and
		// indexing

		for (int i = 0; i < docIds.size(); i++) {
			try (BufferedReader br = new BufferedReader(
					new FileReader(new File(ParsedDirName + "/" + docIds.get(i))))) {
				String line;
				int df = 0;
				while ((line = br.readLine()) != null) {
					String[] terms = line.split(" ");
					for (String term : terms) {
						if (removeStopWords && stopWords.contains(term)) {
							continue;
						}

						if (tfHmRelevanceDocs.containsKey(term)) {
							tfHmRelevanceDocs.put(term, tfHmRelevanceDocs.get(term) + 1);
						} else {
							tfHmRelevanceDocs.put(term, 1);
						}
						df++;
					}
				}
				dfHmRelevanceDocs.put(docIds.get(i), df);
			} catch (IOException e) {

				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets expanded query terms for a query num based on the results obtained
	 * using mode(BM25,Lucene,TF_IDF) and scores it using the score function and
	 * gets topK terms
	 *
	 * @param queryId
	 * @param mode
	 * @param topN
	 * @param removeStopWords
	 * @return
	 */
	private void getExpandedQueryTerms(int queryId, Mode mode, int topNQueryDocs, boolean removeStopWords,
			QueryDataReader qReader) {

		Map<String, Integer> tfHmRelevanceDocs = new HashMap<>();
		Map<String, Integer> dfHmRelevanceDocs = new HashMap<>();

		logger.info("Running Pseudo Revelance for Query Id " + queryId + " Mode " + mode);

		boolean res = buildTermFrequencyRevelanceDocs((getTopNQueryResultDocs(queryId, mode, topNQueryDocs)),
				removeStopWords, tfHmRelevanceDocs, dfHmRelevanceDocs);

		String kExpandedQueryTerms = "";
		if (res == false) {
			logger.warn("No Expanded query terms for query id using Pseudo Revelance" + queryId + " "
					+ kExpandedQueryTerms);
		}
		Map<String, Integer> sortedMap = SortUtils.sortByValue(tfHmRelevanceDocs, true);

		// TODO : Advanced Scoring function
		int num = 0;
		List<String> rawQueryTerms = Arrays.asList(qReader.getRaw_query(queryId).split("\\s"));
		for (String term : sortedMap.keySet()) {
			// avoid selecting terms already in the query
			if (!rawQueryTerms.contains(term)) {
				kExpandedQueryTerms += " " + term;
				if (num++ == numExpandedQueryTerms) break;
			}
		}
		sortedMap.remove(null);
		writeToFile(queryId, mode, qReader, kExpandedQueryTerms, sortedMap);

		expPseudoRel_queries.put(queryId, qReader.getRaw_query(queryId) + " " + kExpandedQueryTerms);
	}

	/**
	 * Write expanded queries to file for debugging and analysis purpose
	 *
	 * @param queryId
	 * @param mode
	 * @param qReader
	 * @param kExpandedQueryTerms
	 * @param sortedMap
	 */
	private void writeToFile(int queryId, Mode mode, QueryDataReader qReader, String kExpandedQueryTerms,
			Map<String, Integer> sortedMap) {
		File file = new File(Pseudo_Relevance + "/Q" + queryId);
		logger.info("Raw Q Terms : " + qReader.getRaw_query(queryId));
		if (logger.isDebugEnabled()) {
			logger.debug("All terms from top 20(collection set) docs found for a Q(sorted on TF) : " + sortedMap);
		}
		logger.info("Expanded Q Terms(without Raw Q terms) : " + kExpandedQueryTerms);
		try {
			Files.append(new StringBuilder(
					"\nFor query " + queryId + " Mode " + mode + "\nRaw query terms : " + qReader.getRaw_query(queryId))
					+ "\n\n", file, Charsets.UTF_8);
			Files.append(
					new StringBuilder("All terms from top 20(collection set) docs found for a query(sorted on TF) :"
							+ sortedMap + "\n\n"),
					file, Charsets.UTF_8);
			Files.append(new StringBuilder("Expanded Q Terms(without Raw Q terms) : " + kExpandedQueryTerms), file,
					Charsets.UTF_8);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
