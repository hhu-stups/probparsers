package de.be4.classicalb.core.parser.composition;

import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.exceptions.CheckException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;

public class ErrorsTest {

	@Test
	public void testMachineNotFound() {
		String PATH = "composition/errors/";
		String file = PATH + "MachineNotFound.mch";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.parseFile(file));
		assertEquals("Machine not found: 'MachineDoesNotExist' in 'MachineNotFound.mch'", e.getMessage());
	}
	
	@Test
	public void testMachineNameDoesNotMachtFileName() {
		String PATH = "composition/errors/";
		String file = PATH + "MachineNameDoesNotMatchFileName.mch";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.parseFile(file));
		assertEquals("Machine name does not match the file name: 'Foo' vs 'MachineNameDoesNotMatchFileName'", e.getMessage());
	}

	@Test
	public void testCyclicRefinementWithMismatchedFileName() {
		String file = "composition/errors/NFA_det.ref";
		ParsingBehaviour parsingBehaviour = new ParsingBehaviour();
		// machineNameMustMatchFileName = false only disables the check for the main machine.
		// The names of referenced machines are always checked.
		// Test that the mismatch is detected when the machine references itself in the REFINES clause.
		parsingBehaviour.setMachineNameMustMatchFileName(false);
		CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.parseFile(file, parsingBehaviour));
		assertEquals("Machine name does not match the file name: 'NFA_det1' vs 'NFA_det'", e.getMessage());
	}
}
