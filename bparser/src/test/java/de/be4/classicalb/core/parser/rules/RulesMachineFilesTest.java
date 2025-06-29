package de.be4.classicalb.core.parser.rules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.*;

import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.exceptions.BParseException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.rules.RulesMachineRunConfiguration.RuleGoalAssumption;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class RulesMachineFilesTest {
	@Test
	public void testSyntax() throws BCompoundException {
		String output = RulesUtil.getFileAsPrologTerm("project/RulesMachineSyntax.rmch");
	}

	@Test
	public void testMachineIncludingDefsFile() throws BCompoundException {
		String output = RulesUtil.getFileAsPrologTerm("project/project_with_def_file/Main.rmch", true);
		assertTrue(output.contains("Defs.def"));
		assertTrue(output.contains("expression_definition(p4(3,2,3,16),'FooValue'"));
		// also check some quoted defs:
		assertTrue(output.contains("predicate_definition(p4(3,3,3,24),'QuotedDef'"));
		assertTrue(output.contains("substitution_definition(p4(-1,5,3,20),'MainDef'"));
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
	public void testFunctionCalledAsExpression() throws BCompoundException {
		String result = RulesUtil.getFileAsPrologTerm("FunctionCalledAsExpression.rmch");
	}
	
	@Test
	public void testFunctionUsesDefinitionOfCallingComputation() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("FunctionUsesDefinitionOfCallingComputation.rmch"));
		assertEquals("Cyclic dependencies between operations: compute_xx -> FUNC_add -> compute_xx", e.getMessage());
	}

	@Test
	public void testSelfReferenceException() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("project/SelfReference.rmch"));
		assertEquals("The reference 'SelfReference' has the same name as the machine in which it is contained.", e.getMessage());
	}

	@Test
	public void testReadXML() throws BCompoundException {
		String result = RulesUtil.getFileAsPrologTerm("ReadXML.rmch");
	}

	@Test
	public void testReadInvalidXML() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("ReadInvalidXML.rmch"));
		assertEquals("XML document structures must start and end within the same entity.", e.getMessage());
	}

	@Test
	public void testRulesMachineConfiguration() throws Exception {
		File file = new File(this.getClass().getResource("/rules/project/RulesMachineConfigurationTest.rmch").toURI());
		ParsingBehaviour parsingBehaviour = new ParsingBehaviour();
		parsingBehaviour.setAddLineNumbers(true);
		parsingBehaviour.setPrologOutput(true);
		RulesProject project = new RulesProject();
		project.parseProject(file);
		project.checkAndTranslateProject();
		RulesMachineRunConfiguration rulesMachineRunConfiguration = project.getRulesMachineRunConfiguration();
		Set<RuleGoalAssumption> rulesGoalAssumptions = rulesMachineRunConfiguration.getRulesGoalAssumptions();
		assertEquals(2, rulesGoalAssumptions.size());
		for (RuleGoalAssumption next : rulesGoalAssumptions) {
			if ("rule1".equals(next.getRuleName())) {
				assertEquals(new HashSet<>(Collections.singletonList(1)), next.getErrorTypesAssumedToSucceed());
				assertTrue(next.isCheckedForCounterexamples());
				assertEquals("rule1", next.getRuleOperation().getOriginalName());
			} else {
				assertEquals("rule2", next.getRuleName());
				assertEquals(new HashSet<>(Collections.singletonList(1)), next.getErrorTypesAssumedToFail());
				assertFalse(next.isCheckedForCounterexamples());
			}
		}
	}

	@Test
	public void testCyclicComputationDependencies() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("CyclicComputationDependencies.rmch"));
		assertTrue(e.getMessage().contains("Cyclic dependencies between operations"));
	}

	@Test
	public void testPackage() throws BCompoundException {
		String result = RulesUtil.getFileAsPrologTerm("project/references/folder/M1.rmch");
	}

	@Test
	public void testFreetypesInMainMachine() throws BCompoundException {
		final String result = RulesUtil.getFileAsPrologTerm("project/RulesMachineFreetypes.rmch");
	}

	@Test
	public void testFreetypesInReferencedMachine() throws BCompoundException {
		final String result = RulesUtil.getFileAsPrologTerm("project/RulesMachineReferencesFreetypes.rmch");
	}

	@Test
	public void testForAll() throws BCompoundException {
		final String result = RulesUtil.getFileAsPrologTerm("ForAllPredicate.rmch");
	}

	@Test
	public void testTransitiveDependency() throws BCompoundException {
		String result = RulesUtil.getFileAsPrologTerm("project/TransitiveDependency.rmch");
	}

	@Test
	public void testDisabled() throws BCompoundException {
		String result = RulesUtil.getFileAsPrologTerm("project/Disabled.rmch");
	}

	@Test
	public void testTransitiveDependencyRule() throws BCompoundException {
		String result = RulesUtil.getFileAsPrologTerm("project/TransitiveDependencyRule.rmch");
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
		assertTrue(e.getCause() instanceof FileNotFoundException || e.getCause() instanceof NoSuchFileException);
		assertTrue(e.getMessage().contains("FileDoesNotExist.rmch"));
	}

	@Test
	public void testUnknownRule() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("project/UnknownRule.rmch"));
		String expected = "Unknown operation: ";
		assertTrue(e.getMessage().contains(expected));
	}

	@Test
	public void testUnknownIdentifier() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("project/UnknownIdentifier.rmch"));
		String expected = "Unknown identifier ";
		assertTrue(e.getMessage().contains(expected));
	}

	@Test
	public void testParseError() {
		final BParseException e = Helpers.assertThrowsCompound(BParseException.class, () -> RulesUtil.getFileAsPrologTerm("project/ParseError.rmch"));
		String expected = "[4,1] expecting: ";
		assertTrue(e.getMessage().contains(expected));
	}

	@Test
	public void testFileNameDoesNotMatchMachineName1() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("project/DifferentFileName.rmch"));
		Helpers.assertParseErrorLocation(new BException("",e), 1, 15, 1, 27);
		assertTrue(e.getMessage().contains("Machine name does not match the file name: 'RulesMachine' vs 'DifferentFileName'"));
	}

	@Test
	public void testFileNameDoesNotMatchMachineName2() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("project/RulesMachineNameDoesNotMatchFileName.rmch"));
		Helpers.assertParseErrorLocation(new BException("",e), 1, 15, 1, 26);
		assertTrue(e.getMessage().contains("Machine name does not match the file name: 'InvalidName' vs 'RulesMachineNameDoesNotMatchFileName'"));
	}

	@Test
	public void testRuleDependsOnItSelf() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("project/RuleDependsOnItSelf.rmch"));
		assertEquals("Cyclic dependencies between operations: rule1 -> rule1", e.getMessage());
	}

	@Test
	public void testFilePragma() throws BCompoundException {
		String result = RulesUtil.getFileAsPrologTerm("project/references/FilePragma.rmch");
	}

	@Test
	public void testDirectoryInFilePragma() {
		Helpers.assertThrowsCompound(IOException.class, () -> RulesUtil.getFileAsPrologTerm("project/references/DirectoryInFilePragma.rmch"));
	}

	@Test
	public void testFileDoesNotExistInFilePragma() {
		Helpers.assertThrowsCompound(NoSuchFileException.class, () -> RulesUtil.getFileAsPrologTerm("project/references/FileDoesNotExistInFilePragma.rmch"));
	}

	@Test
	public void testFunctionDependencies() throws BCompoundException {
		String result = RulesUtil.getFileAsPrologTerm("FunctionDependencies.rmch");
	}

	@Test
	public void testReferencedMachineNotFound() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("project/references/ReferencedMachineNotFound.rmch"));
		assertTrue(e.getMessage().contains("Machine not found"));
	}

	@Test
	public void testReferenceClassicalMachines() throws BCompoundException {
		String result = RulesUtil.getFileAsPrologTerm("project/references/RulesReferencesClassicalRef0.rmch");
	}

	@Test
	public void testReferenceClassicalMachinesViaRulesMachine() throws BCompoundException {
		RulesUtil.getFileAsPrologTerm("project/references/RulesReferencesClassical.rmch");
	}

	@Test
	public void testPackagePragma() throws BCompoundException {
		String result = RulesUtil.getFileAsPrologTerm("project/references/PackagePragma.rmch");
	}

	@Test
	public void testReplacement() throws BCompoundException {
		String result = RulesUtil.getFileAsPrologTerm("project/references/Replacement.rmch");
		// the result should not contain name of the replacement operation
		assertFalse(result.contains("COMP_comp2New"));
	}

	@Test
	public void testImportedPackageDoesNotExist() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("project/references/packagePragma/ImportedPackageDoesNotExist.rmch"));
		assertTrue(e.getMessage().contains("Imported package does not exist"));
	}

	@Test
	public void testImportedPackageDoesNotExist2() {
		final BCompoundException e = assertThrows(BCompoundException.class, () -> RulesUtil.getFileAsPrologTerm("project/references/packagePragma/ImportedPackageDoesNotExist2.rmch"));
		Helpers.assertParseErrorLocation(e, 2, 1, 3, 38);
		assertTrue(e.getMessage().contains("Imported package does not exist"));
	}

	@Test
	public void testDuplicatePackageImport() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("project/references/packagePragma/DuplicatePackageImport.rmch"));
		assertTrue(e.getMessage().contains("Duplicate import statement"));
	}

	@Test
	public void testInvalidPackagePragma() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("project/references/InvalidPackagePragma.rmch"));
		assertTrue(e.getMessage().contains("does not match the folder structure"));
	}

	@Test
	public void testInvalidPackagePragma2() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("project/references/InvalidPackagePragma2.rmch"));
		assertTrue(e.getMessage().contains("Invalid package name"));
	}

	@Test
	public void testComputationDependsOnItSelf() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("project/ComputationDependsOnItSelf.rmch"));
		assertEquals("Cyclic dependencies between operations: compute_x -> compute_x", e.getMessage());
	}

	@Test
	public void testImplicitDependencyToComputation() throws BCompoundException {
		String result = RulesUtil.getFileAsPrologTerm("ImplicitDependencyToComputation.rmch");
	}

	@Test
	public void testConfuseRuleAndComputation() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("project/ConfuseRuleAndComputation.rmch"));
		assertEquals("Identifier 'rule1' is not a COMPUTATION.", e.getMessage());
	}

	@Test
	public void testCyclicRules() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("project/CyclicRules.rmch"));
		assertTrue(e.getMessage().contains("Cyclic dependencies between operations"));
	}

	@Test
	public void testInvisibleComputation() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("project/references/MissingReference/M1.rmch"));
		assertEquals("Operation 'compute_xx' is not visible in RULES_MACHINE 'M2'.", e.getMessage());
	}

	@Test
	public void testUnknwonComputation() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("project/references/MissingReference/M2.rmch"));
		assertEquals("Unknown operation: 'compute_xx'.", e.getMessage());
	}

	@Test
	public void testReplaces() throws BCompoundException {
		String result = RulesUtil.getFileAsPrologTerm("replaces/Replaces.rmch");
		assertFalse(result.contains("COMP_NewComp1"));
	}

	@Test
	public void testInvalidDoubleReplacement() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("replaces/DoubleReplacement.rmch"));
		assertTrue(e.getMessage().contains("COMP_comp1"));
	}

	@Test
	public void testVariableNotReplaced() {
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getFileAsPrologTerm("replaces/VariableNotReplaced.rmch"));
		assertTrue(e.getMessage().contains("COMP_comp1"));
	}

	@Test
	public void testDescriptionPragma() throws BCompoundException {
		String output = RulesUtil.getFileAsPrologTerm("project/Rule_42.rmch");
		assertTrue(output.contains("the answer"));
	}
}
