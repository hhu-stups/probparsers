package de.be4.classicalb.core.parser;

import java.io.File;
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

	public abstract PDefinition getDefinition(String defName);

	public abstract boolean containsDefinition(String defName);

	public abstract Map<String, Type> getTypes();

	public abstract int getParameterCount(String defName);

	public abstract Type getType(String defName);

	public abstract Set<String> getDefinitionNames();

	public abstract void addDefinition(PDefinition defNode, Type type, String defName);

	public void addDefinition(APredicateDefinitionDefinition defNode, Type type) {
		addDefinition(defNode, type, defNode.getName().getText());
	}

	public void addDefinition(ASubstitutionDefinitionDefinition defNode, Type type) {
		addDefinition(defNode, type, defNode.getName().getText());
	}

	public void addDefinition(AExpressionDefinitionDefinition defNode, Type type) {
		addDefinition(defNode, type, defNode.getName().getText());
	}

	public void addDefinition(PDefinition defNode) {
		if (defNode instanceof APredicateDefinitionDefinition) {
			addDefinition((APredicateDefinitionDefinition) defNode, Type.Predicate);
		} else if (defNode instanceof AExpressionDefinitionDefinition) {
			addDefinition((AExpressionDefinitionDefinition) defNode, Type.Expression);
		} else if (defNode instanceof ASubstitutionDefinitionDefinition) {
			addDefinition((ASubstitutionDefinitionDefinition) defNode, Type.Substitution);
		} else {
			throw new AssertionError("Unhandled definition node type: " + defNode.getClass());
		}
	}

	public abstract void addDefinitions(IDefinitions defs) throws PreParseException;

	public abstract void replaceDefinition(String defName, Type type, PDefinition node);

	public abstract void assignIdsToNodes(INodeIds nodeIdMapping, List<File> machineFilesLoaded);

	public void setDefinitionType(String defName, Type type) {
		replaceDefinition(defName, type, getDefinition(defName));
	}

	public abstract File getFile(String defName);

}
