package de.be4.classicalb.core.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DefinitionTypes {
	private final Map<String, IDefinitions.Type> types;

	public DefinitionTypes() {
		this(Collections.emptyMap());
	}

	public DefinitionTypes(Map<String, IDefinitions.Type> newTypes) {
		this.types = new HashMap<>();
		newTypes.forEach(this::addTyping);
	}

	public void addTyping(String definitionName, IDefinitions.Type type) {
		types.put(definitionName, type);
	}

	public void addAll(Map<String, IDefinitions.Type> newTypes) {
		types.putAll(newTypes);
	}

	public IDefinitions.Type getType(String definitionName) {
		return types.getOrDefault(definitionName, IDefinitions.Type.NoDefinition);
	}
	
	@Override
	public String toString(){
		return types.toString();
	}
}
