package neu.ir.cs6200.T1.indexer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.collect.ListMultimap;
import com.google.common.io.Files;

import neu.ir.cs6200.utils.SortUtils;

/**
 * Maintains the InvertedIndex Data Structure and associated functions for
 * storing in file system
 *
 * @author smitha
 *
 */
public class InvertedIndex {

	HashMap<String, HashMap<String, Long>> invertedIndex;
	final static Logger logger = Logger.getLogger(InvertedIndex.class);

	public InvertedIndex() {
		this.invertedIndex = new HashMap<>();
	}

	/**
	 * Add each document's term and frequency to inverted index
	 *
	 * @param hm
	 * @param invertedIndex
	 * @param docId
	 */
	public void addToInvertedIndex(HashMap<String, Long> hm, HashMap<String, HashMap<String, Long>> invertedIndex,
			String docId) {
		for (String word : hm.keySet()) {
			if (invertedIndex.containsKey(word)) {
				invertedIndex.get(word).put(docId, hm.get(word));
			} else {
				HashMap<String, Long> newWordLst = new HashMap<>();
				newWordLst.put(docId, hm.get(word));
				invertedIndex.put(word, newWordLst);
			}
		}
	}

	/**
	 * Store the final inverted index
	 *
	 * @param invertedIndex
	 * @param docId
	 */
	public void storeInvertedIndex(HashMap<String, HashMap<String, Long>> invertedIndex, String fileName) {
		SortedSet<String> lexiWords = new TreeSet<String>(invertedIndex.keySet());

		File file = new File(fileName);
		for (String word : lexiWords) {
			HashMap<String, Long> row = invertedIndex.get(word);
			try {
				SortedSet<String> lexiDocIds = new TreeSet<String>(row.keySet());
				Files.append(new StringBuilder(word + " -> "), file, Charsets.UTF_8);
				int count = 0;
				String sep = ",";
				for (String docId : lexiDocIds) {
					if (++count == lexiDocIds.size()) sep = "";
					Files.append(new StringBuilder("(" + docId + "," + row.get(docId) + ")" + sep), file,
							Charsets.UTF_8);
				}
				Files.append(new StringBuilder("\n"), file, Charsets.UTF_8);
			} catch (IOException e) {
				logger.error("storeInvertedIndex ", e);
			}
		}
	}

	/**
	 * Generate term frequency table from the inverted index
	 *
	 * @param invertedIndex
	 * @param fileName
	 */
	public void storeTermFrequency(HashMap<String, HashMap<String, Long>> invertedIndex, String fileName) {

		HashMap<String, Long> termFrequency = new HashMap<>();
		for (String word : invertedIndex.keySet()) {
			HashMap<String, Long> row = invertedIndex.get(word);
			long termFre = 0;
			for (String docId : row.keySet()) {
				termFre += row.get(docId);
			}
			termFrequency.put(word, termFre);
		}

		/**
		 * Sort the tokens
		 */
		ListMultimap<Long, String> tf = SortUtils.sortMostToLeastFrequent(termFrequency);

		File file = new File(fileName);
		for (Long wordFre : tf.keySet()) {
			for (String word : tf.get(wordFre)) {
				try {
					Files.append(new StringBuilder(word + ":" + wordFre + "\n"), file, Charsets.UTF_8);
				} catch (IOException e) {
					logger.error("storeTermFrequency ", e);
				}
			}
		}
	}

	/**
	 * Store the document frequency for a term {docsId} frequency
	 *
	 * @param invertedIndex
	 * @param fileName
	 */
	public void storeDocFrequencyPerTerm(HashMap<String, HashMap<String, Long>> invertedIndex, String fileName) {

		SortedSet<String> lexiWords = new TreeSet<String>(invertedIndex.keySet());
		String sep = ",";
		File file = new File(fileName);
		for (String word : lexiWords) {
			try {
				HashMap<String, Long> row = invertedIndex.get(word);
				long count = 0;
				int num = 0;
				sep = ",";
				StringBuilder docIdLst = new StringBuilder("(");
				for (String docId : row.keySet()) {
					if (++num == row.keySet().size()) sep = ")";
					count++;
					docIdLst.append(docId + sep);
				}
				Files.append(new StringBuilder(word + " -> " + docIdLst + "," + count + "\n"), file, Charsets.UTF_8);
			} catch (IOException e) {
				logger.error("storeDocFrequencyPerTerm", e);

			}
		}
	}
}
