/**
 * 
 */
package neu.ir.cs6200.querydata;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.lucene.wordnet.SynonymMap;

import neu.ir.cs6200.T1.indexer.IndexedDataReader;
import neu.ir.cs6200.constants.Const_FilePaths;

/**
 * Class to perform the Theasuri Query Expansion.
 * 
 * @author kamlendrak
 *
 */
public class SynonymQueryExpansion {

	private static Logger logger = Logger.getLogger(PseudoRevelance.class);

	private static SynonymMap wordSynonymsMap = loadSynonyms();

	/**
	 * Loads the synonyms from the file into memory.
	 * 
	 * @return
	 */
	private static SynonymMap loadSynonyms() {
		SynonymMap map = null;
		try {
			map = new SynonymMap(new FileInputStream(Const_FilePaths.SYNONYM_FILE));
		} catch (IOException e) {
			logger.error("Could not load the synonym file");
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * Returns the expanded queries.
	 * 
	 * @param rawQueries
	 * @return
	 */
	public static Map<Integer, String> expandQueries(Map<Integer, String> rawQueries, IndexedDataReader index) {
		if (rawQueries == null || rawQueries.size() == 0) {
			return rawQueries;
		}
		Map<Integer, String> expandedQueries = new HashMap<Integer, String>();
		for (Entry<Integer, String> rawQuery : rawQueries.entrySet()) {
			expandedQueries.put(rawQuery.getKey(), expandQuery(rawQuery.getValue(), index));
		}
		return expandedQueries;
	}

	/**
	 * Expands the given query
	 * 
	 * @param query
	 * @return
	 */
	private static String expandQuery(String query, IndexedDataReader index) {
		if (query == null || query.trim().length() == 0) {
			return query;
		}
		StringBuilder builder = new StringBuilder();
		String[] queryTerms = query.split("\\s");
		for (String queryTerm : queryTerms) {
			builder.append(expandQueryTerm(queryTerm, index) + " ");
		}
		logger.info("Query Expansion : \n" + query + "\n" + builder.toString().trim());
		return builder.toString().trim();
	}

	/**
	 * Expands the given query term.
	 * 
	 * @param queryTerm
	 * @return
	 */
	private static String expandQueryTerm(String queryTerm, IndexedDataReader index) {
		StringBuilder expandedQueryTerm = new StringBuilder();
		String[] synonyms = wordSynonymsMap.getSynonyms(queryTerm);
		int count = 0;
		for (String synonym : synonyms) {
			// long synonymFrequency =
			// index.getTermFrequencyCourpus().get(synonym);
			expandedQueryTerm.append(synonym + " ");
			count++;
			if (count == 5) {
				break;
			}
		}
		return expandedQueryTerm.toString().trim();
	}
}
