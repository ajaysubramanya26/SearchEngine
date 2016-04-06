package neu.ir.cs6200.parser;

import static neu.ir.cs6200.constants.Const_FilePaths.TokenizerDirName;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import neu.ir.cs6200.utils.FileUtils;

/**
 * Converts Raw wikipedia file to file containing only title(s) and text
 * Ignoring markup notation (HTML tags), URLs, references to images, tables,
 * formulas, and navigational components.
 *
 * @author smitha
 *
 */
public class Parser {

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
				FileUtils.deleteFolder(TokenizerDirName);

				// read each file and submit it to worker thread to process
				for (File file : listOfSaneFiles) {
					Document doc = Jsoup.parse(file, "UTF-8");

					cleanUpTagsandDivs(doc);

					StringBuilder firstPassCleanUp = removePunctation(caseFold(doc.body().text()));
					StringBuilder numericCleanUp = new StringBuilder();
					String[] raw_words = firstPassCleanUp.toString().split("[\\s]+");
					for (int i = 0; i < raw_words.length; i++) {
						String tmp = raw_words[i];
						if (!isNumeric(raw_words[i]) && (raw_words[i].contains(".") || raw_words[i].contains(","))) {
							tmp = tmp.replaceAll("\\,", "");
							tmp = tmp.replaceAll("\\.", "");
						}
						numericCleanUp.append(tmp + " ");
					}

					Files.write(numericCleanUp, new File(dirParsedOp + "/" + cleanFileName(file)), Charsets.UTF_8);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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

		Elements eleOl = doc.select("ol.reflist");
		if (eleOl != null) eleOl.remove();

		Elements eleH2 = doc.select("h2");
		ArrayList<String> removeLst = new ArrayList<>(
				Arrays.asList("Navigation menu", "Contents", "References", "External links"));
		for (Element ele : eleH2) {
			for (String removeStr : removeLst) {
				Elements tmp = ele.getElementsContainingText(removeStr);
				tmp.remove();
			}
		}
	}

	/**
	 * Remove punctuation while retaining -., which will be processed later in
	 * one more step
	 *
	 * @param str
	 * @return
	 */
	private static StringBuilder removePunctation(StringBuilder str) {
		return new StringBuilder(java.util.regex.Pattern.compile("[^\\d\\w-., ]").matcher(str).replaceAll(""));
	}

	/**
	 * Convert document to lower case
	 *
	 * @param body
	 * @return
	 */
	private static StringBuilder caseFold(String body) {
		return new StringBuilder(body.toLowerCase());
	}

	/**
	 * match a number with optional '-' and decimal. and commas
	 */
	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?") || str.matches("-?\\d+([,]\\d+)*(\\.)?\\d+([,]\\d+)*");
	}

}
