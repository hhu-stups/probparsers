package de.be4.classicalb.core.parser.definitions;

import java.io.IOException;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.exceptions.PreParseException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertTrue;

public class LoadingDefinitionFilesTest {

	
	@Test
	public void testDefinitionFileLoadedByADefinitionFile() throws IOException, BCompoundException {
		String PATH = "definitions/definitionFiles/";
		String file = PATH + "MachineIncludingDefinitionFiles.mch";
		String result = Helpers.parseFile(file);
		assertTrue(result.contains("MachineIncludingDefinitionFiles.mch"));
		assertTrue(result.contains("Foo.def"));
		assertTrue(result.contains("Bar.def"));
	}
	
	@Test
	public void testDefinitionFileDoesNotExists() {
		String PATH = "definitions/definitionFiles/";
		String file = PATH + "MachineIncludingNotExistingDefinitionFile.mch";
		final PreParseException e = Helpers.assertThrowsCompound(PreParseException.class, () -> Helpers.parseFile(file));
		assertTrue(e.getMessage().contains("Definition file cannot be read"));
	}
	
	@Test
	public void testCyclicDefinitionFile() {
		String PATH = "definitions/definitionFiles/cycle/";
		String file = PATH + "MachineIncludingCyclicDefinitionFiles.mch";
		final PreParseException e = Helpers.assertThrowsCompound(PreParseException.class, () -> Helpers.parseFile(file));
		assertTrue(e.getMessage().contains("Cyclic references in definition files"));
	}
	
	@Test
	public void testLoadingDefinitionsFilesFormAnotherMachine() throws IOException, BCompoundException {
		String PATH = "definitions/definitionFiles/";
		String file = PATH + "MachineIncludingDefinitionsFromAnotherMachine.mch";
		String result = Helpers.parseFile(file);
		assertTrue(result.contains("DefinitionOfMachineWithDefinitions"));
	}
	
	@Test
	public void testLoadingDefinitionsFilesInSubdirectory() throws IOException, BCompoundException {
		String PATH = "definitions/definitionFiles/";
		String file = PATH + "MachineIncludingDefinitionFileInSubdirectory.mch";
		String result = Helpers.parseFile(file);
		assertTrue(result.contains("DefinitionInSubdirectory"));
	}
	
	@Test
	public void testLoadingDefinitionChain() throws IOException, BCompoundException {
		String PATH = "definitions/definitionFiles/chain/";
		String file = PATH + "MachineIncludingDefinitionFileChain.mch";
		String result = Helpers.parseFile(file);
		assertTrue(result.contains("A.def"));
		assertTrue(result.contains("B.def"));
		assertTrue(result.contains("C.def"));
	}
	
	@Test
	public void testOverridingDefinition() {
		String PATH = "definitions/definitionFiles/";
		String file = PATH + "MachineOverridingDefinition.mch";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.parseFile(file));
		assertTrue(e.getMessage().contains("Duplicate definition: DefinitionOfBar"));
	}
	
	@Test
	public void testOverridingDefinition2() {
		String PATH = "definitions/definitionFiles/overridingDefinition/";
		String file = PATH + "MachineIncludesA.mch";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.parseFile(file));
		assertTrue(e.getMessage().contains("Duplicate definition: A1"));
	}
	
	@Test
	public void testOverridingDefinition3() {
		String PATH = "definitions/definitionFiles/overridingDefinition/";
		String file = PATH + "MachineIncludesAAndA2.mch";
		Helpers.assertThrowsCompound(PreParseException.class, () -> Helpers.parseFile(file));
	}
	
	@Test
	public void testSeesAndIncludes() throws IOException, BCompoundException {
		String PATH = "definitions/definitionFiles/seesAndIncludes/";
		String file = PATH + "A.mch";
		String result = Helpers.parseFile(file);
	}
	
}
