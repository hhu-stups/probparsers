package de.be4.classicalb.core.parser.analysis.prolog;

import java.io.IOException;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import org.junit.Test;

import util.Helpers;

public class AtelierBCompatibilityTest {

	@Test
	public void testSysExtension() throws IOException, BCompoundException {
		String PATH = "atelierb/sys_extension/";
		String file = PATH + "main.sys";
		Helpers.parseFile(file);
	}
}
