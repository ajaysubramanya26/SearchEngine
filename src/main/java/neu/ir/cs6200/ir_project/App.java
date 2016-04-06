package neu.ir.cs6200.ir_project;

import static neu.ir.cs6200.constants.Const_FilePaths.CorpusDirLoc;
import static neu.ir.cs6200.constants.Const_FilePaths.ParsedDirName;

import java.io.File;

import neu.ir.cs6200.parser.Parser;
import neu.ir.cs6200.utils.FileUtils;

/**
 * Main class for SKA Information Reterival System
 *
 * @author smitha
 * @author ajay
 * @author kamlendra
 */
public class App {
	public static void main(String[] args) {

		/** Deletes all results from previous run and creates new directories */
		FileUtils.resultsDirFileSetUp();

		/** Use CACM corpus folder to parsed document */
		File dirCorpus = new File(CorpusDirLoc);
		if (!dirCorpus.exists()) {
			System.err.println("Corpus Directory not found!!" + CorpusDirLoc);
			return;
		}

		Parser.parseStore(CorpusDirLoc, ParsedDirName);
	}

}
