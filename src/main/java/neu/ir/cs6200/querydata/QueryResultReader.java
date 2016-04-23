package neu.ir.cs6200.querydata;

import static neu.ir.cs6200.constants.Const_FilePaths.Task1QueryResults;
import static neu.ir.cs6200.constants.Const_FilePaths.Task2QueryResults;
import static neu.ir.cs6200.constants.Const_FilePaths.Task3QueryStopWordsResults;
import static neu.ir.cs6200.constants.Const_FilePaths.TaskTable7Results;
import static neu.ir.cs6200.constants.Consts.DOCID_INDEX_QUERY_RESULT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import neu.ir.cs6200.evaluator.Mode;

/**
 * @author kamlendra
 * @info General class for reading query result from a file
 */
public class QueryResultReader {
	private static Logger LOGGER = Logger.getLogger(QueryResultReader.class);

	/**
	 * Loads the search result for a given query.
	 *
	 * @param queryId
	 * @param mode
	 *            retrieval model
	 * @return
	 */
	public static List<String> loadQuerySearchResult(int queryId, Mode mode) {
		List<String> searchResults = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			String dirPath = getDirectoryPath(mode) + "/";
			reader = new BufferedReader(new FileReader(new File(getSearchResultFileName(dirPath, queryId, mode))));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String document = line.split("\\s")[DOCID_INDEX_QUERY_RESULT];
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
	 * Since different tables are written under different directories get the
	 * directory associated with the mode.
	 *
	 * @param mode
	 * @return
	 */
	private static String getDirectoryPath(Mode mode) {

		if (mode == Mode.BM25 || mode == Mode.LUCENE || mode == Mode.TFIDF) {
			return Task1QueryResults;
		} else if (mode == Mode.PSEUDO_REL_QE || mode == Mode.SYN_QE) {
			return Task2QueryResults;
		} else if (mode == Mode.STOPPING) {
			return Task3QueryStopWordsResults;
		} else if (mode == Mode.T7) {
			return TaskTable7Results;
		}
		return null;
	}

	/**
	 * Forms the result file name to be read for evaluations.
	 *
	 * @param queryId
	 * @param mode
	 * @return
	 */
	public static String getSearchResultFileName(String folderPath, int queryId, Mode mode) {
		StringBuilder builder = new StringBuilder(folderPath);
		builder.append("Q");
		builder.append(queryId);
		builder.append("_");
		builder.append(mode.mode());
		return builder.toString();
	}

}
