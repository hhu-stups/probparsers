package de.be4.classicalb.core.parser.pragmas;

import java.io.IOException;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.CheckException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PackagePragmaTest {

	@Test
	public void testImportPackagePragma() throws IOException, BCompoundException {
		String PATH = "pragmas/importPackagePragma/foo/";
		String file = PATH + "M1.mch";
		String result = Helpers.parseFile(file);
		assertTrue(result.contains(
				"machine(abstract_machine(none,machine(none),machine_header(none,'M1',[]),[sees(none,[identifier(none,'M2')])]))."));

	}

	@Test
	public void testImportPackageIdentifier() throws IOException, BCompoundException {
		String PATH = "pragmas/importPackagePragma/foo/";
		String file = PATH + "M11.mch";
		String result = Helpers.parseFile(file);
		assertTrue(result.contains(
				"machine(abstract_machine(none,machine(none),machine_header(none,'M11',[]),[sees(none,[identifier(none,'M2')])]))."));

	}

	@Test
	public void testInvalidImport() {
		String PATH = "pragmas/importPackagePragma/foo/";
		String file = PATH + "InvalidImport.mch";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.parseFile(file));
		assertEquals("Invalid package name foo.*.M2: name part * is not a valid package identifier", e.getMessage());
	}

	@Test
	public void testDuplicateImport() {
		String PATH = "pragmas/importPackagePragma/foo/";
		String file = PATH + "DuplicateImport.mch";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.parseFile(file));
		assertTrue(e.getMessage().contains(
				"Duplicate import statement: foo.bar"));

	}

	@Test
	public void testInvalidPackage() {
		String PATH = "pragmas/importPackagePragma/";
		String file = PATH + "InvalidPackage1.mch";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.parseFile(file));
		assertTrue(e.getMessage().contains("Package declaration 'foo2' does not match the folder structure"));
	}
	
	@Test
	public void testPackageNotFound() {
		String PATH = "pragmas/importPackagePragma/foo/";
		String file = PATH + "PackageNotFound.mch";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.parseFile(file));
		assertTrue(e.getMessage().contains("Imported package does not exist"));
	}
	
	@Test
	public void testDuplicateMachineClause() {
		Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.parseFile("pragmas/packagePragma/project1/Main.mch"));
		//semantic checks (e.g. duplicate clauses) were previously disabled for APackageParseUnit nodes
	}
	
	@Test
	public void testDefinitionFileReferencesInDefinitionFilesClause() throws IOException, BCompoundException {
		final String result = Helpers.parseFile("pragmas/packagePragma/definitionFiles/Main.mch");
	}


}
