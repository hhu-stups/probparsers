package de.be4.classicalb.core.parser.exceptions;

import org.junit.Assert;
import org.junit.Test;

public final class LocationTest {
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
}
