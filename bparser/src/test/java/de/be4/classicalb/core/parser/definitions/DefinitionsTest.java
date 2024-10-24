package de.be4.classicalb.core.parser.definitions;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.CheckException;

import org.junit.Ignore;
import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class DefinitionsTest {

	@Test
	public void testDefinitions1() throws BCompoundException {
		final String testMachine = "MACHINE Test\nDEFINITIONS def_sub1 == skip; def_pred1 == 2 > x; def_expr1 == 41 + 1\nOPERATIONS op = PRE def_pred1 THEN i := def_expr1 || def_sub1 END END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[substitution_definition(none,def_sub1,[],skip(none)),predicate_definition(none,def_pred1,[],greater(none,integer(none,2),identifier(none,x))),expression_definition(none,def_expr1,[],add(none,integer(none,41),integer(none,1)))]),operations(none,[operation(none,identifier(none,op),[],[],precondition(none,definition(none,def_pred1,[]),parallel(none,[assign(none,[identifier(none,i)],[definition(none,def_expr1,[])]),definition(none,def_sub1,[])])))])])).",
				result);
	}

	@Test
	public void testDefinitions2() throws BCompoundException {
		final String testMachine = "MACHINE Test\nDEFINITIONS def_sub1 == skip; def_pred1 == 2 > x; def_expr1 == 41 + 1; def_expr2(a,b,c) == a+b*c \nOPERATIONS op = PRE def_pred1 THEN i := def_expr1 || def_sub1 || j := def_expr2(1,2,3) END END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[substitution_definition(none,def_sub1,[],skip(none)),predicate_definition(none,def_pred1,[],greater(none,integer(none,2),identifier(none,x))),expression_definition(none,def_expr1,[],add(none,integer(none,41),integer(none,1))),expression_definition(none,def_expr2,[identifier(none,a),identifier(none,b),identifier(none,c)],add(none,identifier(none,a),mult_or_cart(none,identifier(none,b),identifier(none,c))))]),operations(none,[operation(none,identifier(none,op),[],[],precondition(none,definition(none,def_pred1,[]),parallel(none,[assign(none,[identifier(none,i)],[definition(none,def_expr1,[])]),definition(none,def_sub1,[]),assign(none,[identifier(none,j)],[definition(none,def_expr2,[integer(none,1),integer(none,2),integer(none,3)])])])))])])).",
				result);
	}

	@Test
	public void testDefinitions3() throws BCompoundException {
		final String testMachine = "MACHINE Test\nDEFINITIONS def_expr1 == 41 + 1; def_expr2(a,b,c) == a+b*c \nOPERATIONS op = PRE 1 = 1 THEN i := def_expr2(1,def_expr1,3) END END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[expression_definition(none,def_expr1,[],add(none,integer(none,41),integer(none,1))),expression_definition(none,def_expr2,[identifier(none,a),identifier(none,b),identifier(none,c)],add(none,identifier(none,a),mult_or_cart(none,identifier(none,b),identifier(none,c))))]),operations(none,[operation(none,identifier(none,op),[],[],precondition(none,equal(none,integer(none,1),integer(none,1)),assign(none,[identifier(none,i)],[definition(none,def_expr2,[integer(none,1),definition(none,def_expr1,[]),integer(none,3)])])))])])).",
				result);
	}

	@Test
	public void testDefinitions4() throws BCompoundException {
		final String testMachine = "MACHINE Test \n DEFINITIONS def_expr1 == 41 + 1; def_expr2(a,b,c) == a+def_expr1*c \nEND";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[expression_definition(none,def_expr1,[],add(none,integer(none,41),integer(none,1))),expression_definition(none,def_expr2,[identifier(none,a),identifier(none,b),identifier(none,c)],add(none,identifier(none,a),mult_or_cart(none,definition(none,def_expr1,[]),identifier(none,c))))])])).",
				result);
	}

	@Test
	public void testKeywordInRhs() throws BCompoundException {
		final String testMachine = "MACHINE Test\nDEFINITIONS\nSTACKSTART == (MAXVAR+1); X==5\nEND";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[expression_definition(none,'STACKSTART',[],add(none,identifier(none,'MAXVAR'),integer(none,1))),expression_definition(none,'X',[],integer(none,5))])])).",
				result);
	}

	@Test
	public void testDefInLocalScope() throws BCompoundException {
		// DEFINITIONS cannot be shadowed by local variables
		final String testMachine = "MACHINE Test\nDEFINITIONS def_expr1 == varname \n OPERATIONS op = PRE # def_expr1 . (def_expr1 < 43) THEN skip END END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[expression_definition(none,def_expr1,[],identifier(none,varname))]),operations(none,[operation(none,identifier(none,op),[],[],precondition(none,exists(none,[definition(none,def_expr1,[])],less(none,definition(none,def_expr1,[]),integer(none,43))),skip(none)))])])).",
				result);
	}

	@Test
	public void testUnparsableRhs() {
		final String testMachine = "MACHINE Test\nDEFINITIONS def_expr1 == 42 < \n OPERATIONS op = PRE def_expr1 THEN skip END END";
		assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test
	public void testExprInParanthesis() throws BCompoundException {
		final String testMachine = "MACHINE BoolLaws\nDEFINITIONS\npt == (PP=TRUE);\nqt == (QQ=TRUE)\nEND";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'BoolLaws',[]),[definitions(none,[predicate_definition(none,pt,[],equal(none,identifier(none,'PP'),boolean_true(none))),predicate_definition(none,qt,[],equal(none,identifier(none,'QQ'),boolean_true(none)))])])).",
				result);
	}

	@Test
	public void testDoubleSemicolon() {
		final String testMachine = "MACHINE Test\nDEFINITIONS\npt == (PP=TRUE);;\nqt == (QQ=TRUE)\nEND";
		assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test
	public void testDefClause() throws BCompoundException {
		String testClause = "DEFINITIONS def2 == y;\ndef1 == xx";
		String result = Helpers.getMachineClauseAsPrologTerm(testClause);

		assertEquals(
				"machine(definitions(none,[expression_definition(none,def2,[],identifier(none,y)),expression_definition(none,def1,[],identifier(none,xx))])).",
				result);
	}

	@Test
	public void testSemicolonInDef1() throws BCompoundException {
		String testClause = "DEFINITIONS def1 == (f;g); def2 == skip";
		String result = Helpers.getMachineClauseAsPrologTerm(testClause);

		assertEquals(
				"machine(definitions(none,[expression_definition(none,def1,[],composition(none,identifier(none,f),identifier(none,g))),substitution_definition(none,def2,[],skip(none))])).",
				result);
	}

	@Test
	public void testSemicolonInDef2() throws BCompoundException {
		String testClause = "DEFINITIONS law6 ==  (dom((ff ; (gg~))) <: dom(ff))";
		String result = Helpers.getMachineClauseAsPrologTerm(testClause);

		assertEquals(
				"machine(definitions(none,[predicate_definition(none,law6,[],subset(none,domain(none,composition(none,identifier(none,ff),reverse(none,identifier(none,gg)))),domain(none,identifier(none,ff))))])).",
				result);
	}

	@Test
	public void testSemicolonInDef3() throws BCompoundException {
		String testClause = "DEFINITIONS\n  law1 ==  (dom(ff\\/gg) = dom(ff) \\/ dom(gg));\n  law2 ==  (ran(ff\\/gg) = ran(ff) \\/ ran(gg));\n  law3 ==  (dom(ff/\\gg) <: dom(ff) /\\ dom(gg));\n  law4 ==  (ran(ff/\\gg) <: ran(ff) /\\ ran(gg));\n  law5 ==  ( (ff \\/ gg)~ = ff~ \\/ gg~);\n  law6 ==  (dom((ff ; (gg~))) <: dom(ff));\n  law7 ==  (!(xx,yy).(xx:setX & yy:setY & xx|->yy : ff  =>  yy: ran(gg))\n              =>  (dom((ff ; (gg~))) = dom(ff)));\n  law8 ==  (ff : setX --> setY  <=>  (ff: setX +-> setY & dom(ff) = setX));\n  ff_is_pf == (!(xx,yy,zz).((xx:setX & yy:setY & zz:setY &\n                    xx|->yy:ff & xx|->zz:ff) => (yy=zz)));\n  law9 ==  (ff : setX +-> setY  <=> ff_is_pf);\n  law10 == (ff : setX >->> setY  <=>  (ff : setX >-> setY  &  ff~: setY >-> setX));\n  law11 == (ff : setX >+> setY  <=> (ff: setX +-> setY &\n                                !(xx,yy).(xx:setX & yy:setX & xx/=yy & xx:dom(ff) & yy: dom(ff)  => ff(xx)/=ff(yy)))) ;\n  law12 == (ff : setX +->> setY  <=>  (ff: setX +-> setY &\n                                    !yy.(yy:setY => yy: ran(ff))))";
		Helpers.getMachineClauseAsPrologTerm(testClause);
	}

	@Test
	public void testDefWithNesting1() throws BCompoundException {
		String testClause = "DEFINITIONS CONSTR3 == (!(f,p).(f:FLIGHTS & f<NRF-1 & p:PERSONNEL &  f|->p:assign & (f+1)|->p:assign => (f+2)|->p /: assign))";
		Helpers.getMachineClauseAsPrologTerm(testClause);
	}

	@Test
	public void testDefWithNesting2() throws BCompoundException {
		String testClause = "DEFINITIONS FT_TYPE == (from:NODES & to:NODES & from/=to);\n FTE_TYPE == (FT_TYPE & packet:PACKETS & type:TYPE)";
		Helpers.getMachineClauseAsPrologTerm(testClause);
	}

	@Test
	public void testDefWithNesting3() throws BCompoundException {
		String testClause = "DEFINITIONS\nFaileSafeIsOn == (sw>0);\nTurnFailSafeOff == BEGIN sw := 0 END;\nTurnFailSafeOn == BEGIN sw := (sw + 1) mod 256\nEND";
		Helpers.getMachineClauseAsPrologTerm(testClause);
	}

	@Test
	public void testDefWithNesting4() throws BCompoundException {
		String testClause = "DEFINITIONS\nlaw1 ==  (SS \\/ SS = SS  &  SS = SS \\/ {}  &  SS = SS /\\ SS  &  SS = SS \\ {})";
		Helpers.getMachineClauseAsPrologTerm(testClause);
	}

	@Test
	public void testSemicolonAtEnd1() throws BCompoundException {
		final String testMachine = "MACHINE Test\nDEFINITIONS\npt == PP=TRUE;\nqt == QQ=TRUE\nEND\n";
		Helpers.getMachineAsPrologTerm(testMachine);
	}

	@Test
	public void testSemicolonAtEnd2() throws BCompoundException {
		final String testMachine = "MACHINE Test\nDEFINITIONS\npt == PP=TRUE;\nqt == QQ=TRUE;\nEND\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[predicate_definition(none,pt,[],equal(none,identifier(none,'PP'),boolean_true(none))),predicate_definition(none,qt,[],equal(none,identifier(none,'QQ'),boolean_true(none)))])])).",
				result);
	}

	@Test
	public void testExprOrSubst1() throws BCompoundException {
		final String testMachine = "MACHINE Test\nDEFINITIONS\ndefSubst==g(x)\nOPERATIONS\nop=defSubst\nEND";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[substitution_definition(none,defSubst,[],operation_call(none,identifier(none,g),[],[identifier(none,x)]))]),operations(none,[operation(none,identifier(none,op),[],[],definition(none,defSubst,[]))])])).",
				result);
	}

	@Test
	public void testExprOrSubst2() throws BCompoundException {
		final String testMachine = "MACHINE Test\nDEFINITIONS\ndefSubst==g(x)\nOPERATIONS\nop= a:=defSubst\nEND";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[expression_definition(none,defSubst,[],function(none,identifier(none,g),[identifier(none,x)]))]),operations(none,[operation(none,identifier(none,op),[],[],assign(none,[identifier(none,a)],[definition(none,defSubst,[])]))])])).",
				result);
	}

	@Test
	public void testExprOrSubst3() {
		final String testMachine = "MACHINE Test\nDEFINITIONS\ndefExpr==g(x)\nOPERATIONS\nop=PRE defExpr=42 THEN defExpr END\nEND";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertEquals("Expecting substitution here but found definition with type 'Expression'", e.getLocalizedMessage());
	}

	@Test
	public void testExprOrSubst4() {
		final String testMachine = "MACHINE Test\nDEFINITIONS\ndefExpr==g(x)\nOPERATIONS\nop=BEGIN defExpr; a:=defExpr END\nEND";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertEquals("Expecting expression here but found definition with type 'Substitution'", e.getLocalizedMessage());
	}

	@Test
	public void testExprOrSubstWParams1() throws BCompoundException {
		final String testMachine = "MACHINE Test\nDEFINITIONS\ndefSubst(x)==g(x)\nOPERATIONS\nop=defSubst(x)\nEND";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[substitution_definition(none,defSubst,[identifier(none,x)],operation_call(none,identifier(none,g),[],[identifier(none,x)]))]),operations(none,[operation(none,identifier(none,op),[],[],definition(none,defSubst,[identifier(none,x)]))])])).",
				result);
	}

	@Test
	public void testExprOrSubstWParams2() throws BCompoundException {
		final String testMachine = "MACHINE Test\nDEFINITIONS\ndefSubst(x)==g(x)\nOPERATIONS\nop= a:=defSubst(x)\nEND";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[expression_definition(none,defSubst,[identifier(none,x)],function(none,identifier(none,g),[identifier(none,x)]))]),operations(none,[operation(none,identifier(none,op),[],[],assign(none,[identifier(none,a)],[definition(none,defSubst,[identifier(none,x)])]))])])).",
				result);
	}

	@Test
	public void testExprOrSubstWParams3() {
		final String testMachine = "MACHINE Test\nDEFINITIONS\ndefExpr(x)==g(x)\nOPERATIONS\nop=PRE defExpr(x)=42 THEN defExpr(x) END\nEND";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertEquals("Expecting substitution here but found definition with type 'Expression'", e.getLocalizedMessage());
	}

	@Test
	public void testExprOrSubstWParams4() {
		final String testMachine = "MACHINE Test\nDEFINITIONS\ndefExpr(x)==g(x)\nOPERATIONS\nop=BEGIN defExpr(x); a:=defExpr(x) END\nEND";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertEquals("Expecting expression here but found definition with type 'Substitution'", e.getLocalizedMessage());
	}

	@Test
	public void testMissingParamsSubst() {
		final String testMachine = "MACHINE Test\nDEFINITIONS\ndefExpr(x)==g(x)\nOPERATIONS\nop=BEGIN defExpr(x); defExpr END\nEND";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertEquals(1, e.getNodesList().size());
		assertNotNull(e.getNodesList().get(0));
		assertEquals("Number of parameters (0) doesn't match declaration of definition defExpr (1)", e.getMessage());
	}

	@Test
	public void testTooManyParamsSubst() {
		final String testMachine = "MACHINE Test\nDEFINITIONS\ndefExpr(x)==g(x)\nOPERATIONS\nop=BEGIN defExpr(x); defExpr(1,2) END\nEND";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertEquals(1, e.getNodesList().size());
		assertNotNull(e.getNodesList().get(0));
		assertEquals("Number of parameters (2) doesn't match declaration of definition defExpr (1)", e.getMessage());
	}

	@Test
	public void testMissingParamsExpr() {
		final String testMachine = "MACHINE Test\nDEFINITIONS\ndefExpr(x)==g(x)\nPROPERTIES\ndefExpr = 1\nEND";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertEquals(1, e.getNodesList().size());
		assertNotNull(e.getNodesList().get(0));
		assertEquals("Number of parameters (0) doesn't match declaration of definition defExpr (1)", e.getMessage());
	}

	@Test
	public void testTooManyParamsExpr() {
		final String testMachine = "MACHINE Test\nDEFINITIONS\ndefExpr(x)==g(x)\nPROPERTIES\ndefExpr(1,2) = 3\nEND";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertEquals(1, e.getNodesList().size());
		assertNotNull(e.getNodesList().get(0));
		assertEquals("Number of parameters (2) doesn't match declaration of definition defExpr (1)", e.getMessage());
	}

	@Test
	public void testMissingParamsPred() {
		final String testMachine = "MACHINE Test\nDEFINITIONS\ndefPred(x)==1=1\nPROPERTIES\ndefPred\nEND";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertEquals(1, e.getNodesList().size());
		assertNotNull(e.getNodesList().get(0));
		assertEquals("Number of parameters (0) doesn't match declaration of definition defPred (1)", e.getMessage());
	}

	@Test
	public void testTooManyParamsPred() {
		final String testMachine = "MACHINE Test\nDEFINITIONS\ndefPred(x)==1=1\nPROPERTIES\ndefPred(1,2)\nEND";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertEquals(1, e.getNodesList().size());
		assertNotNull(e.getNodesList().get(0));
		assertEquals("Number of parameters (2) doesn't match declaration of definition defPred (1)", e.getMessage());
	}

	@Test
	public void testDefOrder() throws BCompoundException {
		final String testMachine = "MACHINE Test  \n DEFINITIONS  \n bar(y) == foo(y);  \n foo(x)==x<3;  \n END";
		String asString = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[predicate_definition(none,bar,[identifier(none,y)],definition(none,foo,[identifier(none,y)])),predicate_definition(none,foo,[identifier(none,x)],less(none,identifier(none,x),integer(none,3)))])])).",
				asString);
	}

	@Test
	public void testAssertInDefinition() throws BCompoundException {
		final String testMachine = "MACHINE Test\n" + "DEFINITIONS\n" + "ABORT == ASSERT TRUE=FALSE THEN skip END\n"
				+ "END\n";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[definitions(none,[substitution_definition(none,'ABORT',[],assertion(none,equal(none,boolean_true(none),boolean_false(none)),skip(none)))])])).",
				result);
	}

	@Test(expected = BCompoundException.class)
	public void testDetectCycleInDefinitions() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS foo == 1=1 & foo END\n";
		Helpers.getMachineAsPrologTerm(testMachine);
	}

	@Test(expected = BCompoundException.class)
	public void testDetectCycleInDefinitions2() throws BCompoundException {
		final String testMachine = "MACHINE Test DEFINITIONS bar == foo; foo == 1=1 & bar END\n";
		Helpers.getMachineAsPrologTerm(testMachine);
	}

	
	@Ignore
	@Test
	public void testMisleadingParseError() throws BCompoundException {
		final String testMachine = "MACHINE foo \nDEFINITIONS \nCONSTANTS a \nPROPERTIES a = 1  END";
		String output = Helpers.getMachineAsPrologTerm(testMachine);
		//TODO
	}

}
