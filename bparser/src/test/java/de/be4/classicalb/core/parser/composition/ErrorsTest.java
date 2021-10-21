package de.be4.classicalb.core.parser.composition;

import de.be4.classicalb.core.parser.exceptions.CheckException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;

public class ErrorsTest {

	@Test
	public void testMachineNotFound() {
		String PATH = "composition/errors/";
		String file = PATH + "MachineNotFound.mch";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.fullParsing(file));
		assertEquals("Machine not found: 'MachineDoesNotExist' in 'MachineNotFound.mch'", e.getMessage());
	}
	
	@Test
	public void testMachineNameDoesNotMachtFileName() {
		String PATH = "composition/errors/";
		String file = PATH + "MachineNameDoesNotMatchFileName.mch";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.fullParsing(file));
		assertEquals("Machine name does not match the file name: 'Foo' vs 'MachineNameDoesNotMatchFileName'", e.getMessage());
	}

}
