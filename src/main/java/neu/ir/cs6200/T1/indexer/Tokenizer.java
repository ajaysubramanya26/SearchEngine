package neu.ir.cs6200.T1.indexer;

import static neu.ir.cs6200.constants.Const_FilePaths.DocLenFname;
import static neu.ir.cs6200.constants.Const_FilePaths.InvertedIndexDirName;
import static neu.ir.cs6200.constants.Const_FilePaths.InvertedIndexFName_DF;
import static neu.ir.cs6200.constants.Const_FilePaths.InvertedIndexFName_TF;
import static neu.ir.cs6200.constants.Const_FilePaths.InvertedIndexFName_Uni;
import static neu.ir.cs6200.constants.Const_FilePaths.TokenizerDirName;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import neu.ir.cs6200.utils.FileUtils;

/**
 * Tokenizer and InvertedIndex formation
 *
 * @author smitha
 *
 */
public class Tokenizer {

	final static Logger logger = Logger.getLogger(Tokenizer.class);

	/**
	 * Reads documents, tokenize and put to invertedIndex
	 *
	 * @param parserOutput
	 *            - cleaned files from parser
	 * @param N
	 *            - words (Uni, Bi, Tri etc)
	 */
	public static void tokenizeIndex(String parserOutput, int N) {

		logger.info("In tokenizeIndex");
		FileUtils.deleteFolder(TokenizerDirName);
		FileUtils.createDirectory(TokenizerDirName);
		FileUtils.deleteFolder(InvertedIndexDirName);
		FileUtils.createDirectory(InvertedIndexDirName);

		File parserFolder = new File(parserOutput);
		File[] listOfFiles = null;
		if (parserFolder.isDirectory()) {
			listOfFiles = parserFolder.listFiles();
			InvertedIndex indexer = new InvertedIndex();
			for (File file : listOfFiles) {
				/*Read File*/
				StringBuilder docContents = new StringBuilder();
				try {
					List<String> lines = Files.readLines(file, Charsets.UTF_8);
					for (String eachLine : lines) {
						docContents.append(eachLine);
						docContents.append(" ");
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				tokenizeInvertedIndex(docContents, file.getName().replaceAll(".txt", ""), indexer, N);
			}

			indexer.storeInvertedIndex(indexer.invertedIndex, InvertedIndexFName_Uni);
			indexer.storeTermFrequency(indexer.invertedIndex, InvertedIndexFName_TF + "_N" + N);
			indexer.storeDocFrequencyPerTerm(indexer.invertedIndex, InvertedIndexFName_DF + "_N" + N);

		}
	}

	/**
	 * Reads each file, tokenizes and stores in the Inverted Index
	 * 
	 * @param sb
	 * @param docId
	 * @param indexer
	 * @param N
	 */
	public static void tokenizeInvertedIndex(StringBuilder sb, String docId, InvertedIndex indexer, int N) {
		String[] raw_words = sb.toString().split("[\\s]+");

		switch (N) {
		case 1:
			HashMap<String, Long> hm = new HashMap<>();
			termFrequencyFileN1(raw_words, hm);
			storeTokensEachDoc(docId, TokenizerDirName, hm);
			indexer.addToInvertedIndex(hm, indexer.invertedIndex, docId);
			break;

		default:
			logger.error("Not supported for N " + N);
		}
	}

	/**
	 * Stores tokens for each Document
	 *
	 * @param docId
	 * @param dirName
	 * @param hm
	 */
	private static void storeTokensEachDoc(String docId, String dirName, HashMap<String, Long> hm) {

		File file = new File(dirName + "/" + docId);
		File fileDocLen = new File(DocLenFname);
		int docLen = 0;
		try {

			for (String word : hm.keySet()) {
				docLen += hm.get(word);
				Files.append(new StringBuilder(word + "," + hm.get(word) + "\n"), file, Charsets.UTF_8);
			}

			Files.append(new StringBuilder(docId + "," + docLen + "\n"), fileDocLen, Charsets.UTF_8);
		} catch (IOException e) {
			logger.error("Tokenizer and DocLen Files not created", e);
		}

	}

	/**
	 * Store term Frequency per file
	 *
	 * @param words
	 * @param hm
	 */
	private static void termFrequencyFileN1(String[] words, HashMap<String, Long> hm) {
		for (int i = 0; i < words.length; i++) {
			if (words[i].equals("")) continue;
			if (hm.containsKey(words[i])) {
				hm.put(words[i], hm.get(words[i]) + 1);
			} else {
				hm.put(words[i], (long) 1);
			}
		}
	}
}
