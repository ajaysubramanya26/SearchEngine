/**
 *
 */
package neu.ir.cs6200.evaluator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import neu.ir.cs6200.constants.Const_FilePaths;
import neu.ir.cs6200.querydata.QueryResultReader;
import neu.ir.cs6200.utils.FileUtils;

/**
 * @author kamlendra
 * @author Smitha
 * @author Ajay
 */
public class SearchEngineEvaluator {

	private Map<Integer, Set<String>> relevanceGroundTruth;

	private static Logger LOGGER = Logger.getLogger(SearchEngineEvaluator.class);

	public SearchEngineEvaluator() {
		relevanceGroundTruth = loadGroundTruth();
	}

	/**
	 * Loads the relevance ground truth for evaluation.
	 *
	 * @return
	 */
	private Map<Integer, Set<String>> loadGroundTruth() {
		BufferedReader reader = null;
		Map<Integer, Set<String>> relevanceGroundTruth = new HashMap<Integer, Set<String>>();
		try {
			reader = new BufferedReader(new FileReader(new File(Const_FilePaths.CACM_RELEVANCE_FILE)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split("\\s");
				int queryId = Integer.parseInt(tokens[0]);
				String relevantDocument = tokens[2].replaceAll("-", "") + ".txt";
				Set<String> relevantSet = relevanceGroundTruth.containsKey(queryId) ? relevanceGroundTruth.get(queryId)
						: new HashSet<String>();
				relevantSet.add(relevantDocument);
				relevanceGroundTruth.put(queryId, relevantSet);
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
				e.printStackTrace();
			}
		}
		return relevanceGroundTruth;
	}

	public void evaluate() {

		FileUtils.deleteFolder(Const_FilePaths.QUERY_EVALUATION_RESULT_TASK4);
		FileUtils.createDirectory(Const_FilePaths.QUERY_EVALUATION_RESULT_TASK4);

		Mode[] modes = new Mode[] { Mode.BM25, Mode.LUCENE, Mode.TFIDF, Mode.PSEUDO_REL_QE };
		for (Mode mode : modes) {
			double totalAveragePrecision = 0;
			double totalReciprocalRank = 0;
			double totalPrecisionAtRank5 = 0;
			double totalPrecisionAtRank20 = 0;

			for (int i = 1; i <= 64; i++) {
				QueryEvaluationSummary querySummary = evaluate(i, mode);
				if (querySummary != null) {
					totalAveragePrecision += querySummary.getAveragePrecision();
					totalReciprocalRank += (double) 1 / querySummary.getReciprocalRank();
					totalPrecisionAtRank5 += querySummary.getResultAtRank(5).getPrecision();
					totalPrecisionAtRank20 += querySummary.getResultAtRank(20).getPrecision();
				}
			}
			writeResult(totalAveragePrecision, totalReciprocalRank, totalPrecisionAtRank5, totalPrecisionAtRank20, 64,
					mode);
		}
	}

	private void writeResult(double totalAveragePrecision, double totalReciprocalRank, double totalPrecisionAtRank5,
			double totalPrecisionAtRank20, int queryCount, Mode mode) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(
					new FileWriter(new File(Const_FilePaths.QUERY_EVALUATION_RESULT + mode.mode())));
			writer.write("MAP : " + totalAveragePrecision / queryCount + "\n");
			writer.write("MRR : " + totalReciprocalRank / queryCount + "\n");
			writer.write("P @ K5 : " + totalPrecisionAtRank5 / queryCount + "\n");
			writer.write("P @ K20 : " + totalPrecisionAtRank20 / queryCount + "\n");
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Evaluates the search results for the given query.
	 *
	 * @param queryId
	 * @param mode
	 */
	private QueryEvaluationSummary evaluate(int queryId, Mode mode) {
		List<String> searchResults = QueryResultReader.loadQuerySearchResult(queryId, mode);
		Set<String> relevanceGroundTruth = this.relevanceGroundTruth.get(queryId);
		if (relevanceGroundTruth == null || relevanceGroundTruth.isEmpty()) {
			System.out.println("No ground truth found for query :" + queryId + " - " + mode.mode);
			LOGGER.error("No ground truth found for query :" + queryId + " - " + mode.mode);
			return null;
		}
		int totalRelevantDocuments = relevanceGroundTruth.size();
		int relevantCount = 0;
		double totalPrecision = 0;
		int firstRelevantRank = 0;
		boolean relevantRankFound = false;

		List<EvaluationResult> result = new ArrayList<EvaluationResult>();
		for (int i = 0; i < searchResults.size(); i++) {
			boolean relevant = relevanceGroundTruth.contains(searchResults.get(i));
			relevantCount = relevant ? relevantCount + 1 : relevantCount;
			double precision = (double) relevantCount / (i + 1);
			double recall = (double) relevantCount / totalRelevantDocuments;
			if (relevant) {
				totalPrecision += precision;
				if (!relevantRankFound) {
					firstRelevantRank = i + 1;
					relevantRankFound = true;
				}
			}
			result.add(new EvaluationResult(i + 1, precision, recall));
		}
		writeRankWiseResults(result, queryId, mode);
		double averagePrecision = relevantRankFound ? totalPrecision / relevantCount : 0;
		QueryEvaluationSummary summary = new QueryEvaluationSummary(averagePrecision, firstRelevantRank, result);
		return summary;
	}

	/**
	 * Write the result.
	 *
	 * @param result
	 * @param queryId
	 * @param mode
	 */
	private void writeRankWiseResults(List<EvaluationResult> result, int queryId, Mode mode) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File(QueryResultReader
					.getSearchResultFileName(Const_FilePaths.QUERY_EVALUATION_RESULT_TASK4, queryId, mode))));
			for (EvaluationResult evalResult : result) {
				writer.write(evalResult.toString() + "\n");
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	// public static void main(String[] args) {
	// SearchEngineEvaluator eval = new SearchEngineEvaluator();
	// eval.evaluate();
	// System.out.println("Done");
	// }
}
