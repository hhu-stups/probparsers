package de.be4.classicalb.core.parser.prios;

import static de.be4.classicalb.core.parser.prios.EAssoc.LEFT;
import static de.be4.classicalb.core.parser.prios.EAssoc.RIGHT;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class BinaryOperator {
	static final List<BinaryOperator> OPS = Collections.unmodifiableList(Arrays.asList(
		new BinaryOperator("*", 190, LEFT, "multiplication or Cartesian product"),
		new BinaryOperator("**", 200, RIGHT, "power"),
		new BinaryOperator("+", 180, LEFT, "addition"),
		new BinaryOperator("+->", 125, LEFT, "partial function"),
		new BinaryOperator("+->>", 125, LEFT, "partial surjection"),
		new BinaryOperator("-", 180, LEFT, "subtraction"),
		new BinaryOperator("-->", 125, LEFT, "total function"),
		new BinaryOperator("-->>", 125, LEFT, "total surjection"),
		new BinaryOperator("->", 160, LEFT, "insertion at front"),
		new BinaryOperator("..", 170, LEFT, "interval"),
		new BinaryOperator("/", 190, LEFT, "division"),
		new BinaryOperator("/\\", 160, LEFT, "intersection"),
		new BinaryOperator("/|\\", 160, LEFT, "restriction at front"),
		new BinaryOperator(";", 20, LEFT, "composition"),
		new BinaryOperator("<+", 160, LEFT, "function override"),
		new BinaryOperator("<->", 125, LEFT, "relations"),
		new BinaryOperator("<-", 160, LEFT, "insertion at end"),
		new BinaryOperator("<<|", 160, LEFT, "domain subtraction"),
		new BinaryOperator("<|", 160, LEFT, "domain restriction"),
		new BinaryOperator(">+>", 125, LEFT, "partial injection"),
		new BinaryOperator(">->", 125, LEFT, "total injection"),
		new BinaryOperator(">->>", 125, LEFT, "total bijection"),
		new BinaryOperator("><", 160, LEFT, "direct relational product"),
		new BinaryOperator("\\/", 160, LEFT, "union"),
		new BinaryOperator("\\|/", 160, LEFT, "restriction of sequence"),
		new BinaryOperator("^", 160, LEFT, "concatenation"),
		new BinaryOperator("mod", 190, LEFT, "modulo"),
		new BinaryOperator("|->", 160, LEFT, "maplet"),
		new BinaryOperator("|>", 160, LEFT, "range restriction"),
		new BinaryOperator("|>>", 160, LEFT, "range subtraction"),
		new BinaryOperator("||", 20, LEFT, "relational parallel product")
	));

	private final String symbol;
	private final int priority;
	private final EAssoc associatifity;
	private final String name;

	public BinaryOperator(final String symbol, final int priority, final EAssoc associatifity, final String name) {
		this.symbol = symbol;
		this.priority = priority;
		this.associatifity = associatifity;
		this.name = name;
	}

	public String getSymbol() {
		return symbol;
	}

	public int getPriority() {
		return priority;
	}

	public EAssoc getAssociativity() {
		return associatifity;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return symbol + "(" + priority + ", " + associatifity + ", \"" + name + "\")";
	}

}
