package de.be4.classicalb.core.parser.util;

import java.io.StringWriter;

import de.be4.classicalb.core.parser.node.Node;

public final class PrettyPrinter extends BasePrettyPrinter {

	public PrettyPrinter() {
		super(new StringWriter());
	}

	public String getPrettyPrint() {
		this.flush();
		return this.getWriter().toString();
	}

	public static String getCompactPrettyPrint(Node node) {
		PrettyPrinter pp = new PrettyPrinter();
		node.apply(pp);
		return pp.getPrettyPrint();
	}

	public static String getPrettyPrint(Node node) {
		PrettyPrinter pp = new PrettyPrinter();
		pp.setUseIndentation(true);
		node.apply(pp);
		return pp.getPrettyPrint();
	}
}
