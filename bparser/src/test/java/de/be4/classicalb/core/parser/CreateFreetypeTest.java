package de.be4.classicalb.core.parser;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.AAbstractMachineParseUnit;
import de.be4.classicalb.core.parser.node.AAssignSubstitution;
import de.be4.classicalb.core.parser.node.ABoolSetExpression;
import de.be4.classicalb.core.parser.node.AConstantsMachineClause;
import de.be4.classicalb.core.parser.node.AConstructorFreetypeConstructor;
import de.be4.classicalb.core.parser.node.AElementFreetypeConstructor;
import de.be4.classicalb.core.parser.node.AEmptySetExpression;
import de.be4.classicalb.core.parser.node.AFreetype;
import de.be4.classicalb.core.parser.node.AFreetypesMachineClause;
import de.be4.classicalb.core.parser.node.AFunctionExpression;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.AInitialisationMachineClause;
import de.be4.classicalb.core.parser.node.AIntSetExpression;
import de.be4.classicalb.core.parser.node.AInvariantMachineClause;
import de.be4.classicalb.core.parser.node.AMachineHeader;
import de.be4.classicalb.core.parser.node.AMachineMachineVariant;
import de.be4.classicalb.core.parser.node.AMemberPredicate;
import de.be4.classicalb.core.parser.node.AOperation;
import de.be4.classicalb.core.parser.node.AOperationsMachineClause;
import de.be4.classicalb.core.parser.node.APowSubsetExpression;
import de.be4.classicalb.core.parser.node.APreconditionSubstitution;
import de.be4.classicalb.core.parser.node.ASetExtensionExpression;
import de.be4.classicalb.core.parser.node.AUnionExpression;
import de.be4.classicalb.core.parser.node.EOF;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.PSubstitution;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermOutput;
import de.prob.prolog.output.PrologTermStringOutput;

public class CreateFreetypeTest {

	private static final String MACHINE_NAME = "TT";
	private static final String VAR_NAME = "x";
	private static final String FREETYPE_NAME = "ft";
	private static final String CONS_EMPTY = "nothing";
	private static final String CONS_INT = "myInt";
	private static final String CONS_BOOL = "myBool";

	@Test
	public void testManualFreetypeCreation() {
		final PrologTermStringOutput pto = new PrologTermStringOutput();
		printProlog(pto);
		final String result = pto.toString();

		final String ftc1 = "constructor(none,myBool,bool_set(none))";
		final String ftc2 = "constructor(none,myInt,int_set(none))";
		final String ftc3 = "element(none,nothing)";
		final String freetypeStr = "freetype(none,ft,[],[" + ftc1 + "," + ftc2
				+ "," + ftc3 + "])";
		final String expectedPart = "freetypes(none,[" + freetypeStr + "])";
		Assert.assertTrue("Freetype contained", result.contains(expectedPart));
	}

	public static void main(String[] args) throws IOException {
		final Path outFile = Paths.get("freetypetest.prob");
		CreateFreetypeTest test = new CreateFreetypeTest();
		try (final OutputStream file = Files.newOutputStream(outFile)) {
			final PrologTermOutput pto = new PrologTermOutput(file);

			// parser_version(none).
			pto.openTerm("parser_version");
			pto.printAtom("none");
			pto.closeTerm();
			pto.fullstop();

			// classical_b(machine_name, [file_name])
			pto.openTerm("classical_b");
			pto.printAtom(MACHINE_NAME);
			pto.openList();
			pto.printAtom(outFile.toString());
			pto.closeList();
			pto.closeTerm();
			pto.fullstop();

			// machine(...)
			pto.openTerm("machine");
			test.printProlog(pto);
			pto.closeTerm();
			pto.fullstop();
		}
	}

	private void printProlog(final IPrologTermOutput pto) {
		final Start machine = createMachine(MACHINE_NAME);
		final ASTProlog printer = new ASTProlog(pto, null);
		machine.apply(printer);
	}

