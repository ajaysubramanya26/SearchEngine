package neu.ir.cs6200.T1.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
 *
 */
public class Parser {

	final static Logger logger = Logger.getLogger(Parser.class);

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
	 * Remove divtags and html tags that do not contribute to the data
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

}