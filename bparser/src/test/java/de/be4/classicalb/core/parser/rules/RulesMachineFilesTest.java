package de.be4.classicalb.core.parser.rules;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.PrologExceptionPrinter;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.rules.RulesMachineRunConfiguration.RuleGoalAssumption;
import de.prob.prolog.output.PrologTermStringOutput;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RulesMachineFilesTest {
	@Test
	public void testSyntax() throws BCompoundException {
		String output = RulesUtil.getFileAsPrologTerm("project/RulesMachineSyntax.rmch");
	}

	@Test
	public void testMachineIncludingDefsFile() throws Exception {
		String output = getRulesMachineAsPrologTerm("project/project_with_def_file/Main.rmch");
		assertTrue(output.contains("Defs.def"));
		assertTrue(output.contains("expression_definition(pos(104,3,2,3,2,16),'FooValue'"));
		//assertTrue(output.contains("expression_definition(p4(3,2,3,16),'FooValue'")); // new position format
        // requires parsingBehaviour.setCompactPrologPositions(); to be called
	}

	@Test
	public void testProject2() throws BCompoundException {
		final String result = RulesUtil.getFileAsPrologTerm("project/references/test1/Rule1.rmch");
	}

	@Test
	public void testImplicitDependenciesDueToFunctionCall() throws BCompoundException {
		final String result = RulesUtil.getFileAsPrologTerm("ImplicitDependencyDueFunctionCall.rmch");
	}

	@Test
	public void testFunctionCalledAsExpression() throws Exception {
		String result = getRulesMachineAsPrologTerm("FunctionCalledAsExpression.rmch");
		assertFalse(result.contains("exception"));
	}
	
	@Test
	public void testFunctionUsesDefinitionOfCallingComputation() throws Exception {
		String result = getRulesMachineAsPrologTerm("FunctionUsesDefinitionOfCallingComputation.rmch");
		assertTrue(result.contains("'Cyclic dependencies between operations: compute_xx -> FUNC_add -> compute_xx'"));
	}

	@Test
	public void testSelfReferenceException() throws Exception {
		String result = getRulesMachineAsPrologTerm("project/SelfReference.rmch");
		assertTrue(result
				.contains("'The reference \\'SelfReference\\' has the same name as the machine in which it is contained.'"));
	}

	@Test
	public void testReadXML() throws Exception {
		String result = getRulesMachineAsPrologTerm("ReadXML.rmch");
		assertFalse(result.contains("exception"));
	}

	@Test
	public void testReadInvalidXML() throws Exception {
		String result = getRulesMachineAsPrologTerm("ReadInvalidXML.rmch");
		assertTrue(result.contains("'XML document structures must start and end within the same entity.'"));

	}

	@Test
	public void testRulesMachineConfiguration() throws Exception {
		File file = new File(this.getClass().getClassLoader().getResource("rules/project/RulesMachineConfigurationTest.rmch").toURI());
		ParsingBehaviour parsingBehaviour = new ParsingBehaviour();
		parsingBehaviour.setAddLineNumbers(true);
		parsingBehaviour.setPrologOutput(true);
		RulesProject project = new RulesProject();
		project.parseProject(file);
		project.checkAndTranslateProject();
		RulesMachineRunConfiguration rulesMachineRunConfiguration = project.getRulesMachineRunConfiguration();
		Set<RuleGoalAssumption> rulesGoalAssumptions = rulesMachineRunConfiguration.getRulesGoalAssumptions();
		assertEquals(2, rulesGoalAssumptions.size());
		for (Iterator<RuleGoalAssumption> iterator = rulesGoalAssumptions.iterator(); iterator.hasNext();) {
			RuleGoalAssumption next = iterator.next();
			if ("rule1".equals(next.getRuleName())) {
				assertEquals(new HashSet<Integer>(Arrays.asList(1)), next.getErrorTypesAssumedToSucceed());
				assertEquals(true, next.isCheckedForCounterexamples());
				assertEquals("rule1", next.getRuleOperation().getOriginalName());
			} else {
				assertEquals("rule2", next.getRuleName());
				assertEquals(new HashSet<Integer>(Arrays.asList(1)), next.getErrorTypesAssumedToFail());
				assertEquals(false, next.isCheckedForCounterexamples());
			}
		}
	}

	@Test
	public void testRulesMachineNameDoesNotMatchFileName() throws Exception {
		String result = getRulesMachineAsPrologTerm(
				"project/RulesMachineNameDoesNotMatchFileName.rmch");
		assertTrue(result.contains("parse_exception("));
		assertTrue(result.contains("pos(1,15"));
		assertTrue(result.contains("RULES_MACHINE name must match the file name"));
	}

	@Test
	public void testCyclicComputationDependencies() throws Exception {
		String result = getRulesMachineAsPrologTerm("CyclicComputationDependencies.rmch");
		assertTrue(result.contains("Cyclic dependencies between operations"));
	}

	@Test
	public void testPackage() throws Exception {
		String result = getRulesMachineAsPrologTerm("project/references/folder/M1.rmch");
		assertFalse(result.contains("exception"));
	}

	@Test
	public void testForAll() throws BCompoundException {
		final String result = RulesUtil.getFileAsPrologTerm("ForAllPredicate.rmch");
	}

	@Test
	public void testTransitiveDependency() throws Exception {
		String result = getRulesMachineAsPrologTerm("project/TransitiveDependency.rmch");
		assertFalse(result.contains("exception"));
	}

	@Test
	public void testDisabled() throws Exception {
		String result = getRulesMachineAsPrologTerm("project/Disabled.rmch");
		assertFalse(result.contains("exception"));
	}

	@Test
	public void testTransitiveDependencyRule() throws Exception {
		String result = getRulesMachineAsPrologTerm("project/TransitiveDependencyRule.rmch");
		assertFalse(result.contains("exception"));
	}

	@Test
	public void testMainFileDoesNotExist() {
		// Cannot use RulesUtil here,
		// because it throws a different exception than RulesProject does
		// when asked to load a nonexistant file.
		final RulesProject project = new RulesProject();
		project.parseProject(new File("DirDoesNotExist/FileDoesNotExist.rmch"));
		assertTrue(project.hasErrors());
		final BException e = project.getBExceptionList().get(0);
		assertTrue(e.getCause() instanceof FileNotFoundException);
		assertTrue(e.getMessage().contains("FileDoesNotExist.rmch"));
	}

	@Test
	public void testUnknownRule() throws Exception {
		String result = getRulesMachineAsPrologTerm("project/UnknownRule.rmch");
		String expected = "Unknown operation: ";
		assertTrue(result.contains(expected));
	}

	@Test
	public void testUnknownIdentifier() throws Exception {
		String result = getRulesMachineAsPrologTerm("project/UnknownIdentifier.rmch");
		String expected = "Unknown identifier ";
		assertTrue(result.contains(expected));
	}

	@Test
	public void testParseError() throws Exception {
		String result = getRulesMachineAsPrologTerm("project/ParseError.rmch");
		String expected = "[4,1] expecting: ";
		assertTrue(result.contains(expected));
	}

	@Test
	public void testFileNameDoesNotMatchMachineName() {
		String result = getRulesMachineAsPrologTerm("project/DifferentFileName.rmch");
		String expected = "RULES_MACHINE name must match the file name";
		assertTrue(result.contains(expected));
	}

	@Test
	public void testRuleDependsOnItSelf() {
		String result = getRulesMachineAsPrologTerm("project/RuleDependsOnItSelf.rmch");
		String expected = "Cyclic dependencies between operations: rule1 -> rule1').";
		assertTrue(result.contains(expected));
	}

	@Test
	public void testFilePragma() {
		String result = getRulesMachineAsPrologTerm("project/references/FilePragma.rmch");
		assertTrue(!result.contains("exception"));
	}

	@Test
	public void testInvalidFilePragma() {
		String result = getRulesMachineAsPrologTerm(
				"project/references/DirectoryInFilePragma.rmch");
		assertTrue(result.contains("is a directory"));
	}

	@Test
	public void testFileDoesNotExistInFilePragma() {
		String result = getRulesMachineAsPrologTerm(
				"project/references/FileDoesNotExistInFilePragma.rmch");
		assertTrue(result.contains("parse_exception"));
		assertTrue(result.contains("does not exist"));
	}

	@Test
	public void testFunctionDependencies() {
		String result = getRulesMachineAsPrologTerm("FunctionDependencies.rmch");
		assertTrue(!result.contains("exception"));
	}

	@Test
	public void testReferencedMachineNotFound() {
		String result = getRulesMachineAsPrologTerm(
				"project/references/ReferencedMachineNotFound.rmch");
		assertTrue(result.contains("parse_exception"));
		assertTrue(result.contains("Machine not found"));
	}

	@Test
	public void testPackagePragma() {
		String result = getRulesMachineAsPrologTerm("project/references/PackagePragma.rmch");
		assertFalse(result.contains("exception"));
	}

	@Test
	public void testReplacement() {
		String result = getRulesMachineAsPrologTerm("project/references/Replacement.rmch");
		assertFalse(result.contains("exception"));
		// the result should not contain name of the replacement operation
		assertFalse(result.contains("COMP_comp2New"));
	}

	@Test
	public void testImportedPackageDoesNotExist() {
		String result = getRulesMachineAsPrologTerm(
				"project/references/packagePragma/ImportedPackageDoesNotExist.rmch");
		assertTrue(result.contains("exception"));
		assertTrue(result.contains("Imported package does not exist"));

	}

	@Test
	public void testImportedPackageDoesNotExist2() {
		String result = getRulesMachineAsPrologTerm(
				"project/references/packagePragma/ImportedPackageDoesNotExist2.rmch");
		assertTrue(result.contains("parse_exception("));
		assertTrue(result.contains("pos(3,19,"));
		assertTrue(result.contains("Imported package does not exist"));

	}

	@Test
	public void testDuplicatePackageImport() {
		String result = getRulesMachineAsPrologTerm(
				"project/references/packagePragma/DuplicatePackageImport.rmch");
		assertTrue(result.contains("exception"));
		assertTrue(result.contains("Duplicate package import"));

	}

	@Test
	public void testInvalidPackagePragma() {
		String result = getRulesMachineAsPrologTerm(
				"project/references/InvalidPackagePragma.rmch");
		assertTrue(result.contains("does not match the folder structure"));
	}

	@Test
	public void testInvalidPackagePragma2() {
		String result = getRulesMachineAsPrologTerm(
				"project/references/InvalidPackagePragma2.rmch");
		assertTrue(result.contains("Invalid folder name"));
	}

	@Test
	public void testComputationDependsOnItSelf() {
		String result = getRulesMachineAsPrologTerm("project/ComputationDependsOnItSelf.rmch");
		String expected = "Cyclic dependencies between operations: compute_x -> compute_x').";
		assertTrue(result.contains(expected));
	}

	@Test
	public void testImplicitDependencyToComputation() {
		String result = getRulesMachineAsPrologTerm("ImplicitDependencyToComputation.rmch");
		assertFalse(result.contains("exception"));
	}

	@Test
	public void testConfuseRuleAndComputation() {
		String result = getRulesMachineAsPrologTerm("project/ConfuseRuleAndComputation.rmch");
		String expected = "Identifier \\'rule1\\' is not a COMPUTATION.').";
		assertTrue(result.contains(expected));
	}

	@Test
	public void testCyclicRules() {
		String result = getRulesMachineAsPrologTerm("project/CyclicRules.rmch");
		String expected = "Cyclic dependencies between operations";
		assertTrue(result.contains(expected));
	}

	@Test
	public void testInvisibleComputation() {
		String result = getRulesMachineAsPrologTerm(
				"project/references/MissingReference/M1.rmch");
		String expected = "Operation \\'compute_xx\\' is not visible in RULES_MACHINE \\'M2\\'.";
		assertTrue(result.contains(expected));
	}

	@Test
	public void testUnknwonComputation() {
		String result = getRulesMachineAsPrologTerm(
				"project/references/MissingReference/M2.rmch");
		String expected = "Unknown operation: \\'compute_xx\\'.')";
		assertTrue(result.contains(expected));
	}

	@Test
	public void testReplaces() {
		String result = getRulesMachineAsPrologTerm("replaces/Replaces.rmch");
		assertFalse(result.contains("exception"));
		assertFalse(result.contains("COMP_NewComp1"));
	}

	@Test
	public void testInvalidDoubleReplacement() {
		String result = getRulesMachineAsPrologTerm("replaces/DoubleReplacement.rmch");
		assertTrue(result.contains("exception"));
		assertTrue(result.contains("COMP_comp1"));
	}

	@Test
	public void testVariableNotReplaced() {
		String result = getRulesMachineAsPrologTerm("replaces/VariableNotReplaced.rmch");
		assertTrue(result.contains("exception"));
		assertTrue(result.contains("COMP_comp1"));
	}

	private String getRulesMachineAsPrologTerm(String fileName) {
		try {
			return RulesUtil.getFileAsPrologTerm(fileName, true);
		} catch (BCompoundException e) {
			final PrologTermStringOutput pout = new PrologTermStringOutput();
			PrologExceptionPrinter.printException(pout, e, false, false);
			pout.flush();
			pout.fullstop();
			return pout.toString();
		}
	}

}
