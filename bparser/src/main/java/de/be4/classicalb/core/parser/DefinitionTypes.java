package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.util.Utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefinitionTypes {
	private final Map<String, Definitions.Type> types;

	public DefinitionTypes() {
		this(Collections.emptyMap());
	}

	public DefinitionTypes(final Map<String, Definitions.Type> newTypes) {
		this.types = new HashMap<>();
		newTypes.forEach(this::addTyping);
	}

	public void addTyping(final String definitionName, final Definitions.Type type) {
		types.put(Utils.unquoteIdentifier(definitionName), type);
	}

	public void addAll(final Map<String, Definitions.Type> newTypes) {
		types.putAll(newTypes);
	}

	public Definitions.Type getType(final String definitionName) {
		return types.getOrDefault(Utils.unquoteIdentifier(definitionName), IDefinitions.Type.NoDefinition);
	}
	
	@Override
	public String toString(){
		return types.toString();
	}
}
