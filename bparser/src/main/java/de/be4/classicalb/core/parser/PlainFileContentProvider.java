package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.util.Utils;

import java.io.File;
import java.io.IOException;

public class PlainFileContentProvider implements IFileContentProvider {

	public PlainFileContentProvider() {
	}

	@Override
	public String getFileContent(File directory, String fileName) throws IOException {
		// TODO: caching could help to speed up if files are used more than once
		// Caching is done for DEFINITIONS in PreParser.java
		File file = this.getFile(directory, fileName);
		return Utils.readFile(file);
	}

	@Override
	public File getFile(final File directory, final String fileName) throws IOException {
		FileSearchPathProvider provider;
		if (directory == null) {
			provider = new FileSearchPathProvider(fileName); // this will use "." as parentPath
		} else {
			String parentPath;
			parentPath = directory.getCanonicalPath();
			provider = new FileSearchPathProvider(parentPath, fileName);
		}
		return provider.resolve();
	}
}
