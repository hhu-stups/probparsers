package de.be4.classicalb.core.parser.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.be4.classicalb.core.parser.IDefinitions;
import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.analysis.prolog.ClassicalPositionPrinter;
import de.be4.classicalb.core.parser.analysis.prolog.INodeIds;
import de.be4.classicalb.core.parser.analysis.transforming.DefinitionInjector;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.AAbstractMachineParseUnit;
import de.be4.classicalb.core.parser.node.AEqualPredicate;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.AIncludesMachineClause;
import de.be4.classicalb.core.parser.node.AMachineHeader;
import de.be4.classicalb.core.parser.node.AMachineMachineVariant;
import de.be4.classicalb.core.parser.node.AMachineReference;
import de.be4.classicalb.core.parser.node.AOperationReference;
import de.be4.classicalb.core.parser.node.APromotesMachineClause;
import de.be4.classicalb.core.parser.node.APropertiesMachineClause;
import de.be4.classicalb.core.parser.node.AStringExpression;
import de.be4.classicalb.core.parser.node.EOF;
import de.be4.classicalb.core.parser.node.PMachineReference;
import de.be4.classicalb.core.parser.node.POperationReference;
import de.be4.classicalb.core.parser.node.PPredicate;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.be4.classicalb.core.parser.node.TStringLiteral;
import de.prob.prolog.output.IPrologTermOutput;

import static de.be4.classicalb.core.parser.rules.ASTBuilder.createConjunction;
import static de.be4.classicalb.core.parser.rules.ASTBuilder.createIdentifier;

public class BMachine implements IModel {
	private final String machineName;
	private ParsingBehaviour parsingBehaviour = new ParsingBehaviour();
	private final Start start;
	private final AAbstractMachineParseUnit parseUnit;

	public BMachine(String name) {
		List<TIdentifierLiteral> nameList = new ArrayList<>();
		nameList.add(new TIdentifierLiteral(name));
		AMachineHeader header = new AMachineHeader(nameList, new ArrayList<>());
		this.parseUnit = new AAbstractMachineParseUnit(new AMachineMachineVariant(), header, new ArrayList<>());
		this.start = new Start(parseUnit, new EOF());
		this.machineName = name;
	}

	@Override
	public Start getStart() {
		return this.start;
	}

	public void setParsingBehaviour(ParsingBehaviour parsingBehaviour) {
		this.parsingBehaviour = parsingBehaviour;
	}

	public void addIncludesClause(String machineName) {
		AIncludesMachineClause includes = new AIncludesMachineClause();
		List<PMachineReference> referencesList = new ArrayList<>();
		List<TIdentifierLiteral> idList = new ArrayList<>();
		idList.add(new TIdentifierLiteral(machineName));
		referencesList.add(new AMachineReference(idList, new ArrayList<>()));
		includes.setMachineReferences(referencesList);
		this.parseUnit.getMachineClauses().add(includes);
	}

	public void addPromotesClause(List<String> operationList) {
		APromotesMachineClause promotes = new APromotesMachineClause();
		List<POperationReference> opList = new ArrayList<>();
		for (String name : operationList) {
			List<TIdentifierLiteral> idList = new ArrayList<>();
			idList.add(new TIdentifierLiteral(name));
			opList.add(new AOperationReference(idList));
		}
		promotes.setOperationNames(opList);
		this.parseUnit.getMachineClauses().add(promotes);

	}

	@Override
	public String getMachineName() {
		return machineName;
	}

	@Override
	public List<RulesMachineReference> getMachineReferences() {
		return new ArrayList<>();
	}

	@Override
	public void printAsProlog(final IPrologTermOutput pout, INodeIds nodeIdMapping) {
		assert start != null;
		final ClassicalPositionPrinter pprinter = new ClassicalPositionPrinter(nodeIdMapping);
		pprinter.setPrintSourcePositions(parsingBehaviour.isAddLineNumbers(), parsingBehaviour.isCompactPrologPositions());
		final ASTProlog prolog = new ASTProlog(pout, pprinter);
		
		pout.openTerm("machine");
		start.apply(prolog);
		pout.closeTerm();
		pout.fullstop();
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
		return this.machineName;
	}

	public void replaceDefinition(IDefinitions definitions) {
		// Note, this replaces all existing definitions
		DefinitionInjector.injectDefinitions(start, definitions);
	}

	public void addPropertiesPredicates(Map<String, String> constantStringValues) {
		if (constantStringValues.isEmpty()) {
			return;
		}
		APropertiesMachineClause clause = new APropertiesMachineClause();
		this.parseUnit.getMachineClauses().add(clause);
		List<PPredicate> predList = new ArrayList<>();
		for (Entry<String, String> entry : constantStringValues.entrySet()) {
			AIdentifierExpression identifier = createIdentifier(entry.getKey());
			AStringExpression value = new AStringExpression(new TStringLiteral(entry.getValue()));
			AEqualPredicate equal = new AEqualPredicate(identifier, value);
			predList.add(equal);
		}
		clause.setPredicates(createConjunction(predList));
	}

}
