package de.prob.unicode;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.prob.unicode.UnicodeTranslator;

public class EmptyFormulas {
	@Test
	public void EmptyToAscii() {
		assertTrue(UnicodeTranslator.toAscii("").isEmpty());
	}

	@Test
	public void EmptyToUnicode() {
		assertTrue(UnicodeTranslator.toUnicode("").isEmpty());
	}

}
