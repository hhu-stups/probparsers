package de.be4.classicalb.core.parser;

import java.io.File;
import java.io.IOException;

public class NoContentProvider implements IFileContentProvider {

	@Override
	public String getFileContent(File directory, String fileName)
			throws IOException {
		throw new IOException("Loading of file content not supported.");
	}

	@Override
	public File getFile(final File directory, String fileName) {
		return null;
	}


	
}
