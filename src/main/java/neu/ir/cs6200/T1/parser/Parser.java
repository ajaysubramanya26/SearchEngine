package neu.ir.cs6200.T1.parser;

import static neu.ir.cs6200.constants.Const_FilePaths.StopListLoc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import neu.ir.cs6200.utils.FileUtils;

/**
 * Converts cacm html file to file containing only title(s) and text Ignoring
 * markup notation (HTML tags), URLs, references to images, tables, formulas,
 * and navigational components.
 *
 * @author smitha
 * @author ajay
 */
public class Parser {

	private static Boolean useStopList;
	private static String[] stopWords;
	final static Logger logger = Logger.getLogger(Parser.class);

	Parser() {
		useStopList = false;
	}

	/**
	 * Parses all the .html files in dirCorpus and cleanup for each page is done
	 * retaining necessary text. Then the file is stored in the dirParsedOp
	 * folder as .txt files
	 *
	 *
	 * @param dirCorpus
	 * @param dirParsedOp
	 */
	public static void parseStore(String dirCorpus, String dirParsedOp) {

		logger.info("In parseStore");

		if (useStopList) {
			stopWords = getStopWords();
			logger.info("using stop words list : " + getUseStopList());
		}

		/** Read the page and get all the anchor tags */
		File[] listOfFiles = null;
		File folder = new File(dirCorpus);

		try {

			if (folder.isDirectory()) {
				listOfFiles = folder.listFiles();
				ArrayList<File> listOfSaneFiles = new ArrayList<>();
				for (File file : listOfFiles) {
					if (file.isFile() && file.getName().endsWith(".html")) {
						listOfSaneFiles.add(file);
					}
				}

				FileUtils.deleteFolder(dirParsedOp);
				FileUtils.createDirectory(dirParsedOp);

				// read each file and submit it to worker thread to process
				for (File file : listOfSaneFiles) {
					Document doc = Jsoup.parse(file, "UTF-8");

					cleanUpTagsandDivs(doc);

					StringBuilder strCleanUp = textCleanUp(doc.body().text());

					Files.write(strCleanUp, new File(dirParsedOp + "/" + cleanFileName(file)), Charsets.UTF_8);
				}
			}
		} catch (IOException e) {
			logger.error("Parsing error!", e);
		}
	}

	/**
	 *
	 * @param dir
	 *            the path of the file containing the stemmed corpus
	 * @param numDocs
	 *            the number of stemmed documents in the file
	 */
	public static void parseStmdCrps(String dir, int numDocs) {
		Map<Integer, String> stemmed = new HashMap<>();
		Map<String, Integer> docIdNumTokens = new HashMap<>();
		Map<String, Map<String, Integer>> uniGram = new HashMap<>();
		ArrayList<String> tokens;

		String corpus = null;

		try {
			corpus = org.apache.commons.io.FileUtils.readFileToString(new File(dir));
		} catch (IOException e) {
			logger.error("error while reading cacm stemmed file " + e.getMessage());
		}

		for (int i = 1; i <= numDocs; i++) {
			int start = i;
			int end = start + 1;
			String record = i != numDocs ? StringUtils.substringBetween(corpus, "# " + start, "# " + end)
			        : StringUtils.substringAfter(corpus, "# " + i);
			stemmed.put(i, textCleanUp(clean(record)).toString());
		}

		for (int i : stemmed.keySet()) {
			tokens = new ArrayList<String>();
			String fileName = Integer.toString(i);
			String content = stemmed.get(i);

			tokens = getTokensInFile(content);

			int numTokens = tokens.size();

			docIdNumTokens.put(fileName, numTokens);

			for (int j = 0; j < numTokens; j++) {
				String token = tokens.get(j).toLowerCase().trim();
				if (isValid(token)) addToTable(token, fileName, uniGram);
			}
		}

		logger.info("index size " + uniGram.size());

	}

	/**
	 * 
	 * @param content
	 *            the filtered HTML page contents
	 * @return a list of tokens in the given page
	 */
	private static ArrayList<String> getTokensInFile(String content) {
		ArrayList<String> tk = new ArrayList<String>();
		String lines[] = content.split("\\r?\\n");
		for (int j = 0; j < lines.length; j++) {
			String[] tkns = lines[j].split(" +");
			for (String t : tkns) {
				tk.add(applyRegex(t));
			}
		}
		return tk;
	}

	/**
	 * applies a set of regex rules to the given token to validate it
	 * 
	 * @param token
	 *            a string that needs to be validated
	 * @return the token after regex'n it
	 */
	private static String applyRegex(String token) {
		if (StringUtils.isNumeric(token)) return token;
		Pattern regex = Pattern.compile("^[0-9]+([,.][0-9]+)?$");
		Matcher matcher = regex.matcher(token);
		if (matcher.find()) return token;
		return token.replaceAll("(?![-])\\p{Punct}", "");
	}

	/**
	 * performs further checks on the data
	 * 
	 * @param token
	 *            the string which needs to be validated
	 * @return
	 */
	private static boolean isValid(String token) {
		if (token.equals("-")) return false;
		if (token.equals(",")) return false;
		if (token.trim().isEmpty()) return false;

		return true;
	}

