package de.be4.classicalb.core.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.be4.classicalb.core.parser.analysis.prolog.INodeIds;
import de.be4.classicalb.core.parser.exceptions.PreParseException;
import de.be4.classicalb.core.parser.node.AExpressionDefinitionDefinition;
import de.be4.classicalb.core.parser.node.APredicateDefinitionDefinition;
import de.be4.classicalb.core.parser.node.ASubstitutionDefinitionDefinition;
import de.be4.classicalb.core.parser.node.PDefinition;

public abstract class IDefinitions {
	public enum Type {
		NoDefinition, Expression, Predicate, Substitution, ExprOrSubst
	}

	protected final List<IDefinitions> referencedDefinitions = new ArrayList<>();

	public abstract PDefinition getDefinition(String defName);

	public abstract boolean containsDefinition(String defName);

	public abstract Map<String, Type> getTypes();

	public abstract int getParameterCount(String defName);

	public abstract Type getType(String defName);

	public abstract Set<String> getDefinitionNames();

	public abstract void addDefinition(APredicateDefinitionDefinition defNode, Type type);

	public abstract void addDefinition(ASubstitutionDefinitionDefinition defNode, Type type);

	public abstract void addDefinition(AExpressionDefinitionDefinition defNode, Type type);

	public abstract void addDefinition(PDefinition defNode, Type type, String defName);

	public abstract void addDefinition(PDefinition defNode);

	public abstract void addDefinitions(IDefinitions defs) throws PreParseException;

	public abstract void replaceDefinition(String defName, Type type, PDefinition node);

	public abstract void assignIdsToNodes(INodeIds nodeIdMapping, List<File> machineFilesLoaded);

	public abstract void setDefinitionType(String defName, Type type);

	public abstract File getFile(String defName);

}
