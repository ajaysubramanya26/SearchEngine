package neu.ir.cs6200.querydata;

import static neu.ir.cs6200.constants.Consts.DOCID_INDEX_QUERY_RESULT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import neu.ir.cs6200.constants.Const_FilePaths;
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
			reader = new BufferedReader(new FileReader(
					new File(getSearchResultFileName(Const_FilePaths.QUERY_RESULT_FILE_PATH, queryId, mode))));
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
