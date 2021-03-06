package neu.ir.cs6200.T1.indexer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

/**
 *
 * @author smitha
 * @info Contains functions to reads inverted index file into Data Structure
 *       HashMap<String, HashMap<String, Integer>> => Term, HashMap<DocId,
 *       Frequency> and DocumentLength file to read into Data Structure
 *       HashMap<String, Long>
 *
 */
public class IndexedDataReader {

	public IndexedDataReader() {
		this.invertedLists = new HashMap<String, HashMap<String, Integer>>();
		this.documentLenHm = new HashMap<String, Long>();
		this.totalTermsPerDoc = new HashMap<String, Long>();
		this.termFrequencyCourpus = new HashMap<String, Long>();
		this.totalDocLenCorpus = 0L;
	}

	final static Logger logger = Logger.getLogger(IndexedDataReader.class);

	HashMap<String, HashMap<String, Integer>> invertedLists;
	HashMap<String, Long> documentLenHm;
	HashMap<String, Long> termFrequencyCourpus;
	long totalDocLenCorpus;

	/**
	 * Stores the total number of terms per doc. Used to calculate term
	 * frequency
	 */
	HashMap<String, Long> totalTermsPerDoc;

	public HashMap<String, HashMap<String, Integer>> getInvertedLists() {
		return invertedLists;
	}

	public void setInvertedLists(HashMap<String, HashMap<String, Integer>> invertedLists) {
		this.invertedLists = invertedLists;
	}

	public HashMap<String, Long> getDocumentLenHm() {
		return documentLenHm;
	}

	public void setDocumentLenHm(HashMap<String, Long> documentLenHm) {
		this.documentLenHm = documentLenHm;
	}

	public long getTotalDocLenCorpus() {
		return totalDocLenCorpus;
	}

	public void setTotalDocLenCorpus(long totalDocLenCorpus) {
		this.totalDocLenCorpus = totalDocLenCorpus;
	}

	/**
	 * Read inverted Index. Stores invertedLists and also computes
	 * totalTermsPerDoc
	 *
	 * @param filePath
	 * @return
	 */
	public void deserializeInvertedIndex(String filePath) {
		try {
			System.out.println("file path" + filePath);
			List<String> rawInvertedList = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
			if (rawInvertedList.size() != 0) {
				for (String termDocIds : rawInvertedList) {
					String[] trDocs = termDocIds.split(" -> ");
					if (trDocs.length == 2) {
						HashMap<String, Integer> docIdFreHm = new HashMap<>();
						invertedLists.put(trDocs[0], docIdFreHm);
						String[] docIds = trDocs[1].replaceAll("\\(", "").split("\\)");
						for (String docIdFre : docIds) {
							if (docIdFre.startsWith(",")) docIdFre = docIdFre.replaceFirst(",", "");
							String[] docAndFre = docIdFre.split(",");
							try {
								if (docAndFre.length != 2) {
									logger.error("Too many tokens docId, term frequency :" + docIdFre);
								}
								if (docAndFre.length == 2) {
									int tf = Integer.parseInt(docAndFre[1]);
									docIdFreHm.put(docAndFre[0], tf);

									if (totalTermsPerDoc.containsKey(docAndFre[0])) {
										totalTermsPerDoc.put(docAndFre[0], totalTermsPerDoc.get(docAndFre[0]) + tf);
									} else {
										totalTermsPerDoc.put(docAndFre[0], (long) tf);
									}
								}
							} catch (NumberFormatException e) {
								logger.error("Number Format exception :" + docAndFre[1]);
							}
						}
					} else {
						logger.error("Wrong Format Expected Term -> (docId1,Fre), (docId2,Fre) Given " + termDocIds);
					}
				}
			}
			logger.info("Number of Lines Read from File:" + rawInvertedList.size() + " Number of entries in HashMap:"
					+ invertedLists.size());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, Long> getTotalTermsPerDoc() {
		return totalTermsPerDoc;
	}

	public void setTotalTermsPerDoc(HashMap<String, Long> totalTermsPerDoc) {
		this.totalTermsPerDoc = totalTermsPerDoc;
	}

	/**
	 * Read Document Length file
	 *
	 * @param filePath
	 * @return
	 */
	public void deserializeDocumentsLength(String filePath) {
		try {
			List<String> rawDocIdLen = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

			if (rawDocIdLen.size() != 0) {
				for (String docId : rawDocIdLen) {
					String[] docTotalWords = docId.split(",");
					try {
						if (docTotalWords.length == 2) {
							Long docLen = Long.parseLong(docTotalWords[1]);
							documentLenHm.put(docTotalWords[0], docLen);
							totalDocLenCorpus += docLen;
						} else {
							logger.error("Wrong tokens Expected 2" + docId);
						}
					} catch (NumberFormatException e) {
						logger.error("NumberFormatException Expected Long (docId, docLen)" + docId);
					}
				}

				logger.info("Documents Length Size :" + documentLenHm.size());
			} else {
				logger.error("DocumentLength file empty!" + filePath);
			}
		} catch (IOException e) {
			logger.error("Error in reading Document length file");
			e.printStackTrace();
		}
	}

	/**
	 * Get top in words(stopwords) front the term frequency file
	 *
	 * @param filePath
	 * @param topNStopWords
	 * @return
	 */
	public static List<String> getStopWords_TermFrequencyFile(String filePath, int topNStopWords) {
		List<String> stopWords = new ArrayList<>();
		try {
			List<String> rawTF = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
			for (int i = 0; i < topNStopWords; i++) {
				stopWords.add(rawTF.get(i).split(":")[0]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stopWords;
	}

	/**
	 * Get Map of term frequency file for entire coupus
	 *
	 * @param filePath
	 * @param topNStopWords
	 * @return
	 */
	public void getTermFrequencyCorpus(String filePath) {

		try {
			List<String> rawTF = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
			for (int i = 0; i < rawTF.size(); i++) {
				String[] terms = rawTF.get(i).split(":");
				this.termFrequencyCourpus.put(terms[0], Long.parseLong(terms[1]));
			}
		} catch (NullPointerException e) {
			logger.error("termFrequencyCourpus Null", e);
		} catch (IOException e) {
			logger.error("Could not read TermFrequency File", e);
		}
	}

	/**
	 * @return the termFrequencyCourpus
	 */
	public HashMap<String, Long> getTermFrequencyCourpus() {
		return termFrequencyCourpus;
	}

	/**
	 * @param termFrequencyCourpus the termFrequencyCourpus to set
	 */
	public void setTermFrequencyCourpus(HashMap<String, Long> termFrequencyCourpus) {
		this.termFrequencyCourpus = termFrequencyCourpus;
	}

}
