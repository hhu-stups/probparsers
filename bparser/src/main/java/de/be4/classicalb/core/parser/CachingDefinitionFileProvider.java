package de.be4.classicalb.core.parser;

import java.util.HashMap;
import java.util.Map;

public class CachingDefinitionFileProvider extends PlainFileContentProvider
		implements IDefinitionFileProvider {

	private final Map<String, IDefinitions> store = new HashMap<>();

	/**
	 * s. {@link PlainFileContentProvider#PlainFileContentProvider()}
	 */
	public CachingDefinitionFileProvider() {
		super();
	}

	@Override
	public IDefinitions getDefinitions(final String fileName) {
		return store.get(fileName);
	}

	@Override
	public void storeDefinition(final String fileName,
			final IDefinitions definitions) {
		store.put(fileName, definitions);
	}
}
