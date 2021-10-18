package de.be4.classicalb.core.parser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;

import org.junit.Test;

public class ExploreSourcePositionTest {

	@Test
	public void test() throws BCompoundException, IOException, URISyntaxException {

		final BParser parser = new BParser("m");
		Start parse = parser.parseFile(new File(this.getClass().getClassLoader().getResource("LabelTest.mch").toURI()), false);
	}

}
