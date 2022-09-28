package de.be4.classicalb.core.parser.pragmas;

import java.io.IOException;
import java.net.URISyntaxException;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BParseException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertTrue;

public class FilePragmaTest {

	@Test
	public void testFilePragma() throws IOException, BCompoundException {
		String PATH = "pragmas/filePragma/";
		String file = PATH + "Main1.mch";
		Helpers.parseFile(file);
	}

	@Test
	public void testInvalidUseOfFilePragma() {
		final String testMachine = "MACHINE foo CONSTANTS a PROPERTIES a /*@file \"foo1/foo2.mch\" */  END";
		Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test(expected = BCompoundException.class)
	public void testFilePragma2() throws IOException, BCompoundException {
		String PATH = "pragmas/filePragma/";
		String file = PATH + "Main2.mch";
		Helpers.parseFile(file);
	}

	@Test
	public void testFilePragmaExtends() throws IOException, BCompoundException {
		String PATH = "pragmas/filePragma/";
		String file = PATH + "Extends.mch";
		Helpers.parseFile(file);
	}

	@Test
	public void testFilePragma3() throws IOException, BCompoundException {
		String PATH = "pragmas/filePragma/";
		String file = PATH + "Main1.mch";
		Helpers.parseFile(file);
	}

	@Test
	public void testFilePragmaDefinitionsFiles() throws IOException, BCompoundException {
		String PATH = "pragmas/filePragma/";
		String file = PATH + "Main3.mch";
		Helpers.parseFile(file);
	}

	@Test(expected = BCompoundException.class)
	public void testFileCircle() throws IOException, BCompoundException {
		String PATH = "pragmas/filePragma/circle/";
		String file = PATH + "Mch1.mch";
		Helpers.parseFile(file);
	}

	@Test(expected = BCompoundException.class)
	public void testFileCircle_complex() throws IOException, BCompoundException {
		String PATH = "pragmas/filePragma/circle/complex/";
		String file = PATH + "Mch1.mch";
		Helpers.parseFile(file);
	}


	@Test(expected = BCompoundException.class)
	public void testFileCircle_uses() throws IOException, BCompoundException {
		String PATH = "pragmas/filePragma/circle/uses/";
		String file = PATH + "Mch1.mch";
		Helpers.parseFile(file);
	}

	@Test(expected = BCompoundException.class)
	public void testFileCircle_includes() throws IOException, BCompoundException {
		String PATH = "pragmas/filePragma/circle/includesExtendsImports/";
		String file = PATH + "Mch1.mch";
		Helpers.parseFile(file);
	}

	@Test(expected = BCompoundException.class)
	public void testFileCircle_extends() throws IOException, BCompoundException {
		String PATH = "pragmas/filePragma/circle/includesExtendsImports/";
		String file = PATH + "Mch2.mch";
		Helpers.parseFile(file);
	}


	@Test(expected = BCompoundException.class)
	public void testFileCircle_imp() throws IOException, BCompoundException {
		String PATH = "pragmas/filePragma/circle/imp/";
		String file = PATH + "Mch1.imp";
		Helpers.parseFile(file);
	}

	@Test//(expected = BCompoundException.class)
	public void test_sensor() throws IOException, BCompoundException {
		String PATH = "pragmas/filePragma/circle/sensor/";
		String file = PATH + "Sensor0_i.imp";
		Helpers.parseFile(file);
	}


	@Test(expected = BCompoundException.class)
	public void testFileCircle_refines() throws IOException, BCompoundException {
		String PATH = "pragmas/filePragma/circle/includesExtendsImports/";
		String file = PATH + "Mch3.ref";
		Helpers.parseFile(file);
	}


	@Test(expected = BCompoundException.class)
	public void testFileCircle_self () throws IOException, BCompoundException {
		String PATH = "pragmas/filePragma/circle/self/";
		String file = PATH + "TheracBug.ref";
		Helpers.parseFile(file);
	}


	@Test(expected = BCompoundException.class)
	public void testFileCircle_notAllInvolved () throws IOException, BCompoundException, URISyntaxException {
		String file = "pragmas/filePragma/circle/notAllInvolved/Mch2.mch";
		Helpers.parseFile(file);
	}


	@Test(expected = BCompoundException.class)
	public void testInvalidPragmaFile() throws IOException, BCompoundException {
		String PATH = "pragmas/filePragma/";
		String file = PATH + "InvalidPragmaFile.mch";
		Helpers.parseFile(file);
	}

}
