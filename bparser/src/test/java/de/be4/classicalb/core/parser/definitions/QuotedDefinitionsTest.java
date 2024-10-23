package de.be4.classicalb.core.parser.definitions;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import org.junit.Ignore;
import org.junit.Test;
import util.Helpers;

import static org.junit.Assert.*;

public class QuotedDefinitionsTest {

	@Test
	public void testQuotedExpressionDefinition() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS `def!expr` == TRUE CONSTANTS x PROPERTIES x = `def!expr` END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[expression_definition(none,'def!expr',[],boolean_true(none))]),constants(none,[identifier(none,x)]),properties(none,equal(none,identifier(none,x),definition(none,'def!expr',[])))])).",
				result);
	}

	@Test
	public void testQuotedExpressionDefinitionWithDot() {
		final String testMachine = "MACHINE Test DEFINITIONS `def.expr`(a) == a END\n";
		BCompoundException e = assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(e.getLocalizedMessage().startsWith("A quoted identifier cannot contain a dot"));
	}

	@Test
	public void testQuotedExpressionDefinitionMixed1() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS `def_expr` == TRUE CONSTANTS x PROPERTIES x = def_expr END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[expression_definition(none,def_expr,[],boolean_true(none))]),constants(none,[identifier(none,x)]),properties(none,equal(none,identifier(none,x),definition(none,def_expr,[])))])).",
				result);
	}

	@Test
	public void testQuotedExpressionDefinitionMixed2() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS def_expr == TRUE CONSTANTS x PROPERTIES x = `def_expr` END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[expression_definition(none,def_expr,[],boolean_true(none))]),constants(none,[identifier(none,x)]),properties(none,equal(none,identifier(none,x),definition(none,def_expr,[])))])).",
				result);
	}

	@Test
	public void testQuotedExpressionDefinitionWithArgs1() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS `def!expr`(a) == a CONSTANTS x PROPERTIES x = `def!expr`(TRUE) END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[expression_definition(none,'def!expr',[identifier(none,a)],identifier(none,a))]),constants(none,[identifier(none,x)]),properties(none,equal(none,identifier(none,x),definition(none,'def!expr',[boolean_true(none)])))])).",
				result);
	}

	@Test
	public void testQuotedExpressionDefinitionWithArgs2() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS `def!expr`(a,`b!`) == a + `b!` CONSTANTS x PROPERTIES x = `def!expr`(1,1) END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[expression_definition(none,'def!expr',[identifier(none,a),identifier(none,'b!')],add(none,identifier(none,a),identifier(none,'b!')))]),constants(none,[identifier(none,x)]),properties(none,equal(none,identifier(none,x),definition(none,'def!expr',[integer(none,1),integer(none,1)])))])).",
				result);
	}

	@Test
	public void testQuotedExpressionDefinitionWithWrongNrOfArgs() {
		final String testMachine = "MACHINE Test DEFINITIONS `def!expr`(a,b) == a + b CONSTANTS x PROPERTIES x = `def!expr`(1) END\n";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(e.getLocalizedMessage().startsWith("Number of parameters"));
	}

	@Test
	public void testQuotedExpressionDefinitionWithArgsMixed1() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS def_expr(a) == a CONSTANTS x PROPERTIES x = `def_expr`(TRUE) END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[expression_definition(none,def_expr,[identifier(none,a)],identifier(none,a))]),constants(none,[identifier(none,x)]),properties(none,equal(none,identifier(none,x),definition(none,def_expr,[boolean_true(none)])))])).",
				result);
	}

	@Test
	public void testQuotedExpressionDefinitionWithArgsMixed2() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS `def_expr`(a) == a CONSTANTS x PROPERTIES x = def_expr(TRUE) END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[expression_definition(none,def_expr,[identifier(none,a)],identifier(none,a))]),constants(none,[identifier(none,x)]),properties(none,equal(none,identifier(none,x),definition(none,def_expr,[boolean_true(none)])))])).",
				result);
	}

	@Test
	public void testQuotedExpressionDefinitionDuplicate() {
		final String testMachine = "MACHINE Test DEFINITIONS `def_expr`(a,b) == a+b; `def_expr` == TRUE END\n";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(e.getLocalizedMessage().startsWith("Duplicate definition: def_expr"));
	}

	@Test
	public void testQuotedExpressionDefinitionDuplicateMixed1() {
		final String testMachine = "MACHINE Test DEFINITIONS `def_expr`(a,b) == a+b; def_expr == TRUE END\n";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(e.getLocalizedMessage().startsWith("Duplicate definition: def_expr"));
	}

	@Test
	public void testQuotedExpressionDefinitionDuplicateMixed2() {
		final String testMachine = "MACHINE Test DEFINITIONS def_expr(a,b) == a+b; `def_expr` == TRUE END\n";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(e.getLocalizedMessage().startsWith("Duplicate definition: def_expr"));
	}

	@Test
	public void testQuotedPredicateDefinition() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS `def!pred` == btrue PROPERTIES `def!pred` OPERATIONS op = PRE `def!pred` THEN skip END END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[predicate_definition(none,'def!pred',[],truth(none))]),properties(none,definition(none,'def!pred',[])),operations(none,[operation(none,identifier(none,op),[],[],precondition(none,definition(none,'def!pred',[]),skip(none)))])])).",
				result);
	}

	@Test
	public void testQuotedPredicateDefinitionWithDot() {
		final String testMachine = "MACHINE Test DEFINITIONS `def.pred`(a,b) == a=b END\n";
		BCompoundException e = assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(e.getLocalizedMessage().startsWith("A quoted identifier cannot contain a dot"));
	}

	@Test
	public void testQuotedPredicateDefinitionMixed1() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS `def_pred` == btrue PROPERTIES def_pred END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[predicate_definition(none,def_pred,[],truth(none))]),properties(none,definition(none,def_pred,[]))])).",
				result);
	}

	@Test
	public void testQuotedPredicateDefinitionMixed2() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS def_pred == btrue PROPERTIES `def_pred` END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[predicate_definition(none,def_pred,[],truth(none))]),properties(none,definition(none,def_pred,[]))])).",
				result);
	}

	@Test
	public void testQuotedPredicateDefinitionWithArgs1() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS `def!pred`(a,b,c) == a=b & b=c PROPERTIES `def!pred`(1,2,3) END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[predicate_definition(none,'def!pred',[identifier(none,a),identifier(none,b),identifier(none,c)],conjunct(none,[equal(none,identifier(none,a),identifier(none,b)),equal(none,identifier(none,b),identifier(none,c))]))]),properties(none,definition(none,'def!pred',[integer(none,1),integer(none,2),integer(none,3)]))])).",
				result);
	}

	@Test
	public void testQuotedPredicateDefinitionWithArgs2() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS `def!pred`(a,`b!`) == a=`b!` PROPERTIES `def!pred`(1,1) END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[predicate_definition(none,'def!pred',[identifier(none,a),identifier(none,'b!')],equal(none,identifier(none,a),identifier(none,'b!')))]),properties(none,definition(none,'def!pred',[integer(none,1),integer(none,1)]))])).",
				result);
	}

	@Test
	public void testQuotedPredicateDefinitionWithWrongNrOfArgs() {
		final String testMachine = "MACHINE Test DEFINITIONS `def!pred`(a,b) == a=b CONSTANTS x PROPERTIES `def!pred`(1) END\n";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(e.getLocalizedMessage().startsWith("Number of parameters"));
	}

	@Test
	public void testQuotedPredicateDefinitionWithArgsMixed1() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS def_pred(a,b) == a=b PROPERTIES `def_pred`(1,2) END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[predicate_definition(none,def_pred,[identifier(none,a),identifier(none,b)],equal(none,identifier(none,a),identifier(none,b)))]),properties(none,definition(none,def_pred,[integer(none,1),integer(none,2)]))])).",
				result);
	}

	@Test
	public void testQuotedPredicateDefinitionWithArgsMixed2() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS `def_pred`(a,b) == a=b PROPERTIES def_pred(1,2) END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[predicate_definition(none,def_pred,[identifier(none,a),identifier(none,b)],equal(none,identifier(none,a),identifier(none,b)))]),properties(none,definition(none,def_pred,[integer(none,1),integer(none,2)]))])).",
				result);
	}

	@Test
	public void testQuotedPredicateDefinitionDuplicate() {
		final String testMachine = "MACHINE Test DEFINITIONS `def_pred`(a,b) == a=b; `def_pred` == bfalse END\n";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(e.getLocalizedMessage().startsWith("Duplicate definition: def_pred"));
	}

	@Test
	public void testQuotedPredicateDefinitionDuplicateMixed1() {
		final String testMachine = "MACHINE Test DEFINITIONS def_pred(a,b) == a=b; `def_pred` == bfalse END\n";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(e.getLocalizedMessage().startsWith("Duplicate definition: def_pred"));
	}

	@Test
	public void testQuotedPredicateDefinitionDuplicateMixed2() {
		final String testMachine = "MACHINE Test DEFINITIONS `def_pred`(a,b) == a=b; def_pred == bfalse END\n";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(e.getLocalizedMessage().startsWith("Duplicate definition: def_pred"));
	}

	@Test
	public void testQuotedSubstitutionDefinition() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS `def!sub` == skip OPERATIONS op = `def!sub` END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[substitution_definition(none,'def!sub',[],skip(none))]),operations(none,[operation(none,identifier(none,op),[],[],definition(none,'def!sub',[]))])])).",
				result);
	}

	@Test
	public void testQuotedSubstitutionDefinitionWithDot() {
		final String testMachine = "MACHINE Test DEFINITIONS `def.sub`(a,b) == a:=b END\n";
		BCompoundException e = assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(e.getLocalizedMessage().startsWith("A quoted identifier cannot contain a dot"));
	}

	@Test
	public void testQuotedSubstitutionDefinitionMixed1() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS `def_sub` == skip OPERATIONS op = def_sub END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[substitution_definition(none,def_sub,[],skip(none))]),operations(none,[operation(none,identifier(none,op),[],[],definition(none,def_sub,[]))])])).",
				result);
	}

	@Test
	public void testQuotedSubstitutionDefinitionMixed2() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS def_sub == skip OPERATIONS op = `def_sub` END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[substitution_definition(none,def_sub,[],skip(none))]),operations(none,[operation(none,identifier(none,op),[],[],definition(none,def_sub,[]))])])).",
				result);
	}

	@Test
	public void testQuotedSubstitutionDefinitionWithArgs1() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS `def!sub`(a,b,c) == a:=b+c OPERATIONS op(a) = `def!sub`(a,2,3) END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[substitution_definition(none,'def!sub',[identifier(none,a),identifier(none,b),identifier(none,c)],assign(none,[identifier(none,a)],[add(none,identifier(none,b),identifier(none,c))]))]),operations(none,[operation(none,identifier(none,op),[],[identifier(none,a)],definition(none,'def!sub',[identifier(none,a),integer(none,2),integer(none,3)]))])])).",
				result);
	}

	@Test
	public void testQuotedSubstitutionDefinitionWithArgs2() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS `def!sub`(a,`b!`) == a:=`b!` OPERATIONS op(y) = ANY x WHERE x:INT THEN `def!sub`(y,x) END END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[substitution_definition(none,'def!sub',[identifier(none,a),identifier(none,'b!')],assign(none,[identifier(none,a)],[identifier(none,'b!')]))]),operations(none,[operation(none,identifier(none,op),[],[identifier(none,y)],any(none,[identifier(none,x)],member(none,identifier(none,x),int_set(none)),definition(none,'def!sub',[identifier(none,y),identifier(none,x)])))])])).",
				result);
	}

	@Test
	public void testQuotedSubstitutionDefinitionWithWrongNrOfArgs() {
		final String testMachine = "MACHINE Test DEFINITIONS `def!sub`(a,b) == a:=b OPERATIONS op = `def!sub`(2) END\n";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(e.getLocalizedMessage().startsWith("Number of parameters"));
	}

	@Test
	public void testQuotedSubstitutionDefinitionWithArgsMixed1() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS def_sub(a,b) == a:=b OPERATIONS op = `def_sub`(1,2) END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[substitution_definition(none,def_sub,[identifier(none,a),identifier(none,b)],assign(none,[identifier(none,a)],[identifier(none,b)]))]),operations(none,[operation(none,identifier(none,op),[],[],definition(none,def_sub,[integer(none,1),integer(none,2)]))])])).",
				result);
	}

	@Test
	public void testQuotedSubstitutionDefinitionWithArgsMixed2() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS `def_sub`(a,b) == a:=b OPERATIONS op = def_sub(1,2) END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[substitution_definition(none,def_sub,[identifier(none,a),identifier(none,b)],assign(none,[identifier(none,a)],[identifier(none,b)]))]),operations(none,[operation(none,identifier(none,op),[],[],definition(none,def_sub,[integer(none,1),integer(none,2)]))])])).",
				result);
	}

	@Test
	public void testQuotedSubstitutionDefinitionDuplicate() {
		final String testMachine = "MACHINE Test DEFINITIONS `def_sub`(a,b) == a:=b; `def_sub` == skip END\n";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(e.getLocalizedMessage().startsWith("Duplicate definition: def_sub"));
	}

	@Test
	public void testQuotedSubstitutionDefinitionDuplicateMixed1() {
		final String testMachine = "MACHINE Test DEFINITIONS `def_sub`(a,b) == a:=b; def_sub == skip END\n";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(e.getLocalizedMessage().startsWith("Duplicate definition: def_sub"));
	}

	@Test
	public void testQuotedSubstitutionDefinitionDuplicateMixed2() {
		final String testMachine = "MACHINE Test DEFINITIONS def_sub(a,b) == a:=b; `def_sub` == skip END\n";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(e.getLocalizedMessage().startsWith("Duplicate definition: def_sub"));
	}

	@Test
	public void testQuotedDefinitionAllMixed() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS `e!add`(a,b)==a+b; `pre,4`(a,b)==a/=b; `op!add`(v,a,b) == PRE `pre,4`(a,b) THEN v:=`e!add`(a,b) END OPERATIONS add(v) = `op!add`(v,4,5) END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[expression_definition(none,'e!add',[identifier(none,a),identifier(none,b)],add(none,identifier(none,a),identifier(none,b))),predicate_definition(none,'pre,4',[identifier(none,a),identifier(none,b)],not_equal(none,identifier(none,a),identifier(none,b))),substitution_definition(none,'op!add',[identifier(none,v),identifier(none,a),identifier(none,b)],precondition(none,definition(none,'pre,4',[identifier(none,a),identifier(none,b)]),assign(none,[identifier(none,v)],[definition(none,'e!add',[identifier(none,a),identifier(none,b)])])))]),operations(none,[operation(none,identifier(none,add),[],[identifier(none,v)],definition(none,'op!add',[identifier(none,v),integer(none,4),integer(none,5)]))])])).",
				result);
	}

	// TODO: from def files

	@Ignore
	@Test
	public void testMisleadingParseError() throws BCompoundException {
		final String testMachine = "MACHINE foo \nDEFINITIONS \nCONSTANTS a \nPROPERTIES a = 1  END";
		String output = Helpers.getMachineAsPrologTerm(testMachine);
		//TODO
	}

}
