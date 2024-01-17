package de.be4.classicalb.core.parser.util;

import java.io.StringWriter;

public final class PrettyPrinter extends BasePrettyPrinter {

	public PrettyPrinter() {
		super(new StringWriter());
	}

	public String getPrettyPrint() {
		this.flush();
		return this.getWriter().toString();
	}
}
