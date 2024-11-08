package de.be4.classicalb.core.parser.exceptions;

import de.be4.classicalb.core.parser.node.ASkipSubstitution;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.be4.classicalb.core.parser.node.Token;
import de.hhu.stups.sablecc.patch.SourcePosition;

import org.junit.Assert;
import org.junit.Test;

public final class LocationTest {
	@SuppressWarnings({"ConstantValue", "EqualsWithItself"})
	@Test
	public void equalsHashCode() {
		BException.Location loc1a = new BException.Location("unittest.mch", 1, 2, 3, 4);
		BException.Location loc1b = new BException.Location("unittest.mch", 1, 2, 3, 4);
		BException.Location loc2a = new BException.Location(null, 1, 2, 3, 4);
		BException.Location loc2b = new BException.Location(null, 1, 2, 3, 4);
		BException.Location loc3 = new BException.Location(null, 9, 2, 3, 4);
		BException.Location loc4 = new BException.Location(null, 1, 9, 3, 4);
		BException.Location loc5 = new BException.Location(null, 1, 2, 9, 4);
		BException.Location loc6 = new BException.Location(null, 1, 2, 3, 9);
		
		Assert.assertFalse(loc1a.equals(null));
		Assert.assertTrue(loc1a.equals(loc1a));
		Assert.assertEquals(loc1a.hashCode(), loc1a.hashCode());
		Assert.assertTrue(loc1a.equals(loc1b));
		Assert.assertEquals(loc1a.hashCode(), loc1b.hashCode());
		
		Assert.assertFalse(loc2a.equals(null));
		Assert.assertTrue(loc2a.equals(loc2a));
		Assert.assertEquals(loc2a.hashCode(), loc2a.hashCode());
		Assert.assertTrue(loc2a.equals(loc2b));
		Assert.assertEquals(loc2a.hashCode(), loc2b.hashCode());
		
		// Do not check non-equality of hashCode (technically, they're not strictly required to be different)
		Assert.assertFalse(loc1a.equals(loc2a));
		Assert.assertFalse(loc1a.equals(loc3));
		Assert.assertFalse(loc2a.equals(loc3));
		Assert.assertFalse(loc1a.equals(loc4));
		Assert.assertFalse(loc2a.equals(loc4));
		Assert.assertFalse(loc1a.equals(loc5));
		Assert.assertFalse(loc2a.equals(loc5));
		Assert.assertFalse(loc1a.equals(loc6));
		Assert.assertFalse(loc2a.equals(loc6));
	}
	
	@Test
	public void toStringNoSpan() {
		BException.Location location = new BException.Location("unittest.mch", 12, 34, 12, 34);
		Assert.assertEquals("unittest.mch:12:34", location.toString());
	}
	
	@Test
	public void toStringSpanOnSameLine() {
		BException.Location location = new BException.Location("unittest.mch", 12, 34, 12, 50);
		Assert.assertEquals("unittest.mch:12:34 to 12:50", location.toString());
	}
	
	@Test
	public void toStringSpanAcrossLines() {
		BException.Location location = new BException.Location("unittest.mch", 12, 34, 17, 22);
		Assert.assertEquals("unittest.mch:12:34 to 17:22", location.toString());
	}
	
	@Test
	public void toStringSpanAcrossLinesSameColumn() {
		BException.Location location = new BException.Location("unittest.mch", 12, 34, 17, 34);
		Assert.assertEquals("unittest.mch:12:34 to 17:34", location.toString());
	}
	
	@Test
	public void toStringNullFilenameNoSpan() {
		BException.Location location = new BException.Location(null, 12, 34, 12, 34);
		Assert.assertNull(location.getFilename());
		Assert.assertEquals("null:12:34", location.toString());
	}
	
	@Test
	public void toStringNullFilenameSpan() {
		BException.Location location = new BException.Location(null, 12, 34, 12, 50);
		Assert.assertNull(location.getFilename());
		Assert.assertEquals("null:12:34 to 12:50", location.toString());
	}
	
	@Test
	public void fromNodeWithoutPositions() {
		Node node = new ASkipSubstitution();
		Assert.assertNull(BException.Location.fromNode("unittest.mch", node));
	}
	
	@Test
	public void fromNodeWithOnlyStartPosition() {
		// This case should really never happen, but we implement it, so let's test it...
		Node node = new ASkipSubstitution();
		node.setStartPos(new SourcePosition(12, 34));
		Assert.assertEquals(
			new BException.Location("unittest.mch", 12, 34, 12, 34),
			BException.Location.fromNode("unittest.mch", node)
		);
	}
	
	@Test
	public void fromNodeWithPositions() {
		Node node = new ASkipSubstitution();
		node.setStartPos(new SourcePosition(12, 34));
		node.setEndPos(new SourcePosition(17, 22));
		Assert.assertEquals(
			new BException.Location("unittest.mch", 12, 34, 17, 22),
			BException.Location.fromNode("unittest.mch", node)
		);
	}
	
	@Test
	public void fromTokenWithPositions() {
		Token token = new TIdentifierLiteral("identifier", 12, 34);
		Assert.assertEquals(
			new BException.Location("unittest.mch", 12, 34, 12, 44),
			BException.Location.fromNode("unittest.mch", token)
		);
	}
}
