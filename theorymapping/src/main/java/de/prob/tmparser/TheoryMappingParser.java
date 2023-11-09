package de.prob.tmparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import de.prob.core.theorymapping.lexer.Lexer;
import de.prob.core.theorymapping.lexer.LexerException;
import de.prob.core.theorymapping.node.Start;
import de.prob.core.theorymapping.parser.Parser;
import de.prob.core.theorymapping.parser.ParserException;
import de.prob.tmparser.internal.MappingVisitor;

public class TheoryMappingParser {
	public static Collection<OperatorMapping> parseTheoryMapping(
			String theoryName, String filename) throws IOException {
		return parseTheoryMapping(theoryName, Paths.get(filename));
	}

	public static Collection<OperatorMapping> parseTheoryMapping(String theoryName, Path path) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path)) {
			return parseTheoryMapping(theoryName, reader);
		}
	}

	public static Collection<OperatorMapping> parseTheoryMapping(
			String theoryName, Reader input) throws IOException {
		Start ast;
		try {
			ast = parse(input);
		} catch (ParserException | LexerException e) {
			throw new TheoryMappingException(e);
		}
        return extractMappings(ast, theoryName);
	}

	private static Start parse(Reader input) throws ParserException,
			LexerException, IOException {
		final Lexer lexer = new Lexer(new PushbackReader(input));
		final Parser parser = new Parser(lexer);
		return parser.parse();
	}

	private static Collection<OperatorMapping> extractMappings(Start ast,
			String theoryName) {
		MappingVisitor visitor = new MappingVisitor(theoryName);
		ast.apply(visitor);
		return visitor.getMappings();
	}

}
