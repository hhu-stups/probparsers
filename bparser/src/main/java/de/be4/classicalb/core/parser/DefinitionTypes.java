package de.be4.classicalb.core.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefinitionTypes {
	private final Map<String, Definitions.Type> types;

	public DefinitionTypes() {
		this(Collections.emptyMap());
	}

	public DefinitionTypes(final Map<String, Definitions.Type> newTypes) {
		this.types = new HashMap<>(newTypes);
	}

	public void addTyping(final String definitionName,
			final Definitions.Type type) {
		types.put(definitionName, type);
	}

	public void addAll(final Map<String, Definitions.Type> newTypes) {
		types.putAll(newTypes);
	}

	public Definitions.Type getType(final String definitionName) {
		return types.getOrDefault(definitionName, IDefinitions.Type.NoDefinition);
	}
	
	@Override
	public String toString(){
		return types.toString();
	}
}
