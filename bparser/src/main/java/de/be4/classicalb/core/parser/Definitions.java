package de.be4.classicalb.core.parser;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.be4.classicalb.core.parser.analysis.prolog.INodeIds;
import de.be4.classicalb.core.parser.exceptions.PreParseException;
import de.be4.classicalb.core.parser.node.AExpressionDefinitionDefinition;
import de.be4.classicalb.core.parser.node.APredicateDefinitionDefinition;
import de.be4.classicalb.core.parser.node.ASubstitutionDefinitionDefinition;
import de.be4.classicalb.core.parser.node.PDefinition;
import de.hhu.stups.sablecc.patch.SourcePosition;

public class Definitions extends IDefinitions {

	private final Map<String, PDefinition> definitionsMap = new HashMap<>();
	private final Map<String, Type> types = new HashMap<>();
	private final File file;

	public Definitions() {
		this.file = null;
	}

	public Definitions(File file) {
		this.file = file;
	}

	@Override
	public Map<String, Type> getTypes() {
        final Map<String, Type> map = new HashMap<>(types);
		for (IDefinitions definitions : referencedDefinitions) {
			map.putAll(definitions.getTypes());
		}
		return map;
	}

	@Override
	public int getParameterCount(final String defName) {
		final PDefinition defNode = getDefinition(defName);
		return getParameterCount(defNode);
	}

	private int getParameterCount(PDefinition defNode) {
		if (defNode instanceof APredicateDefinitionDefinition)
			return ((APredicateDefinitionDefinition) defNode).getParameters().size();
		else if (defNode instanceof ASubstitutionDefinitionDefinition)
			return ((ASubstitutionDefinitionDefinition) defNode).getParameters().size();
		else if (defNode instanceof AExpressionDefinitionDefinition)
			return ((AExpressionDefinitionDefinition) defNode).getParameters().size();
		else
			return -1;

	}

	@Override
	public Type getType(final String defName) {
		if (types.containsKey(defName)) {
			return types.get(defName);
		} else {
			for (IDefinitions definitions : this.referencedDefinitions) {
				final Type type = definitions.getType(defName);
				if (type != Type.NoDefinition) {
					return type;
				}
			}
		}
		return Type.NoDefinition;
	}

	@Override
	public Set<String> getDefinitionNames() {
        Set<String> resultSet = new HashSet<>(definitionsMap.keySet());
		for (IDefinitions iDefinitions : referencedDefinitions) {
			resultSet.addAll(iDefinitions.getDefinitionNames());
		}
		return resultSet;
	}

	@Override
	public PDefinition getDefinition(final String defName) {
		if (definitionsMap.containsKey(defName)) {
			return definitionsMap.get(defName);
		} else {
			for (IDefinitions iDefinitions : referencedDefinitions) {
				if (iDefinitions.containsDefinition(defName)) {
					return iDefinitions.getDefinition(defName);
				}
			}
		}
		throw new AssertionError(getErrorMessageDefinitionDoesNotExist(defName));
	}

	private String getErrorMessageDefinitionDoesNotExist(String defName) {
		return String.format("Definition %s does not exist.", defName);
	}

	@Override
	public File getFile(final String defName) {
		if (definitionsMap.containsKey(defName)) {
			return this.file;
		} else {
			for (IDefinitions iDefinitions : referencedDefinitions) {
				if (iDefinitions.containsDefinition(defName)) {
					return iDefinitions.getFile(defName);
				}
			}
		}
		throw new AssertionError(getErrorMessageDefinitionDoesNotExist(defName));
	}

