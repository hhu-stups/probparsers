package de.be4.classicalb.core.parser;

import static org.junit.Assert.assertEquals;
import static util.Helpers.getTreeAsString;

import org.junit.Test;

public class CommentsTest {


	@Test
	public void testCommentInsideEmptySet() throws Exception {
		final String testMachine = "MACHINE Comments CONSTANTS k PROPERTIES k : POW(INTEGER) & k = { /* comment */  } END";
		String result=getTreeAsString(testMachine);
		assertEquals(result,"Start(AAbstractMachineParseUnit(AMachineHeader([Comments],[]),[AConstantsMachineClause([AIdentifierExpression([k])]),APropertiesMachineClause(AConjdisjunctPredicate(AElementconjdisjunctPredicate(AMemberPredicate(AIdentifierExpression([k]),APowSubsetExpression(AIntegerSetExpression())),&),AEqualPredicate(AIdentifierExpression([k]),AEmptySetExpression())))]))");

	}

	@Test
	public void testCommentInsideEmptySequence() throws Exception {
		final String testMachine = "MACHINE Comments CONSTANTS k PROPERTIES k : seq(INTEGER) & k = [ /* comment */  ] END";
		String result=getTreeAsString(testMachine);
		assertEquals(result,"Start(AAbstractMachineParseUnit(AMachineHeader([Comments],[]),[AConstantsMachineClause([AIdentifierExpression([k])]),APropertiesMachineClause(AConjdisjunctPredicate(AElementconjdisjunctPredicate(AMemberPredicate(AIdentifierExpression([k]),ASeqExpression(AIntegerSetExpression())),&),AEqualPredicate(AIdentifierExpression([k]),AEmptySequenceExpression())))]))");

	}

	@Test
	public void testCommentInsideEmptySequence2() throws Exception {
	// we no longer accept comments inside this empty sequence; <> is not supported by Atelier-B anyway
	// this allows us to treat <> as a single token and improve error messages in other parts of the parser
	//		final String testMachine = "MACHINE Comments CONSTANTS k PROPERTIES k : seq(INTEGER) & k = < /* comment */  > END";
		final String testMachine = "MACHINE Comments CONSTANTS k PROPERTIES k : seq(INTEGER) & k = <  > END";
		getTreeAsString(testMachine);
	}

}
