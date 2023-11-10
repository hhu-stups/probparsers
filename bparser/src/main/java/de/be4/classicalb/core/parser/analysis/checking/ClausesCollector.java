package de.be4.classicalb.core.parser.analysis.checking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.AAbstractMachineParseUnit;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.AImplementationMachineParseUnit;
import de.be4.classicalb.core.parser.node.ARefinementMachineParseUnit;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.PMachineClause;
import de.be4.classicalb.core.parser.node.PMachineHeader;

public class ClausesCollector extends DepthFirstAdapter {

	private final Map<Class<? extends Node>, Set<Node>> availableClauses = new HashMap<>();
	private boolean scalarParameter = false;
	boolean refinement = false;

	public boolean hasScalarParameter() {
		return scalarParameter;
	}

	public boolean isRefinement() {
		return refinement;
	}
	
	private void collectParams(final PMachineHeader machineHeader) {
		machineHeader.apply(new DepthFirstAdapter() {
			@Override
			public void caseAIdentifierExpression(final AIdentifierExpression node) {
				scalarParameter = scalarParameter || allLowerCase(node.getIdentifier().getLast().getText());
			}
		});
	}
	
	private void addMachineClauses(final LinkedList<PMachineClause> machineClauses) {
		for (final PMachineClause clause : machineClauses) {
			Set<Node> nodesForclause = availableClauses.get(clause.getClass());

			if (nodesForclause == null) {
				nodesForclause = new HashSet<>();
			}

			nodesForclause.add(clause);
			availableClauses.put(clause.getClass(), nodesForclause);
		}
	}
	
	@Override
	public void caseAAbstractMachineParseUnit(final AAbstractMachineParseUnit node) {
		collectParams(node.getHeader());
		addMachineClauses(node.getMachineClauses());
	}
	
	@Override
	public void caseARefinementMachineParseUnit(final ARefinementMachineParseUnit node) {
		collectParams(node.getHeader());
		addMachineClauses(node.getMachineClauses());
		refinement = true;
	}
	
	@Override
	public void caseAImplementationMachineParseUnit(final AImplementationMachineParseUnit node) {
		collectParams(node.getHeader());
		addMachineClauses(node.getMachineClauses());
		refinement = true;
	}

	private boolean allLowerCase(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (Character.isUpperCase(s.charAt(i)))
				return false;
		}
		return true;
	}

	public Map<Class<? extends Node>, Set<Node>> getAvailableClauses() {
		return availableClauses;
	}

}
