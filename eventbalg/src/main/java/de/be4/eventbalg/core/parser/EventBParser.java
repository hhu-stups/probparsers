package de.be4.eventbalg.core.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import de.be4.eventbalg.core.parser.lexer.LexerException;
import de.be4.eventbalg.core.parser.node.Start;
import de.be4.eventbalg.core.parser.node.TComment;
import de.be4.eventbalg.core.parser.node.Token;
import de.be4.eventbalg.core.parser.parser.Parser;
import de.be4.eventbalg.core.parser.parser.ParserException;
import de.hhu.stups.sablecc.patch.IToken;
import de.hhu.stups.sablecc.patch.PositionedNode;

public class EventBParser {

	public static final String MSG_COMMENT_PLACEMENT = "Comment can only be place behind the element they belong to. Please move the comment to an appropriate place!";

	@Deprecated
	private de.hhu.stups.sablecc.patch.SourcePositions sourcePositions;

	/**
	 * Parses the input file.
	 * 
	 * @see #parse(String, boolean)
	 * @param machine the machine file
	 * @param verbose print debug information
	 * @return the generated AST
	 * @throws IOException if stream cannot be written to or closed
	 * @throws BException if parsing fails
	 */
	public Start parseFile(final File machine, final boolean verbose) throws IOException, BException {
		final InputStreamReader inputStreamReader
			= new InputStreamReader(new FileInputStream(machine), StandardCharsets.UTF_8);

		final StringBuilder builder = new StringBuilder();
		final char[] buffer = new char[1024];
		int read;
		while ((read = inputStreamReader.read(buffer)) >= 0) {
			builder.append(String.valueOf(buffer, 0, read));
		}
		inputStreamReader.close();

		return parse(builder.toString(), verbose);
	}

	public Start parseFile(File machine) throws IOException, BException {
		return this.parseFile(machine, false);
	}

	/**
	 * Parses the input string.
	 * 
	 * @param input
	 *            the {@link String} to be parsed
	 * @param debugOutput
	 *            output debug messages on standard out?
	 * @return the root node of the AST
	 * @throws BException
	 *             The {@link BException} class stores the actual exception as
	 *             delegate and forwards all method calls to it. So it is save
	 *             for tools to just use this exception if they want to extract
	 *             an error message. If the tools needs to extract additional
	 *             information, such as a sourcecode position or involved tokens
	 *             respectively nodes, it needs to retrieve the delegate
	 *             exception. The {@link BException} class offers a
	 *             {@link BException#getCause()} method for this, which returns
	 *             the delegate exception.
	 *             <p>
	 *             Internal exceptions:
	 *             <ul>
	 *             <li>{@link EventBLexerException}:
	 *             Thrown if any error occurs in the generated or customized lexer.
	 *             Unlike SableCC's standard {@link LexerException},
	 *             our own exception provides the source code position
	 *             of the last characters that were read from the input.</li>
	 *             <li>{@link EventBParseException}: This exception is thrown in
	 *             two situations. On the one hand if the parser throws a
	 *             {@link ParserException} we convert it into a
	 *             {@link EventBParseException}. On the other hand it can be
	 *             thrown if any error is found during the AST transformations
	 *             after the parser has finished. We try to provide a token if a
	 *             single token is involved in the error.</li>
	 *             </ul>
	 */
	@SuppressWarnings("deprecation")
	public Start parse(final String input, final boolean debugOutput) throws BException {
		final Reader reader = new StringReader(input);

		try {
			/*
			 * Main parser
			 */
			final EventBLexer lexer = new EventBLexer(new PushbackReader(reader, 99));
			lexer.setDebugOutput(debugOutput);

			Parser parser = new Parser(lexer);
			final Start rootNode = parser.parse();
			final List<IToken> tokenList = lexer.getTokenList();

			final Map<PositionedNode, de.hhu.stups.sablecc.patch.SourcecodeRange> positions = parser.getMapping();

			sourcePositions = new de.hhu.stups.sablecc.patch.SourcePositions(tokenList, positions);

			return rootNode;
		} catch (final LexerException e) {
			throw new BException(e);
		} catch (final ParserException e) {
			throw new BException(createEventBParseException(e));
		} catch (final EventBParseException e) {
			throw new BException(e);
		} catch (final IOException e) {
			// shouldn't happen and if, we cannot handle it
			throw new BException(e);
		}
	}

	public Start parse(String input) throws BException {
		return this.parse(input, false);
	}

	private EventBParseException createEventBParseException(final ParserException e) {
		final Token token = e.getToken();
		String message = e.getMessage();
		final boolean expectingFound = message.indexOf("expecting") >= 0;

		/*
		 * Special error message for misplaced comments.
		 */
		if (expectingFound && token instanceof TComment) {
			message = MSG_COMMENT_PLACEMENT;
		}

		/*
		 * Replace some token names...
		 */
		message = message.replaceFirst(" at", " @");

		return new EventBParseException(token, message);
	}

	/**
	 * @deprecated Please use the {@link PositionedNode} methods to get position information instead. All SableCC-generated nodes and tokens extend this class.
	 */
	@Deprecated
	public de.hhu.stups.sablecc.patch.SourcePositions getSourcePositions() {
		return sourcePositions;
	}
}
