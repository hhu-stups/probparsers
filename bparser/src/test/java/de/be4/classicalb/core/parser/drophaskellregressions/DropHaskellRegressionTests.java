package de.be4.classicalb.core.parser.drophaskellregressions;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.PreParseException;
import de.be4.classicalb.core.parser.node.Start;

public class DropHaskellRegressionTests {

	@Test
	public void test() throws BCompoundException, IOException, PreParseException, URISyntaxException {
		String name = "parsable/InfiniteParityFunction.mch";
		BParser parser = new BParser(name);
		parser.parseFile(new File(this.getClass().getClassLoader().getResource(name).toURI()), false);

		String code = "not(finite({x|x>2}))";
		BParser parser2 = new BParser(name);
		
		parser2.getDefinitions().addDefinitions(parser.getDefinitions());
		Start parse = parser2.parseFormula(code);
		assertNotNull(parse);

	}

}
