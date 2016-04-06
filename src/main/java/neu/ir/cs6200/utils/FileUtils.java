package neu.ir.cs6200.utils;

import static neu.ir.cs6200.constants.Const_FilePaths.Results;

import java.io.File;

public class FileUtils {
	/**
	 * Creates a directory if not present
	 *
	 * @param dirName
	 */
	public static void createDirectory(String dirName) {
		File file = new File(dirName);
		boolean result = file.mkdirs();
		if (result) {
			System.out.println("Directory is created!" + dirName);
		} else {
			System.err.println("Failed to create directory!" + dirName);
		}
	}

	public static void deleteFolder(String dirName) {
		File dir = new File(dirName);

		if (dir.exists() && dir.isDirectory()) {
			deleteFolderRecursive(dir);
		}
	}

	public static void deleteFolderRecursive(File folder) {
		File[] files = folder.listFiles();
		if (files != null) { // some JVMs return null for empty dirs
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolderRecursive(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}

	public static void resultsDirFileSetUp() {
		deleteFolder(Results);
	}
}
