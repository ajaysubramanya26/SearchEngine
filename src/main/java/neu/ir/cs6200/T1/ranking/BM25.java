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

/**
 *
 * @author smitha
 * @info Calculates BM25 scores for documents and ranks them and stores
 *       topNRankedDocs in file in cacm result format
 *
 */
public class BM25 {
	final static Logger logger = Logger.getLogger(BM25.class);

	public BM25(double k1, double b, double k2, int topNRankedDocs, String queryResultDir) {
		this.k1 = k1;
		this.b = b;
		this.k2 = k2;
		this.topNRankedDocs = topNRankedDocs;
		this.queryResultDir = queryResultDir;
	}

	double k1;
	double b;
	double k2;
	double avdl;
	int topNRankedDocs;
	/**
	 * Total number of documents in the corpus
	 */
	int N;

	long totalDocLenCorpus;

	/**
	 * Stores the invertedIndex for Unigram after reading data from disk
	 */
	HashMap<String, HashMap<String, Integer>> invertedLists;

	/**
	 * Stores the document length(Number of words) for each document after
	 * reading data from the file
	 */
	HashMap<String, Long> documentLenHm;

	String queryResultDir;

	public double getAvdl() {
		return avdl;
	}

	public void setAvdl(double avdl) {
		this.avdl = avdl;
	}

	public int getN() {
		return N;
	}

	public void setN(int n) {
		N = n;
	}

	public HashMap<String, HashMap<String, Integer>> getInvertedLists() {
		return invertedLists;
	}

	public void setInvertedLists(HashMap<String, HashMap<String, Integer>> invertedLists) {
		this.invertedLists = invertedLists;
	}

	public HashMap<String, Long> getDocumentLenHm() {
		return documentLenHm;
	}

	public void setDocumentLenHm(HashMap<String, Long> documentLenHm, long totalDocLenCorpus) {
		this.documentLenHm = documentLenHm;

		/**
		 * Set N(Number of documents) and Average Document Length of the corpus
		 */
		setTotalDocLenCorpus(totalDocLenCorpus);
		setN(documentLenHm.size());

		if (this.totalDocLenCorpus == 0) {
			logger.error("totalDocLenCorpus not yet calulated");
		}
		setAvdl(this.totalDocLenCorpus / documentLenHm.size());
	}

	public long getTotalDocLenCorpus() {
		return totalDocLenCorpus;
	}

	public void setTotalDocLenCorpus(long totalDocLenCorpus) {
		this.totalDocLenCorpus = totalDocLenCorpus;
	}

	/**
	 * Calculate K
	 */
	private double calculateK(String docId) {
		return this.k1 * ((1 - this.b) + this.b * (documentLenHm.get(docId) / avdl));
	}

	/**
	 * Outputs the list of top 100 ranked documents for a given query using BM25
	 *
	 * @param queryStr
	 * @param fileNameAppender
	 */
	public void searchBM25(String queryStr, int qNum, String sysName, String fileNameAppender) {

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

				queryScore += bM25ScorePerDocumentPerQTerm(docId, queryTerm, qTermFre.get(queryTerm));
			}
			rankScoreHm.put(docId, queryScore);
		}

		writeTopNDocsBM25Score(rankScoreHm, qNum, sysName, fileNameAppender);
	}

	/**
	 * Writes topN ranked BM25 score documents to file in cacm result format
	 *
	 * @param rankScoreHm
	 * @param qNum
	 * @param sysName
	 * @param fileNameAppender
	 */
	private void writeTopNDocsBM25Score(HashMap<String, Double> rankScoreHm, int qNum, String sysName,
			String fileNameAppender) {
		ListMultimap<Double, String> sortedRanks = SortUtils.sortMostToLeastScore(rankScoreHm);
		int topRanked = 1;

		try {
			File fileQResBM25 = new File(this.queryResultDir + "/Q" + qNum + "_" + fileNameAppender);

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
			logger.error("writeTopNDocsBM25Score Error in Writing", e);
		}
	}

	/**
	 * Apply BM25 formula for a single query term
	 *
	 * @param docId
	 *            Document Id
	 * @param queryTerm
	 *            Query Term
	 * @param qfi
	 *            Query Term Frequency in the Query
	 * @return
	 */
	private double bM25ScorePerDocumentPerQTerm(String docId, String queryTerm, int qfi) {

		/**
		 * No relevance information given
		 */
		int ri = 0;
		int R = 0;

		/**
		 * Number of documents the query term is found in the corpus 0 if query
		 * term not found in corpus
		 */
		int ni = 0;
		if (invertedLists.get(queryTerm) != null) {
			ni = invertedLists.get(queryTerm).size();
		}

		/**
		 * Term frequency of the query term in the given document docId Value is
		 * 0 if the term is not found in the document
		 */
		int fi = 0;
		try {
			if (invertedLists.get(queryTerm) != null && invertedLists.get(queryTerm).get(docId) != null) {
				fi = invertedLists.get(queryTerm).get(docId);
			}
		} catch (NullPointerException e) {
			logger.error("queryTerm:" + queryTerm + "docId:" + docId);
		}

		/**
		 * Compute K for this document docId
		 */
		double K = calculateK(docId);
		if (K == Double.POSITIVE_INFINITY || K == Double.NEGATIVE_INFINITY) {
			logger.error("Unable to calcuate K for Query Term " + queryTerm + "Doc " + docId);
		}

		double first = Math.log(((ri + 0.5) / (R - ri + 0.5)) / ((ni - ri + 0.5) / (this.N - ni - R + ri + 0.5)));
		double second = (((this.k1 + 1) * fi) / (K + fi));
		double third = ((this.k2 + 1) * qfi) / (this.k2 + qfi);

		return first * second * third;
	}

	public void runBM25(HashMap<Integer, String> queries, IndexedDataReader indexReader, String fileNameAppender) {
		logger.info("Running BM25");

		this.setInvertedLists(indexReader.getInvertedLists());
		this.setDocumentLenHm(indexReader.getDocumentLenHm(), indexReader.getTotalDocLenCorpus());

		logger.debug("Document Length HM Size " + this.getDocumentLenHm().size() + "totalDocLenCorpus "
				+ this.totalDocLenCorpus);
		for (int qNum : queries.keySet()) {
			logger.debug("Running BM25 ranking for query :" + queries.get(qNum));
			this.searchBM25(queries.get(qNum), qNum, IR_SystemName, fileNameAppender);
			logger.debug("");
		}
	}

}
