package de.be4.classicalb.core.parser.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.be4.classicalb.core.parser.IDefinitions;
import de.be4.classicalb.core.parser.analysis.prolog.MachineReference;
import de.be4.classicalb.core.parser.analysis.transforming.DefinitionInjector;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.AAbstractMachineParseUnit;
import de.be4.classicalb.core.parser.node.AEqualPredicate;
import de.be4.classicalb.core.parser.node.AIncludesMachineClause;
import de.be4.classicalb.core.parser.node.AMachineHeader;
import de.be4.classicalb.core.parser.node.AMachineMachineVariant;
import de.be4.classicalb.core.parser.node.AMachineReference;
import de.be4.classicalb.core.parser.node.AOperationReference;
import de.be4.classicalb.core.parser.node.APromotesMachineClause;
import de.be4.classicalb.core.parser.node.APropertiesMachineClause;
import de.be4.classicalb.core.parser.node.EOF;
import de.be4.classicalb.core.parser.node.PMachineReference;
import de.be4.classicalb.core.parser.node.POperationReference;
import de.be4.classicalb.core.parser.node.PPredicate;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.ASTBuilder;

import static de.be4.classicalb.core.parser.util.ASTBuilder.createConjunction;
import static de.be4.classicalb.core.parser.util.ASTBuilder.createIdentifier;
import static de.be4.classicalb.core.parser.util.ASTBuilder.createStringExpression;

public class BMachine extends IModel {
	private final AAbstractMachineParseUnit parseUnit;

	public BMachine(String name) {
		super();
		AMachineHeader header = new AMachineHeader(ASTBuilder.createTIdentifierList(name), new ArrayList<>());
		this.parseUnit = new AAbstractMachineParseUnit(new AMachineMachineVariant(), header, new ArrayList<>());
		this.setStart(new Start(parseUnit, new EOF()));
		this.setMachineName(name);
	}

	public void addIncludesClause(String machineName) {
		List<PMachineReference> referencesList = new ArrayList<>();
		referencesList.add(new AMachineReference(ASTBuilder.createTIdentifierList(machineName), new ArrayList<>()));
		this.parseUnit.getMachineClauses().add(new AIncludesMachineClause(referencesList));
	}

	public void addPromotesClause(List<String> operationList) {
		List<POperationReference> opList = new ArrayList<>();
		for (String name : operationList) {
			opList.add(new AOperationReference(ASTBuilder.createTIdentifierList(name)));
		}
		this.parseUnit.getMachineClauses().add(new APromotesMachineClause(opList));
	}

	@Override
	public List<MachineReference> getMachineReferences() {
		return new ArrayList<>();
	}

	@Override
	public boolean hasError() {
		return false;
	}

	@Override
	public BCompoundException getCompoundException() {
		throw new AssertionError();
	}

	@Override
	public String getPath() {
		return this.getMachineName();
	}

	public void replaceDefinition(IDefinitions definitions) {
		// Note, this replaces all existing definitions
		DefinitionInjector.injectDefinitions(this.getStart(), definitions);
	}

	public void addPropertiesPredicates(Map<String, String> constantStringValues) {
		if (constantStringValues.isEmpty()) {
			return;
		}
		List<PPredicate> predList = new ArrayList<>();
		constantStringValues.forEach((key, value) ->
				predList.add(new AEqualPredicate(createIdentifier(key), createStringExpression(value))));
		this.parseUnit.getMachineClauses().add(new APropertiesMachineClause(createConjunction(predList)));
	}

}