	@Override
	public boolean containsDefinition(String defName) {
		if (definitionsMap.containsKey(defName)) {
			return true;
		} else {
			for (IDefinitions iDefinitions : referencedDefinitions) {
				if (iDefinitions.containsDefinition(defName)) {
					iDefinitions.getDefinition(defName);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void setDefinitionType(String defName, Type expression) {
		if (this.types.containsKey(defName)) {
			types.put(defName, expression);
			return;
		} else {
			for (IDefinitions iDefinitions : referencedDefinitions) {
				if (iDefinitions.containsDefinition(defName)) {
					iDefinitions.setDefinitionType(defName, expression);
					return;
				}
			}
		}
		throw new AssertionError(getErrorMessageDefinitionDoesNotExist(defName));
	}

	@Override
	public void replaceDefinition(final String defName, final Type type, final PDefinition node) {
		if (types.containsKey(defName)) {
			types.put(defName, type);
			definitionsMap.put(defName, node);
			return;
		} else {
			for (IDefinitions iDefinitions : referencedDefinitions) {
				if (iDefinitions.containsDefinition(defName)) {
					iDefinitions.replaceDefinition(defName, type, node);
					return;
				}
			}
		}
		throw new AssertionError(getErrorMessageDefinitionDoesNotExist(defName));
	}

	@Override
	public void addDefinition(PDefinition defNode) {
		if (defNode instanceof APredicateDefinitionDefinition) {
			addDefinition((APredicateDefinitionDefinition) defNode, Type.Predicate);
		} else if (defNode instanceof AExpressionDefinitionDefinition) {
			addDefinition((AExpressionDefinitionDefinition) defNode, Type.Expression);
		} else if (defNode instanceof ASubstitutionDefinitionDefinition) {
			addDefinition((ASubstitutionDefinitionDefinition) defNode, Type.Substitution);
		}
	}

	@Override
	public void addDefinitions(IDefinitions defs) throws PreParseException {
		for (String def: defs.getDefinitionNames()) {
			if (containsDefinition(def)) {
				if(getFile(def)!=defs.getFile(def)) {
					SourcePosition posfile1= getDefinition(def).getStartPos();
					SourcePosition posfile2= defs.getDefinition(def).getStartPos();
					throw new PreParseException("Duplicate definition: " + def + ".\n"+
					"(First appearance at " + "Line: " + posfile1.getLine() + ", Column: " + posfile1.getPos() + " in file: " + getFile(def) +
					" And redefinition at " + "Line: " + posfile2.getLine() + ", Column: " + posfile2.getPos() + " in file: " + defs.getFile(def) +")");
				}
			}
		}
		referencedDefinitions.add(defs);
	}

	@Override
	public void addDefinition(final APredicateDefinitionDefinition defNode, final Type type) {
		addDefinition(defNode, type, defNode.getName().getText());
	}

	@Override
	public void addDefinition(final ASubstitutionDefinitionDefinition defNode, final Type type) {
		addDefinition(defNode, type, defNode.getName().getText());
	}

	@Override
	public void addDefinition(final AExpressionDefinitionDefinition defNode, final Type type) {
		addDefinition(defNode, type, defNode.getName().getText());
	}

	@Override
	public void addDefinition(final PDefinition defNode, final Type type, final String key) {
		if (this.containsDefinition(key)) {
			throw new AssertionError("Duplicate definition \"" + key + "\". This should be handled by the caller.");
		}
		definitionsMap.put(key, defNode);
		types.put(key, type);
	}

	@Override
	public String toString() {
		return definitionsMap.keySet().toString();
	}

	@Override
	public void assignIdsToNodes(INodeIds nodeIdMapping, List<File> machineFilesLoaded) {
		if (file != null) {
			if(!machineFilesLoaded.contains(file)) {
				machineFilesLoaded.add(file);
			}
			final int fileNumber = machineFilesLoaded.indexOf(file) + 1;
			for (PDefinition def : definitionsMap.values()) {
				nodeIdMapping.assignIdentifiers(fileNumber, def);
			}
		}
		for (IDefinitions defintions : super.referencedDefinitions) {
			defintions.assignIdsToNodes(nodeIdMapping, machineFilesLoaded);
		}
	}

}
