package de.be4.classicalb.core.parser.prolog;

import java.io.IOException;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BLexerException;
import de.be4.classicalb.core.parser.exceptions.BParseException;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import util.Helpers;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class StringTest {

	@Test
	public void testFile() throws IOException, BCompoundException {
		String file = "strings/StringIncludingQuotes.mch";
		String result = Helpers.parseFile(file);
		assertTrue(result.contains("'a\"b'"));
	}

	@Test
	public void testString() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES \"a\\\"b\" = \"a\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("'a\"b'"));
	}

	@Test
	public void testNewlineInSingleLineString() {
		final String testMachine = "MACHINE Test PROPERTIES k = \" \n \" END";
		final BCompoundException e = assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		String message = e.getFirstException().getMessage();
		assertTrue(message.contains("Unknown token: \""));
	}

	@Test
	public void testDoubleBackslash() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ''' \\ ''' = ''' \\\\ ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,string(none,' \\\\ '),string(none,' \\\\ ')))]"));
	}

	@Test
	public void testDoubleBackslashTemplate() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ``` \\ ``` = ``` \\\\ ``` END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,multiline_template(none,[literal(none,' \\\\ ')]),multiline_template(none,[literal(none,' \\\\ ')])))]"));
	}

	@Test
	public void testNewline() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ''' \\n ''' = ''' \n ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,string(none,' \\n '),string(none,' \\n ')))]"));
	}

	@Test
	public void testNewlineTemplate() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ``` \\n ``` = ``` \n ``` END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,multiline_template(none,[literal(none,' \\n ')]),multiline_template(none,[literal(none,' \\n ')])))]"));
	}

	@Test
	public void testTab() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ''' \\t ''' = ''' \t ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("string(none,' \\11\\ '),string(none,' \\11\\ '))"));
	}

	@Test
	public void testTabTemplate() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ``` \\t ``` = ``` \t ``` END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("multiline_template(none,[literal(none,' \\11\\ ')]),multiline_template(none,[literal(none,' \\11\\ ')]))"));
	}

	@Test
	public void testCarriageReturn() throws BCompoundException {
		// \r and \r\n will be transformed into a single \n
		// \n will stay the same
		final String testMachine = "MACHINE Test PROPERTIES '''a\r\nb\rc\nd''' = \"b\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("string(none,'a\\nb\\nc\\nd')"));
	}

	@Test
	public void testCarriageReturnTemplate() throws BCompoundException {
		// \r and \r\n will be transformed into a single \n
		// \n will stay the same
		final String testMachine = "MACHINE Test PROPERTIES ```a\r\nb\rc\nd``` = \"b\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("multiline_template(none,[literal(none,'a\\nb\\nc\\nd')])"));
	}

	@Test
	public void testEscapedCarriageReturn() throws BCompoundException {
		// ...those same characters in their escaped variants will not trigger normalization
		final String testMachine = "MACHINE Test PROPERTIES '''a\\r\\nb\\rc\\nd\r\\ne\\r\nf''' = \"b\" END";
		String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("string(none,'a\\15\\\\nb\\15\\c\\nd\\n\\ne\\15\\\\nf')"));
	}

	@Test
	public void testEscapedCarriageReturnTemplate() throws BCompoundException {
		// ...those same characters in their escaped variants will not trigger normalization
		final String testMachine = "MACHINE Test PROPERTIES ```a\\r\\nb\\rc\\nd\r\\ne\\r\nf``` = \"b\" END";
		String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("multiline_template(none,[literal(none,'a\\15\\\\nb\\15\\c\\nd\\n\\ne\\15\\\\nf')])"));
	}

	@Test
	public void testSingleQuote() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ''' \\' ''' = ''' ' ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(none,string(none,' \\' '),string(none,' \\' '))"));
	}

	@Test
	public void testSingleQuoteTemplate() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ``` \\' ``` = ``` ' ``` END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(none,multiline_template(none,[literal(none,' \\' ')]),multiline_template(none,[literal(none,' \\' ')]))"));
	}

	@Test
	public void testDoubleQuote() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ''' \\\" ''' = ''' \" ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(none,string(none,' \" '),string(none,' \" '))"));
	}

	@Test
	public void testDoubleQuoteTemplate() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ``` \\\" ``` = ``` \" ``` END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(none,multiline_template(none,[literal(none,' \" ')]),multiline_template(none,[literal(none,' \" ')]))"));
	}

	@Test
	public void testBackQuote() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ''' \\` ''' = ''' ` ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(none,string(none,' \\` '),string(none,' \\` '))"));
	}

	@Test
	public void testBackQuoteTemplate() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ``` \\` ``` = ``` ` ``` END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(none,multiline_template(none,[literal(none,' \\` ')]),multiline_template(none,[literal(none,' \\` ')]))"));
	}

	@Test
	public void testBackslashAtEnd1() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES '''\\'''' = \"'\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,string(none,'\\''),string(none,'\\'')))]"));
	}

	@Test
	public void testBackslashAtEnd1Template() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ```\\```` = \"`\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,multiline_template(none,[literal(none,'\\`')]),string(none,'\\`')))]"));
	}

	@Test
	public void testBackslashAtEnd2() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES '''\\\\''' = \"\\\\\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(none,string(none,'\\\\'),string(none,'\\\\'))"));
	}

	@Test
	public void testBackslashAtEnd2Template() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ```\\\\``` = \"\\\\\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(none,multiline_template(none,[literal(none,'\\\\')]),string(none,'\\\\'))"));
	}

	@Test
	public void testBackslashAtEnd3() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES '''\\\\\\'''' = \"\\\\'\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,string(none,'\\\\\\''),string(none,'\\\\\\'')))]"));
	}

	@Test
	public void testBackslashAtEnd3Template() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ```\\\\\\```` = \"\\\\`\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,multiline_template(none,[literal(none,'\\\\\\`')]),string(none,'\\\\\\`')))]"));
	}

	@Test
	public void testMultiLineString() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES k = ''' adfa \"a\" ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("' adfa \"a\" '"));
	}

	@Test
	public void testMultiLineTemplate() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES k = ``` adfa \"a\" ``` END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("' adfa \"a\" '"));
	}

	@Test
	public void testMultiLineString2() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES k = ''' adfa \"a ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("' adfa \"a '"));
	}

	@Test
	public void testMultiLineTemplate2() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES k = ``` adfa \"a ``` END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("' adfa \"a '"));
	}

	@Test
	public void testTemplateParameterSimple() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ```${1}``` = \"1\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,multiline_template(none,[parameter(none,'',integer(none,1))]),string(none,'1')))]"));
	}

	@Test
	public void testTemplateParameterExpr() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ```${1 + 2*3}``` = \"6\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,multiline_template(none,[parameter(none,'',add(none,integer(none,1),mult_or_cart(none,integer(none,2),integer(none,3))))]),string(none,'6')))]"));
	}

	@Test
	public void testTemplateBracesInParameter() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ```${{{1},{2}}-{{1}}}``` = \"{{2}}\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,multiline_template(none,[parameter(none,'',minus_or_set_subtract(none,set_extension(none,[set_extension(none,[integer(none,1)]),set_extension(none,[integer(none,2)])]),set_extension(none,[set_extension(none,[integer(none,1)])])))]),string(none,'{{2}}')))]"));
	}

	@Test
	public void testTemplateBackslashInParameter() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ```${{1}\\{1}}``` = \"{}\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,multiline_template(none,[parameter(none,'',set_subtraction(none,set_extension(none,[integer(none,1)]),set_extension(none,[integer(none,1)])))]),string(none,'{}')))]"));
	}

	@Test
	public void testTemplateParameterOptionsEmpty() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ```$[]{{}}``` = \"{}\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,multiline_template(none,[parameter(none,'',empty_set(none))]),string(none,'{}')))]"));
	}

	@Test
	public void testTemplateParameterOptionsNonEmpty() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ```$[u]{{}}``` = \"∅\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,multiline_template(none,[parameter(none,u,empty_set(none))]),string(none,'\\21005\\')))]"));
	}

	@Test
	public void testTemplateComplex() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ```abc ${1} def $[u]{{}} ghi``` = \"abc 1 def ∅ ghi\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,multiline_template(none,[literal(none,'abc '),parameter(none,'',integer(none,1)),literal(none,' def '),parameter(none,u,empty_set(none)),literal(none,' ghi')]),string(none,'abc 1 def \\21005\\ ghi')))]"));
	}

	@Test
	public void testTemplateParameterEscapedDollar() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ```\\${1}\\\\${2}\\\\\\${3}``` = \"${1}\\\\2\\\\${3}\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,multiline_template(none,[literal(none,'${1}\\\\'),parameter(none,'',integer(none,2)),literal(none,'\\\\${3}')]),string(none,'${1}\\\\2\\\\${3}')))]"));
	}

	@Test
	public void testTemplateParseErrorInParameter() {
		final String testMachine = "MACHINE Test PROPERTIES ```${1++2}``` = \"\" END";
		BLexerException exc = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(exc.getMessage().contains("Invalid combination of symbols"));
	}

	@Test
	public void testTemplateUnclosed() {
		final String testMachine = "MACHINE Test PROPERTIES ```a";
		BParseException exc = Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(exc.getMessage().contains("expecting: '```'"));
	}

	@Test
	public void testTemplateUnclosedParameter() {
		final String testMachine = "MACHINE Test PROPERTIES ```${1";
		BLexerException exc = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(exc.getMessage().contains("missing 1 closing brace"));
	}

	@Test
	public void testTemplateUnclosedParameterOptions() {
		final String testMachine = "MACHINE Test PROPERTIES ```$[u";
		Assertions.assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test
	public void testTemplateParameterOptionsWithoutExpression() {
		final String testMachine = "MACHINE Test PROPERTIES ```$[u]";
		Assertions.assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test
	public void testTemplateParameterMismatchedBraces() {
		final String testMachine = "MACHINE Test PROPERTIES ```${{{1}``` = \"\" END";
		BLexerException exc = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(exc.getMessage().contains("missing 2 closing braces"));
	}

	@Test
	public void testTemplateParameterCannotContainTemplate() {
		final String testMachine = "MACHINE Test PROPERTIES ```${```a```}``` = \"a\" END";
		BLexerException exc = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(exc.getMessage().contains("missing 1 closing brace"));
	}

	@Test
	public void testTemplateParameterCannotContainString() {
		final String testMachine = "MACHINE Test PROPERTIES ```${\"a\"}``` = \"a\" END";
		BLexerException exc = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(exc.getMessage().contains("Strings are not allowed inside template parameters"));
	}

	@Test
	public void testTemplateParameterCannotContainMultiLineString() {
		final String testMachine = "MACHINE Test PROPERTIES ```${'''a'''}``` = \"a\" END";
		BLexerException exc = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(exc.getMessage().contains("Strings are not allowed inside template parameters"));
	}

	@Test
	public void testTemplateParameterCannotContainQuotedIdentifier() {
		final String testMachine = "MACHINE Test PROPERTIES ```${`a`}``` = \"\" END";
		BLexerException exc = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(exc.getMessage().contains("Quoted identifiers are not allowed inside template parameters"));
	}

	@Test
	public void testTemplateParameterCannotContainSingleLineComment() {
		final String testMachine = "MACHINE Test PROPERTIES ```${1// comment\n}``` = \"1\" END";
		BLexerException exc = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(exc.getMessage().contains("Comments are not allowed inside template parameters"));
	}

	@Test
	public void testTemplateParameterCannotContainMultiLineComment() {
		final String testMachine = "MACHINE Test PROPERTIES ```${1/* comment */}``` = \"1\" END";
		BLexerException exc = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(exc.getMessage().contains("Comments are not allowed inside template parameters"));
	}

	@Test
	public void testTemplateParameterCannotContainPragma() {
		final String testMachine = "MACHINE Test PROPERTIES ```${/*@symbolic*/ {}}``` = \"{}\" END";
		BLexerException exc = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(exc.getMessage().contains("Comments are not allowed inside template parameters"));
	}

	@Test
	public void testTemplateParameterOptionsCannotContainSingleQuote() {
		final String testMachine = "MACHINE Test PROPERTIES ```$['a']{1}``` = \"1\" END";
		Assertions.assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test
	public void testTemplateParameterOptionsCannotContainDoubleQuote() {
		final String testMachine = "MACHINE Test PROPERTIES ```$[\"a\"]{1}``` = \"1\" END";
		Assertions.assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test
	public void testTemplateParameterOptionsCannotContainBackQuote() {
		final String testMachine = "MACHINE Test PROPERTIES ```$[`a`]{1}``` = \"1\" END";
		Assertions.assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test
	public void testTemplateParameterOptionsCannotContainParameter() {
		final String testMachine = "MACHINE Test PROPERTIES ```$[${0}]{1}``` = \"1\" END";
		Assertions.assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test
	public void testTemplateParameterOptionsCannotContainDollar() {
		final String testMachine = "MACHINE Test PROPERTIES ```$[$]{1}``` = \"1\" END";
		Assertions.assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test
	public void testTemplateParameterOptionsCannotContainParentheses() {
		final String testMachine = "MACHINE Test PROPERTIES ```$[()]{1}``` = \"1\" END";
		Assertions.assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test
	public void testTemplateParameterOptionsCannotContainBrackets() {
		final String testMachine = "MACHINE Test PROPERTIES ```$[[]{1}``` = \"1\" END";
		Assertions.assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test
	public void testTemplateParameterOptionsCannotContainBraces() {
		final String testMachine = "MACHINE Test PROPERTIES ```$[{}]{1}``` = \"1\" END";
		Assertions.assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}
}
