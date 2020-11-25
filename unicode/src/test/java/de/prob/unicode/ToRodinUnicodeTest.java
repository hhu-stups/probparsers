package de.prob.unicode;

import org.junit.Assert;
import org.junit.Test;

public final class ToRodinUnicodeTest {
	@Test
	public void regularAscii() {
		Assert.assertEquals("a ∧ b", UnicodeTranslator.toRodinUnicode("a & b"));
	}
	
	@Test
	public void regularUnicode() {
		Assert.assertEquals("a ∧ b", UnicodeTranslator.toRodinUnicode("a ∧ b"));
	}
	
	@Test
	public void inverseAscii() {
		Assert.assertEquals("f∼", UnicodeTranslator.toRodinUnicode("f~"));
	}
	
	@Test
	public void inverseUnicodeTilde() {
		Assert.assertEquals("f∼", UnicodeTranslator.toRodinUnicode("f∼"));
	}
	
	@Test
	public void inverseUnicodePowMinus1() {
		Assert.assertEquals("f∼", UnicodeTranslator.toRodinUnicode("f⁻¹"));
	}
	
	@Test
	public void typeCommentAfterAscii() {
		Assert.assertEquals("⊤ ", UnicodeTranslator.toRodinUnicode("true /* v : BOOL */"));
	}
	
	@Test
	public void typeCommentAfterUnicode() {
		Assert.assertEquals("⊤ ", UnicodeTranslator.toRodinUnicode("⊤ /* v ∈ BOOL */"));
	}
	
	@Test
	public void typeCommentBeforeAscii() {
		Assert.assertEquals(" ⊤", UnicodeTranslator.toRodinUnicode("/* v : BOOL */ true"));
	}
	
	@Test
	public void typeCommentBeforeUnicode() {
		Assert.assertEquals(" ⊤", UnicodeTranslator.toRodinUnicode("/* v ∈ BOOL */ ⊤"));
	}
	
	@Test
	public void typeCommentBetweenAscii() {
		Assert.assertEquals("x y ∨ ⊤", UnicodeTranslator.toRodinUnicode("x/* dummy */y or true"));
	}
	
	@Test
	public void typeCommentBetweenUnicode() {
		Assert.assertEquals("x y∨⊤", UnicodeTranslator.toRodinUnicode("x/* dummy */y∨⊤"));
	}
}
