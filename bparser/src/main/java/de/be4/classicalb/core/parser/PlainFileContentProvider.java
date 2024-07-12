package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.util.Utils;

import java.io.File;
import java.io.IOException;

public class PlainFileContentProvider implements IFileContentProvider {

	public PlainFileContentProvider() {
	}

	@Override
	public String getFileContent(File directory, String filename) throws IOException {
		// TODO: caching could help to speed up if files are used more than once
		// Caching is done for DEFINITIONS in PreParser.java
		File file = this.getFile(directory, filename);
		return Utils.readFile(file);
	}

	@Override
	public File getFile(final File directory, final String filename) throws IOException {
		FileSearchPathProvider provider;
		if ((new File(filename).isAbsolute())) {
		    // FileSearchPathPathProvider/2 and java.io.File/2 constructor are not capable on their own to detect when filename is absolute
		    // maybe we should add isAbsolute check to FileSearchPathProvider/3 constructor
			provider = new FileSearchPathProvider("",filename);
		} else if (directory == null) {
			provider = new FileSearchPathProvider(filename); // this will use "." as parentPath
		} else {
			String parentPath;
			parentPath = directory.getCanonicalPath();
			provider = new FileSearchPathProvider(parentPath, filename);
		}
		return provider.resolve();
	}
}
