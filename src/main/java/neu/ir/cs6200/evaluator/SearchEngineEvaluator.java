/**
 * 
 */
package neu.ir.cs6200.evaluator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

	/**
	 * Evaluates the search results for the given query.
	 * 
	 * @param queryId
	 * @param mode
	 */
	public void evaluate(int queryId, Mode mode) {
		List<String> searchResults = loadQuerySearchResult(queryId, mode);
		Set<String> relevanceGroundTruth = this.relevanceGroundTruth.get(queryId);
		if (relevanceGroundTruth.isEmpty()) {
			LOGGER.error("No ground truth found for query :" + queryId + " - " + mode.mode);
			return;
		}
		int totalRelevantDocuments = relevanceGroundTruth.size();
		int relevantCount = 0;

		List<EvaluationResult> result = new ArrayList<EvaluationResult>();
		for (int i = 0; i < searchResults.size(); i++) {
			relevantCount = relevanceGroundTruth.contains(searchResults.get(i)) ? relevantCount + 1 : relevantCount;
			result.add(new EvaluationResult(i, (double) relevantCount / (i + 1),
					(double) relevantCount / totalRelevantDocuments));
		}
		System.out.println("Doe");
	}

	/**
	 * Loads the search result for a given query.
	 * 
	 * @param queryId
	 * @param mode
	 *            retrieval model
	 * @return
	 */
	private List<String> loadQuerySearchResult(int queryId, Mode mode) {
		List<String> searchResults = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(getSearchResultFileName(queryId, mode))));
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
	private String getSearchResultFileName(int queryId, Mode mode) {
		StringBuilder builder = new StringBuilder(Const_FilePaths.QUERY_RESULT_FILE_PATH);
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
