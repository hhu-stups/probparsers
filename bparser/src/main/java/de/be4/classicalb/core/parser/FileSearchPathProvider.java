package de.be4.classicalb.core.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class FileSearchPathProvider implements Iterable<File> {
	private final String fileName;
	private ArrayList<String> searchPath = new ArrayList<>();

	public FileSearchPathProvider(String fileName) {
		this(".", fileName);
	}

	public FileSearchPathProvider(String prefix, String fileName) {
		this(prefix, fileName, Collections.emptyList());
	}

	public FileSearchPathProvider(String prefix, String fileName, List<String> paths) {
		this.fileName = fileName;

		searchPath.add(prefix);
		searchPath.addAll(paths);
		searchPath.addAll(getLibraryPath());
	}

	private List<String> getLibraryPath() {
		// User provided stdlib search path
		final String stdlib = System.getProperty("prob.stdlib");
		if (stdlib != null) {
			return Arrays.asList(stdlib.split(File.pathSeparator));
		} else {
			return Collections.singletonList("." + File.separator + "stdlib");
		}
	}

	@Override
	public Iterator<File> iterator() {
		return new SearchPathIterator();
	}

	public int size() {
		return this.searchPath.size();
	}

	public String getFilename() {
		return fileName;
	}

	public File resolve() throws IOException {
		for (File f : this) {

			if (f.exists() && f.isFile()) {
				return f.getCanonicalFile();
			}
		}
		throw new FileNotFoundException("did not found: " + fileName );
	}

	private class SearchPathIterator implements Iterator<File> {
		private int idx;

		public SearchPathIterator() {
			this.idx = 0;
		}

		@Override
		public boolean hasNext() {
			return this.idx < FileSearchPathProvider.this.size();
		}

		@Override
		public File next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			String base = get(this.idx);
			this.idx += 1;
			return new File(base, FileSearchPathProvider.this.getFilename());
		}

		private String get(int idx) {
			return FileSearchPathProvider.this.searchPath.get(idx);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
