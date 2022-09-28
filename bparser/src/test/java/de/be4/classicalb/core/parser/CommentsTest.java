package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import org.junit.Test;

import util.Helpers;

public class CommentsTest {

	
	@Test
	public void testCommentInsideEmptySet() throws BCompoundException {
		final String testMachine = "MACHINE Comments CONSTANTS k PROPERTIES k : POW(INTEGER) & k = { /* comment */  } END";
		Helpers.getMachineAsPrologTerm(testMachine);
	}
	
	@Test
	public void testCommentInsideEmptySequence() throws BCompoundException {
		final String testMachine = "MACHINE Comments CONSTANTS k PROPERTIES k : seq(INTEGER) & k = [ /* comment */  ] END";
		Helpers.getMachineAsPrologTerm(testMachine);
	}
	
	@Test
	public void testCommentInsideEmptySequence2() throws BCompoundException {
	// we no longer accept comments inside this empty sequence; <> is not supported by Atelier-B anyway
	// this allows us to treat <> as a single token and improve error messages in other parts of the parser
	//		final String testMachine = "MACHINE Comments CONSTANTS k PROPERTIES k : seq(INTEGER) & k = < /* comment */  > END";
		final String testMachine = "MACHINE Comments CONSTANTS k PROPERTIES k : seq(INTEGER) & k = <  > END";
		Helpers.getMachineAsPrologTerm(testMachine);
	}
}
