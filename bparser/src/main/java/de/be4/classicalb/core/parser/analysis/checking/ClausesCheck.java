package de.be4.classicalb.core.parser.analysis.checking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.be4.classicalb.core.parser.ParseOptions;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.node.*;
import de.be4.classicalb.core.parser.util.Utils;

/**
 * This class checks several dependencies between machine clauses. Examples are:
 * Which clauses are valid for an abstract machine? Is a PROPERTIES clause
 * present, if a CONSTANTS clause is used?
 * 
 * All violations of checks are collected in <code>exceptions</code> and can be
 * retrieved by {@link #getCheckExceptions()}.
 * 
 * @author Fabian
 * 
 */
public class ClausesCheck implements SemanticCheck {
	private static final Set<Class<? extends Node>> MACHINE_FORBIDDEN_CLAUSES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		/* ALocalOperationsMachineClause.class, */
		AImportsMachineClause.class,
		AValuesMachineClause.class
	)));
	private static final Set<Class<? extends Node>> REFINEMENT_FORBIDDEN_CLAUSES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		AUsesMachineClause.class,
		AConstraintsMachineClause.class,
		ALocalOperationsMachineClause.class,
		AImportsMachineClause.class,
		AValuesMachineClause.class
	)));
	private static final Set<Class<? extends Node>> IMPLEMENTATION_FORBIDDEN_CLAUSES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		AConstraintsMachineClause.class,
		AIncludesMachineClause.class,
		AUsesMachineClause.class,
		AAbstractConstantsMachineClause.class,
		AVariablesMachineClause.class
	)));

	private Map<Class<? extends Node>, Set<Node>> clauses;

	private final List<CheckException> exceptions = new ArrayList<>();

	/**
	 * The following requirements for the machine clauses are checked:
	 * <ul>
	 * <li>In MACHINEs, REFINEMENTs and IMPLEMENATATIONs different clauses are
	 * not allowed.</li>
	 * <li>If one of the CONCRETE_CONSTANTS, CONSTANTS or ABSTRACT_CONSTANTS
	 * clauses is present, then the PROPERTIES clause must be present.</li>
	 * <li>If one of the CONCRETE_VARIABLES, VARIABLES or ABSTRACT_VARIABLES
	 * clauses is present, then the INVARIANT and INITIALISATION clauses must be
	 * present.</li>
	 * </ul>
	 * 
	 */
	public void runChecks(final Start rootNode) {
		// only need to check complete machines
		if (!Utils.isCompleteMachine(rootNode)) {
			return;
		}

		final ClausesCollector collector = new ClausesCollector();
		rootNode.apply(collector);
		clauses = collector.getAvailableClauses();

		checkDoubleClauses();
		checkMachineClauses(rootNode);
		checkRefinementClauses(rootNode);
		checkImplementationClauses(rootNode);
		if (!collector.isRefinement()) {
			checkConstantsClause();
			checkVariablesClauses();
			if (collector.hasScalarParameter()) {
				checkConstraintExistance(rootNode);
			}
		}
	}

	private void checkConstraintExistance(Start rootNode) {

		if (!clauses.containsKey(AConstraintsMachineClause.class)) {
			exceptions.add(new CheckException("Specification has formal scalar parameter and no CONSTRAINTS clause.",
					rootNode.getPParseUnit()));
		}
	}

	/**
	 * If the machine is a IMPLEMENTATION the following clauses are not allowed:
	 * CONSTRAINTS, INCLUDES, USES, ABSTRACT_CONSTANTS, ABSTRACT_VARIABLES,
	 * VARIABLES
	 */
	private void checkImplementationClauses(final Start rootNode) {
		if (!(rootNode.getPParseUnit() instanceof AImplementationMachineParseUnit)) {
			return;
		}

		findForbidden(IMPLEMENTATION_FORBIDDEN_CLAUSES, "implementation machine");
	}

	/**
	 * If the machine is a REFINEMENT the following clauses are not allowed:
	 * USES, CONSTRAINTS, LOCAL_VARIABLES, VALUES, IMPORTS
	 */
	private void checkRefinementClauses(final Start rootNode) {
		if (!(rootNode.getPParseUnit() instanceof ARefinementMachineParseUnit)) {
			return;
		}

		findForbidden(REFINEMENT_FORBIDDEN_CLAUSES, "refinement machine");
	}

	/**
	 * If the machine is a MACHINE the following clauses are not allowed:
	 * LOCAL_VARIABLES, VALUES, IMPORTS
	 */
	private void checkMachineClauses(final Start rootNode) {
		if (!(rootNode.getPParseUnit() instanceof AAbstractMachineParseUnit)) {
			return;
		}

		findForbidden(MACHINE_FORBIDDEN_CLAUSES, "abstract machine");
	}

	private void checkVariablesClauses() {
		/*
		 * CONCRETE_VARIABLES || VARIABLES || ABSTRACT_VARIABLES => INVARIANT &&
		 * INITIALISATION
		 */
		if ((clauses.containsKey(AVariablesMachineClause.class) || clauses.containsKey(AConcreteVariablesMachineClause.class))
				&& (!clauses.containsKey(AInvariantMachineClause.class) || !clauses.containsKey(AInitialisationMachineClause.class))) {

			final Set<Node> nodes = new HashSet<>();
			if (clauses.containsKey(AVariablesMachineClause.class)) {
				nodes.addAll(clauses.get(AVariablesMachineClause.class));
			}
			if (clauses.containsKey(AConcreteVariablesMachineClause.class)) {
				nodes.addAll(clauses.get(AConcreteVariablesMachineClause.class));
			}

			final StringBuilder message = new StringBuilder("Clause(s) missing: ");
			boolean first = true;
			if (!clauses.containsKey(AInvariantMachineClause.class)) {
				message.append("INVARIANT");
				first = false;
			}
			if (!clauses.containsKey(AInitialisationMachineClause.class)) {
				if (!first) {
					message.append(", ");
				}
				message.append("INITIALISATION");
			}
			exceptions.add(new CheckException(message.toString(), new ArrayList<>(nodes)));
		}
	}

	private void checkConstantsClause() {
		/*
		 * CONCRETE_CONSTANTS || CONSTANTS || ABSTRACT_CONSTANTS => PROPERTIES
		 */
		if ((clauses.containsKey(AConstantsMachineClause.class) || clauses.containsKey(AAbstractConstantsMachineClause.class))
				&& !clauses.containsKey(APropertiesMachineClause.class)) {
			final Set<Node> nodes = new HashSet<>();

			if (clauses.containsKey(AConstantsMachineClause.class)) {
				nodes.addAll(clauses.get(AConstantsMachineClause.class));
			}
			if (clauses.containsKey(AAbstractConstantsMachineClause.class)) {
				nodes.addAll(clauses.get(AAbstractConstantsMachineClause.class));
			}
			exceptions.add(new CheckException("Clause(s) missing: PROPERTIES", new ArrayList<>(nodes)));
		}
	}

	private static String clauseNameFromNodeClass(final Class<? extends Node> nodeClass) {
		final String nodeClassName = nodeClass.getSimpleName();
		final int endIndex = nodeClassName.indexOf("MachineClause");
		return nodeClassName.substring(1, endIndex).toUpperCase();
	}

	private void findForbidden(final Set<Class<? extends Node>> forbiddenClasses, final String machineKindDescription) {
		final Set<Class<? extends Node>> wrongClauseClasses = new HashSet<>(clauses.keySet());
		wrongClauseClasses.retainAll(forbiddenClasses);

		if (!wrongClauseClasses.isEmpty()) {
			final Set<Node> nodes = new HashSet<>();
			final Set<String> wrongClauseNames = new HashSet<>();

			for (final Class<? extends Node> wrongClauseClass : wrongClauseClasses) {
				nodes.addAll(clauses.get(wrongClauseClass));
				wrongClauseNames.add(clauseNameFromNodeClass(wrongClauseClass));
			}
			exceptions.add(new CheckException("Clauses not allowed in " + machineKindDescription + ": " + String.join(", ", wrongClauseNames),
					new ArrayList<>(nodes)));
		}
	}

	/**
	 * Checks if one clause is used more than once in the machine.
	 */
	private void checkDoubleClauses() {
		for (final Iterator<Set<Node>> iterator = clauses.values().iterator(); iterator.hasNext();) {
			final Set<Node> nodesforClause = iterator.next();

			if (nodesforClause.size() > 1) {
				final Node clauseNode = nodesforClause.iterator().next();
				final String clauseName = clauseNameFromNodeClass(clauseNode.getClass());

				exceptions.add(new CheckException("Clause '" + clauseName + "' is used more than once",
						new ArrayList<>(nodesforClause)));
			}
		}
	}

	public void setOptions(ParseOptions options) {
		// ignore options
	}

	@Override
	public List<CheckException> getCheckExceptions() {

		return exceptions;
	}
}
