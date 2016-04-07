package neu.ir.cs6200.T1.ranking;

import static neu.ir.cs6200.constants.Consts.IR_SystemName;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.collect.ListMultimap;
import com.google.common.io.Files;

import neu.ir.cs6200.T1.indexer.IndexedDataReader;
import neu.ir.cs6200.querydata.QueryDataReader;
import neu.ir.cs6200.utils.SortUtils;

public class TF_IDF {

	final static Logger logger = Logger.getLogger(TF_IDF.class);
	/**
	 * Stores the invertedIndex for Unigram after reading data from disk
	 */
	HashMap<String, HashMap<String, Integer>> invertedLists;

	/**
	 * Stores the document length(Number of words) for each document after
	 * reading data from the file
	 */
	HashMap<String, Long> documentLenHm;

	int topNRankedDocs;
	String queryResultDir;

	public TF_IDF(int topNRankedDocs, String queryResultDir) {
		this.topNRankedDocs = topNRankedDocs;
		this.queryResultDir = queryResultDir;
	}

	public HashMap<String, HashMap<String, Integer>> getInvertedLists() {
		return invertedLists;
	}

	public void setInvertedLists(HashMap<String, HashMap<String, Integer>> invertedLists) {
		if (invertedLists.size() == 0) {
			logger.error("Empty Inverted List");
		}
		this.invertedLists = invertedLists;
	}

	public HashMap<String, Long> getDocumentLenHm() {
		return documentLenHm;
	}

	public void setDocumentLenHm(HashMap<String, Long> documentLenHm) {
		this.documentLenHm = documentLenHm;
	}

	/**
	 * Using log normalization for computing term frequency (1 +
	 * log(tf(t,docId)). logarithmically scaled frequency: tf(t,d) = 1 + log
	 * ft,d, or zero if ft,d is zero;
	 *
	 * @param qTerm
	 * @param docId
	 * @return
	 */
	private double computeTermFrequency(String qTerm, String docId) {
		int tf = 0;
		if (invertedLists.get(qTerm) != null && invertedLists.get(qTerm).get(docId) != null) {
			tf = invertedLists.get(qTerm).get(docId);
			return (1 + Math.log(tf));
		} else {
			return 0;
		}

	}

	/**
	 * Using inverse document frequency smooth for computing idf (1 +
	 * log(N/n(t)))
	 *
	 * @param qTerm
	 * @param docId
	 * @return
	 */
	private double computeInverseDocumentFrequency(String qTerm, String docId) {
		double totalNumOfDocs = this.documentLenHm.size();
		if (this.invertedLists.get(qTerm) != null) {
			double numOfDocsQueryTermPresent = this.invertedLists.get(qTerm).size();
			return (1 + Math.log(totalNumOfDocs / numOfDocsQueryTermPresent));
		} else {
			return 0;
		}
	}

	private void computeTFIDFScore(String queryStr, int qNum, String sysName) {
		HashMap<String, Double> rankScoreHm = new HashMap<>();

		HashMap<String, Short> qTermFre = new HashMap<>();
		QueryDataReader.computeQueryTermFre(queryStr, qTermFre);

		HashSet<String> queryDocs = new HashSet<>();
		for (String queryTerm : qTermFre.keySet()) {
			/** Query term present in the inverted list */
			if (invertedLists.get(queryTerm) != null) {
				queryDocs.addAll(invertedLists.get(queryTerm).keySet());
			}
		}

		/**
		 * Compute score for each document based on the query terms
		 */
		for (String docId : queryDocs) {
			double queryScore = 0.0;
			for (String queryTerm : qTermFre.keySet()) {
				queryScore += tfIDFScorePerDocumentPerQTerm(docId, queryTerm);
			}
			rankScoreHm.put(docId, queryScore);
		}

		writeTopNDocsTFIDFScore(rankScoreHm, qNum, sysName);
	}

	private double tfIDFScorePerDocumentPerQTerm(String docId, String queryTerm) {

		return computeTermFrequency(queryTerm, docId) * computeInverseDocumentFrequency(queryTerm, docId);
	}

	/**
	 * Writes topN ranked TF-IDF score documents to file in cacm result format
	 *
	 * @param rankScoreHm
	 * @param qNum
	 * @param sysName
	 */
	private void writeTopNDocsTFIDFScore(HashMap<String, Double> rankScoreHm, int qNum, String sysName) {
		ListMultimap<Double, String> sortedRanks = SortUtils.sortMostToLeastScore(rankScoreHm);
		int topRanked = 1;

		try {
			File fileQResBM25 = new File(this.queryResultDir + "/Q" + qNum + "_TFIDF");

			for (Double rankScore : sortedRanks.keySet()) {
				List<String> docIds = sortedRanks.get(rankScore);
				for (String docId : docIds) {
					Files.append(new StringBuilder(qNum + " " + "Q0" + " " + docId + ".txt" + " " + topRanked + " "
							+ rankScore + " " + sysName + "\n"), fileQResBM25, Charsets.UTF_8);
					topRanked++;

				}
				if (topRanked > this.topNRankedDocs) break;
			}
		} catch (IOException e) {
			logger.error("writeTopNDocsTFIDFScore Error in Writing", e);
		}
	}

	public void runTFIDF(HashMap<Integer, String> queries, IndexedDataReader indexReader) {

		logger.info("Running TFIDF");

		this.setInvertedLists(indexReader.getInvertedLists());
		this.setDocumentLenHm(indexReader.getDocumentLenHm());

		for (int qNum : queries.keySet()) {
			logger.debug("Running TF-IDF ranking for query :" + queries.get(qNum));
			this.computeTFIDFScore(queries.get(qNum), qNum, IR_SystemName);
			logger.debug("");
		}

	}

}