	private Start createMachine(String name) {
		final AFreetypesMachineClause freetypes = createFreetype();

		final AConstantsMachineClause variables = new AConstantsMachineClause(
				createIdentifiers(VAR_NAME));

		final AMemberPredicate member = new AMemberPredicate(
				createIdentifier(VAR_NAME), new APowSubsetExpression(
						createIdentifier(FREETYPE_NAME)));
		final AInvariantMachineClause inv = new AInvariantMachineClause(member);

		final AInitialisationMachineClause init = new AInitialisationMachineClause(
				createAssignment(VAR_NAME, new AEmptySetExpression()));
		final AOperationsMachineClause operations = createOperations();

		final AMachineHeader header = new AMachineHeader(createIdLits(name), Collections.emptyList());
		final AAbstractMachineParseUnit machine = new AAbstractMachineParseUnit(
				new AMachineMachineVariant(), header, Arrays.asList(freetypes,
						variables, inv, init, operations));
		return new Start(machine, new EOF());
	}

	private AOperationsMachineClause createOperations() {
		final AOperation op1 = createAdd("addBool", "b",
				new ABoolSetExpression(), CONS_BOOL);
		final AOperation op2 = createAdd("addInt", "i",
				new AIntSetExpression(), CONS_INT);
		final AOperation op3 = createSimpleAdd("addEmpty");
		return new AOperationsMachineClause(
				Arrays.asList(op1, op2, op3));
	}

	private AOperation createAdd(String name, String param, PExpression type,
			String cons) {
		final AMemberPredicate pre = new AMemberPredicate(
				createIdentifier(param), type);
		final ASetExtensionExpression newVal = new ASetExtensionExpression(
				Collections.singletonList(new AFunctionExpression(
						createIdentifier(cons), createIdentifiers(param))));
		final PSubstitution subst = new APreconditionSubstitution(pre,
				createAssignment(VAR_NAME, new AUnionExpression(
						createIdentifier(VAR_NAME), newVal)));
		return new AOperation(Collections.emptyList(), createIdLits(name),
				createIdentifiers(param), subst);
	}

	private AOperation createSimpleAdd(String name) {
		final ASetExtensionExpression newVal = new ASetExtensionExpression(
				createIdentifiers(CONS_EMPTY));
		final PSubstitution subst = createAssignment(VAR_NAME,
				new AUnionExpression(createIdentifier(VAR_NAME), newVal));
		return new AOperation(Collections.emptyList(), createIdLits(name), Collections.emptyList(),
				subst);

	}

	private PSubstitution createAssignment(String var, PExpression expr) {
		return new AAssignSubstitution(createIdentifiers(var), Collections.singletonList(expr));
	}

	private AFreetypesMachineClause createFreetype() {
		final AConstructorFreetypeConstructor cons1 = new AConstructorFreetypeConstructor(
				new TIdentifierLiteral(CONS_BOOL), new ABoolSetExpression());
		final AConstructorFreetypeConstructor cons2 = new AConstructorFreetypeConstructor(
				new TIdentifierLiteral(CONS_INT), new AIntSetExpression());
		final AElementFreetypeConstructor cons3 = new AElementFreetypeConstructor(
				new TIdentifierLiteral(CONS_EMPTY));
		final AFreetype freetype = new AFreetype(
				new TIdentifierLiteral(FREETYPE_NAME),
				Collections.emptyList(),
				Arrays.asList(cons1, cons2, cons3));
		return new AFreetypesMachineClause(Collections.singletonList(freetype));
	}

	private List<PExpression> createIdentifiers(String name) {
		return Collections.singletonList(createIdentifier(name));
	}

	private AIdentifierExpression createIdentifier(String name) {
		return new AIdentifierExpression(createIdLits(name));
	}

	private List<TIdentifierLiteral> createIdLits(String name) {
		return Collections.singletonList(new TIdentifierLiteral(name));
	}

	@Test
	public void testFreetypeSyntax() throws IOException, BCompoundException, URISyntaxException {
		final BParser parser = new BParser("FreetypeIntList");
		final File file = new File(this.getClass().getResource("/FreetypeIntList.mch").toURI());
		parser.parseFile(file);
	}
}
