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

/**
 * @author kamlendra
 * @author Smitha
 * @author Ajay
 */
public class SearchEngineEvaluator {

	enum Mode {

		BM25("BM25"), TFIDF("TFIDF"), LUCENE("Lucene");

		private final String mode;

		private Mode(String mode) {
			this.mode = mode;
		}

		public String mode() {
			return this.mode;
		}
	}

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
		Mode[] modes = new Mode[] {Mode.BM25, Mode.LUCENE, Mode.TFIDF};
		for(Mode mode : modes) {
			double totalAveragePrecision = 0;
			double totalReciprocalRank = 0;
			for(int i=1; i<=64 ; i++) {
				QueryEvaluationSummary querySummary = evaluate(i, mode);
				if(querySummary != null) {
					totalAveragePrecision += querySummary.getAveragePrecision();
					totalReciprocalRank += (double) 1 / querySummary.getReciprocalRank();
				}
 			}
			
		}
	}

	/**
	 * Evaluates the search results for the given query.
	 * @param queryId
	 * @param mode
	 */
	private QueryEvaluationSummary evaluate(int queryId, Mode mode) {
		List<String> searchResults = loadQuerySearchResult(queryId, mode);
		Set<String> relevanceGroundTruth = this.relevanceGroundTruth.get(queryId);
		if (relevanceGroundTruth.isEmpty()) {
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
		double averagePrecision = relevantRankFound ? (double) totalPrecision / relevantCount : 0;
		QueryEvaluationSummary summary = new QueryEvaluationSummary(averagePrecision, firstRelevantRank, result);
		System.out.println("Doe");
		return summary;
	}

	/**
	 * Write the result.
	 * @param result
	 * @param queryId
	 * @param mode
	 */
	private void writeRankWiseResults(List<EvaluationResult> result, int queryId, Mode mode) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(
					new File(getSearchResultFileName(Const_FilePaths.QUERY_EVALUATION_RESULT, queryId, mode))));
			for(EvaluationResult evalResult : result) {
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

	/**
	 * Loads the search result for a given query.
	 * @param queryId
	 * @param mode
	 *            retrieval model
	 * @return
	 */
	private List<String> loadQuerySearchResult(int queryId, Mode mode) {
		List<String> searchResults = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(
					new File(getSearchResultFileName(Const_FilePaths.QUERY_RESULT_FILE_PATH, queryId, mode))));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String document = line.split("\\s")[2];
				searchResults.add(document);
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
		return searchResults;
	}

	/**
	 * Forms the result file name to be read for evaluations.
	 * 
	 * @param queryId
	 * @param mode
	 * @return
	 */
	private String getSearchResultFileName(String folderPath, int queryId, Mode mode) {
		StringBuilder builder = new StringBuilder(folderPath);
		builder.append("Q");
		builder.append(queryId);
		builder.append("_");
		builder.append(mode.mode());
		return builder.toString();
	}

	public static void main(String[] args) {
		SearchEngineEvaluator eval = new SearchEngineEvaluator();
		eval.evaluate(1, Mode.TFIDF);
	}
}
