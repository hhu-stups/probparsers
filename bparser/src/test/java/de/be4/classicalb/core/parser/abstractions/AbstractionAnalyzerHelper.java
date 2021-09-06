package de.be4.classicalb.core.parser.abstractions;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.*;

import java.util.ArrayList;
import java.util.List;

public class AbstractionAnalyzerHelper extends DepthFirstAdapter {


	int variables = 0;
	int constants = 0;
	boolean abstraction;

	@Override
	public void caseAAbstractedMachineParseUnit(AAbstractedMachineParseUnit node)
	{
		abstraction = true;
		List<PMachineClause> copy = new ArrayList<>(node.getMachineClauses());
		for(PMachineClause e : copy)
		{
			e.apply(this);
		}
	}

	@Override
	public void caseAAbstractedVariablesMachineClause(AAbstractedVariablesMachineClause node)
	{

		List<PExpression> copy = new ArrayList<>(node.getIdentifiers());
		for(PExpression e : copy)
		{
			variables++;
			e.apply(this);
		}

	}

	@Override
	public void caseAAbstractedConstantsMachineClause(AAbstractedConstantsMachineClause node) {
		List<PExpression> copy = new ArrayList<>(node.getIdentifiers());
		for (PExpression e : copy) {
			constants++;
			e.apply(this);
		}
	}
}
