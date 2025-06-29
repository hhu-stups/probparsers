package de.be4.classicalb.core.parser.definitions;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.IDefinitionFileProvider;
import de.be4.classicalb.core.parser.IDefinitions;
import de.be4.classicalb.core.parser.IFileContentProvider;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.PreParseException;
import de.be4.classicalb.core.parser.node.AExpressionDefinitionDefinition;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.APredicateDefinitionDefinition;
import de.be4.classicalb.core.parser.node.PPredicate;
import de.be4.classicalb.core.parser.util.Utils;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class DefinitionFilesTest {
	private static final class TestFileContentProvider implements IFileContentProvider {
		private static final Map<String, String> defFileContents = new HashMap<>();

		static {
			defFileContents.put("DefFile", "DEFINITIONS def2 == yy; def3 == zz");
			defFileContents.put("DefFile1", "DEFINITIONS \"DefFile2\"; def3 == bb");
			defFileContents.put("DefFile2", "DEFINITIONS def2 == yy; def4 == zz");
			defFileContents.put("DefFile3", "DEFINITIONS \"DefFile4\"");
			defFileContents.put("DefFile4", "DEFINITIONS \"DefFile3\"");
			defFileContents.put("DefFile5", "DEFINITIONS \"DefFile6\"");
			defFileContents.put("DefFile6", "DEFINITIONS def == 5");
		}

		@Override
		public String getFileContent(File directory, String fileName) throws IOException {
			return defFileContents.get(fileName);
		}

		@Override
		public File getFile(File directory, String fileName) throws IOException {
			return null;
		}
	}

	@Test
	public void testOneDefinitionFile() throws BCompoundException {
		final String testMachine = "MACHINE Test\nDEFINITIONS \"DefFile\"; def1 == xx\nINVARIANT def2 = def3\nEND";
		final BParser parser = new BParser("testcase");
		parser.setContentProvider(new TestFileContentProvider());
		parser.parseMachine(testMachine);

		final IDefinitions definitions = parser.getDefinitions();
		final AExpressionDefinitionDefinition def1 = (AExpressionDefinitionDefinition) definitions
				.getDefinition("def1");
		assertEquals("def1", def1.getName().getText());
		assertEquals(0, def1.getParameters().size());
		assertTrue(def1.getRhs() instanceof AIdentifierExpression);

		final AExpressionDefinitionDefinition def2 = (AExpressionDefinitionDefinition) definitions
				.getDefinition("def2");
		assertEquals("def2", def2.getName().getText());
		assertEquals(0, def2.getParameters().size());
		assertTrue(def2.getRhs() instanceof AIdentifierExpression);
	}

	// TODO test two files

	/*
	 * test recursive references from def file to def file
	 */
	@Test
	public void testRecursiveReference() throws Exception {
		final String testMachine = "MACHINE Test\nDEFINITIONS \"DefFile1\"; def1 == xx; def02 == aa\nEND";
		final BParser parser = new BParser("testcase");
		parser.setContentProvider(new TestFileContentProvider());
		parser.parseMachine(testMachine);

		final IDefinitions definitions = parser.getDefinitions();
		final AExpressionDefinitionDefinition def1 = (AExpressionDefinitionDefinition) definitions
				.getDefinition("def1");
		assertEquals("def1", def1.getName().getText());
		assertEquals(0, def1.getParameters().size());
		assertTrue(def1.getRhs() instanceof AIdentifierExpression);
		String ident = Utils
				.getTIdentifierListAsString(((AIdentifierExpression) def1.getRhs())
						.getIdentifier());
		assertEquals("xx", ident);

		final AExpressionDefinitionDefinition def2 = (AExpressionDefinitionDefinition) definitions
				.getDefinition("def2");
		assertEquals("def2", def2.getName().getText());
		assertEquals(0, def2.getParameters().size());
		assertTrue(def2.getRhs() instanceof AIdentifierExpression);
		ident = Utils.getTIdentifierListAsString(((AIdentifierExpression) def2
				.getRhs()).getIdentifier());

		assertEquals("yy", ident);

		final AExpressionDefinitionDefinition def3 = (AExpressionDefinitionDefinition) definitions
				.getDefinition("def3");
		assertEquals("def3", def3.getName().getText());
		assertEquals(0, def3.getParameters().size());
		assertTrue(def3.getRhs() instanceof AIdentifierExpression);
		ident = Utils.getTIdentifierListAsString(((AIdentifierExpression) def3
				.getRhs()).getIdentifier());
		// definition in outer def file should overwrite the one in referenced
		// def file
		assertEquals("bb", ident);
	}

	/*
	 * test circles references between def files
	 */
	@Test
	public void testCircleReference() {
		final String testMachine = "MACHINE Test\nDEFINITIONS \"DefFile3\"\nEND";
		final BParser parser = new BParser("testcase");
		parser.setContentProvider(new TestFileContentProvider());
		Helpers.assertThrowsCompound(PreParseException.class, () -> parser.parseMachine(testMachine));
	}

	/*
	 * test circles references between def files
	 */
	@Test
	public void testNonCircleReference() throws Exception {
		final String testMachine = "MACHINE Test\nDEFINITIONS \"DefFile5\";\n\"DefFile6\"\nEND";
		final BParser parser = new BParser("testcase");
		parser.setContentProvider(new TestFileContentProvider());
		parser.parseMachine(testMachine);
	}

	/*
	 * test with real files
	 */
	@Test
	public void testRealFiles() throws Exception {
		final BParser parser = new BParser("testcase");
		File machine = new File(
				this.getClass().getResource("/parsable/DefinitionFileTest.mch").toURI());
		parser.parseFile(machine);

		final IDefinitions definitions = parser.getDefinitions();
		final APredicateDefinitionDefinition def1 = (APredicateDefinitionDefinition) definitions
				.getDefinition("GRD2");
		assertEquals("GRD2", def1.getName().getText());
		assertEquals(0, def1.getParameters().size());
		assertTrue(def1.getRhs() instanceof PPredicate);

		final APredicateDefinitionDefinition def2 = (APredicateDefinitionDefinition) definitions
				.getDefinition("GRD1");
		assertEquals("GRD1", def2.getName().getText());
		assertEquals(0, def2.getParameters().size());
		assertTrue(def2.getRhs() instanceof PPredicate);
	}

	@Test
	public void testNotExistingFile() {
		final String testMachine = "MACHINE Test\nDEFINITIONS \"DefFile\"; def1 == xx\nEND";
		assertThrows(BCompoundException.class, () ->
			new BParser("testcase").parseMachine(testMachine)
		);
	}

	@Test
	public void testDefCaching() throws Exception {
		final String testMachine = "MACHINE Test\nDEFINITIONS \"DefFile1\"; \"DefFile2\"\nEND";
		final BParser parser = new BParser("testcase");
		final CountingDefinitionFileProvider provider = new CountingDefinitionFileProvider();
		parser.setContentProvider(provider);
		parser.parseMachine(testMachine);

		assertEquals(4, provider.getStoredCounter);
		assertEquals(2, provider.storeCounter);
		assertEquals(2, provider.getContentCounter);
	}

	@Test
	public void testErrorInDefinitions() throws IOException, BCompoundException {
		String file = "./definitions/errors/DefinitionErrorPosition.mch";
		// file contains DEFINITIONS aa == 1 + + END
		final BCompoundException e = assertThrows(BCompoundException.class, () -> Helpers.parseFile(file));
		Helpers.assertParseErrorLocation(e, 2, 23, 2, 23);
		// now contains Invalid combination of symbols: PLUS PLUS is not allowed. '
	}

	@Test
	public void testErrorInIncludedDefinitionFile() throws IOException,
			BCompoundException {
		String file = "./definitions/errors/MachineWithErrorInIncludedDefinitionFile.mch";
		final BCompoundException e = assertThrows(BCompoundException.class, () -> Helpers.parseFile(file));
		Helpers.assertParseErrorLocation(e, 3, 1, 3, 1);
		// now contains Invalid combination of symbols: PLUS OF is not allowed. '
	}

	@Test
	public void testLexerErrorInIncludedDefinitionFile() {
		String file = "definitions/errors/LexerErrorInIncludedDefinitionFile.mch";
		final BCompoundException e = assertThrows(BCompoundException.class, () -> Helpers.parseFile(file));
		assertEquals("Unknown token: | in DEFINITIONS clause", e.getMessage());
		Helpers.assertParseErrorLocation(e, 4, 67, 4, 67);
	}

	static class CountingDefinitionFileProvider implements IDefinitionFileProvider {
		int getStoredCounter = 0;
		int storeCounter = 0;
		int getContentCounter = 0;
		private final Map<String, IDefinitions> store = new HashMap<>();

		@Override
		public IDefinitions getDefinitions(final String fileName) {
			getStoredCounter++;
			return store.get(fileName);
		}

		@Override
		public void storeDefinition(final String fileName,
				final IDefinitions definitions) {
			storeCounter++;
			store.put(fileName, definitions);
		}

		@Override
		public String getFileContent(File directory, String fileName)
				throws IOException {
			getContentCounter++;
			if ("DefFile1".equals(fileName)) {
				return "DEFINITIONS \"DefFile2\"; def1 == 1";
			} else if ("DefFile2".equals(fileName)) {
				return "DEFINITIONS def2 == 2";
			} else {
				return "";
			}
		}

		@Override
		public File getFile(File directory, String fileName) throws IOException {
			return null;
		}
	}
}
