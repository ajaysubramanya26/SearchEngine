package neu.ir.cs6200.querydata;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import neu.ir.cs6200.T1.parser.Parser;

/**
 *
 * @author smitha
 * @author ajay
 * @author kamlendra
 * @info This class reads query file in cacm format and stores raw_queries in
 *       HashMap with query number and query string
 */
public class QueryDataReader {

	public HashMap<Integer, String> raw_queries;
	final static Logger logger = Logger.getLogger(QueryDataReader.class);

	public HashMap<Integer, String> getRaw_queries() {
		return raw_queries;
	}

	public void setRaw_queries(HashMap<Integer, String> raw_queries) {
		this.raw_queries = raw_queries;
	}

	public void readQueryDocument(String filePath) {
		try {
			List<String> allLines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
			raw_queries = new HashMap<>();

			boolean startDoc = false;
			boolean startDocNo = false;
			int totalNumOfLines = allLines.size();
			int curLineNum = 0;
			int totalDOCTags = 0;
			int totalDOCNOTags = 0;
			String docNum = "";
			int qNum = 0;
			int sNum = "<DOCNO>".length();
			while (curLineNum < totalNumOfLines) {
				startDoc = false;
				startDocNo = false;
				if (allLines.get(curLineNum).trim().equals("<DOC>")) {
					curLineNum++;
					docNum = allLines.get(curLineNum).trim();
					startDoc = true;
				}

				if (startDoc && docNum.startsWith("<DOCNO>") && docNum.endsWith("</DOCNO>")) {
					qNum = Integer
							.parseInt(docNum.substring(sNum, docNum.indexOf("</DOCNO>")).trim().replaceAll(" ", ""));
					startDocNo = true;
					curLineNum++;
				}

				if (startDoc && startDocNo && curLineNum < totalNumOfLines) {
					StringBuilder queryStr = new StringBuilder();
					while (curLineNum < totalNumOfLines && !allLines.get(curLineNum).trim().equals("</DOC>")) {
						queryStr.append(allLines.get(curLineNum).trim() + " ");
						curLineNum++;
					}
					raw_queries.put(qNum, parseQuery(queryStr.toString().trim()));
					totalDOCTags++;
					totalDOCNOTags++;
				}
				curLineNum++;
			}
			if (totalDOCTags % 2 != 0 && totalDOCNOTags % 2 != 0) {
				logger.info("All query not processed in Query document");
			} else {
				logger.info("Queries found:" + totalDOCTags + " Query document:" + filePath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Parse the Query through same steps as documents
	 *
	 * @param query
	 * @return
	 */
	public String parseQuery(String query) {
		return Parser.textCleanUp(query).toString();
	}

	/**
	 * Computes Query term frequency
	 *
	 * @param queryStr
	 * @param qTermFre
	 */
	public static void computeQueryTermFre(String queryStr, HashMap<String, Short> qTermFre) {
		String[] queryTerms = queryStr.split(" ");
		for (String qTerm : queryTerms) {
			if (qTermFre.containsKey(qTerm)) {
				qTermFre.put(qTerm, (short) (qTermFre.get(qTerm) + 1));
			} else {
				qTermFre.put(qTerm, (short) 1);
			}
		}
	}
}