	/**
	 * adds the given token and doc Id to the HashMap after performing minor
	 * checks
	 * 
	 * @param token
	 *            the [uni|bi|tri]gram term
	 * @param docId
	 *            the document in which the token is present
	 * @param index
	 *            [uniGram|biGram|triGram]
	 */
	private static void addToTable(String token, String docId, Map<String, Map<String, Integer>> index) {
		if (index.containsKey(token)) {
			if (index.get(token).containsKey(docId)) {
				int tf = index.get(token).get(docId) + 1;
				index.get(token).put(docId, tf);
			} else {
				index.get(token).put(docId, 1);
			}
		} else {
			Map<String, Integer> temp = new HashMap<>();
			temp.put(docId, 1);
			index.put(token, temp);
		}
	}

	/**
	 * removes the unwanted data from the corpus
	 *
	 * @param record
	 *            the record with junk
	 * @return the record that does not contain junk
	 */
	private static String clean(String record) {
		return StringUtils.substringBeforeLast(record, record.contains("pm") ? "pm" : "am");
	}

	/**
	 *
	 * @param doc
	 * @return cleans us the supplied doc
	 */
	public static StringBuilder textCleanUp(String doc) {
		StringBuilder firstPassCleanUp = new StringBuilder(doc.trim().toLowerCase());
		StringBuilder numericCleanUp = new StringBuilder();

		String[] raw_words = firstPassCleanUp.toString().split("[\\r\\n]+");
		boolean reachedEnd = false;
		for (int i = 0; i < raw_words.length; i++) {
			String[] tmp = removePunctation(raw_words[i].trim()).toString().split("[\\s]+");

			for (int j = 0; j < tmp.length; j++) {
				if (!isNumeric(tmp[j]) && ((tmp[j].contains(".") && !(tmp[j].endsWith(".") && tmp[j].length() == 2))
				        || tmp[j].contains(","))) {
					tmp[j] = tmp[j].replaceAll("\\,", "");
					tmp[j] = tmp[j].replaceAll("\\.", "");
				}

				// (useStopList && isStopWord(tmp[j])
				if (!(reachedEnd && isNumeric(tmp[j]))) {
					numericCleanUp.append(tmp[j] + " ");
					reachedEnd = false;
				}

				if (tmp[j].equals("pm") || tmp[j].equals("am")) {
					reachedEnd = true;
				}

			}
		}
		return numericCleanUp;
	}

	/**
	 * Produces fileName : The file name is the same as the article title,
	 * however, without underscores or hyphens, e.g.,
	 * http://en.wikipedia.org/wiki/Green_Energy GreenEnergy.txt
	 *
	 * @param file
	 * @return
	 */
	private static String cleanFileName(File file) {
		return file.getName().replaceAll(".html", ".txt").replaceAll("_", "").replaceAll("-", "").replaceAll("\\(", "")
		        .replaceAll("\\)", "").replaceAll(",", "");
	}

	/**
	 * Remove div tags and html tags that do not contribute to the data
	 *
	 * @param doc
	 */
	private static void cleanUpTagsandDivs(Document doc) {

		for (Element anchor : doc.select("a[href]")) {
			if (anchor.attr("href").startsWith("#")) {
				anchor.remove();
			}
		}
		Elements eleScript = doc.select("script");
		if (eleScript != null) eleScript.remove();

		Elements eleImg = doc.select("img");
		if (eleImg != null) eleImg.remove();

		Elements eleTable = doc.select("table.metadata");
		if (eleTable != null) eleTable.remove();

		Elements eleSpan = doc.select("span");
		if (eleSpan != null) eleSpan.remove();
	}

	/**
	 * Remove punctuation while retaining -., which will be processed later in
	 * one more step
	 *
	 * @param str
	 * @return
	 */
	private static StringBuilder removePunctation(String str) {
		return new StringBuilder(java.util.regex.Pattern.compile("[^\\d\\w-.:, ]").matcher(str).replaceAll(""));
	}

	/**
	 * match a number with optional '-' and decimal. and commas
	 */
	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?") || str.matches("-?\\d+([,]\\d+)*(\\.)?\\d+([,]\\d+)*");
	}

	/**
	 *
	 * @param str
	 * @return if the given string is a stop-word or not
	 */
	public static boolean isStopWord(String str) {
		return Arrays.asList(stopWords).contains(str);
	}

	/**
	 * retrieves the stop words from the provided file
	 *
	 * @return
	 */
	public static String[] getStopWords() {
		String stopWrdsFile = null;
		try {
			stopWrdsFile = org.apache.commons.io.FileUtils.readFileToString((new File(StopListLoc)));
		} catch (IOException e) {
			logger.error("Exception while reading stop list file + " + e.getMessage());
		}
		return stopWrdsFile.split("\n");
	}

	public static Boolean getUseStopList() {
		return useStopList;
	}

	public static void setUseStopList(Boolean useStopList) {
		Parser.useStopList = useStopList;
	}

}
