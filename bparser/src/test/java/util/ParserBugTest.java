package util;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.node.Start;

import org.junit.Test;

public class ParserBugTest {
	String s = "MACHINE ParserDefBug\n DEFINITIONS\n D(y) == f\n "
			+ "CONSTANTS f,g\n	PROPERTIES\n f = pred &\n	"
			+ "             g = %x.(x:1..10 | (D(\"hello\")(x)))\n	"
			+ "ASSERTIONS\n	g(1)=0\n END";

	@Test
	public void test() throws Exception {
		BParser parser = new BParser();
		Start ast = parser.parse(s, true);
		// parser does not raise an exception, everything is fine
	}

}
