/*
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.prob.parser;

import de.prob.core.sablecc.node.*;
import de.prob.prolog.term.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This generator extracts prolog terms from a SableCC syntax tree.
 */
public final class PrologTermGenerator {

	public static PrologTerm toPrologTerm(PResult topnode) {
		PrologTerm term;
		if (topnode instanceof AYesResult) {
			term = toPrologTerm(((AYesResult) topnode).getTerm());
		} else if (topnode instanceof ANoResult) {
			term = null;
		} else if (topnode instanceof AInterruptedResult) {
			term = null;
		} else if (topnode instanceof AExceptionResult) {
			String message = "ProB raised an exception: " + ((AExceptionResult) topnode).getString().getText();
			throw new ResultParserException(message);
		} else if (topnode instanceof AProgressResult) {
			term = toPrologTerm(((AProgressResult) topnode).getTerm());
		} else if (topnode instanceof ACallBackResult) {
			term = toPrologTerm(((ACallBackResult) topnode).getTerm());
		} else {
			throw new IllegalStateException("Unknown subclass of PResult: " + topnode.getClass().getCanonicalName());
		}
		return term;
	}

	public static PrologTerm toPrologTerm(Start node) {
		return toPrologTerm(node.getPResult());
	}

	public static PrologTerm toPrologTermMustNotFail(final String query, final Start node) {
		PResult topnode = node.getPResult();
		if (topnode instanceof ACallBackResult || topnode instanceof AProgressResult) {
			throw new ResultParserException("Prolog query returned a callback/progress result, which isn't supported here: " + query);
		} else if (!(topnode instanceof AYesResult)) {
			final String message = "Prolog query unexpectedly failed: " + query;
			throw new ResultParserException(message);
		}
		return toPrologTerm(((AYesResult) topnode).getTerm());
	}

	public static PrologTerm toPrologTerm(final PTerm node) {
		PrologTerm term;
		if (node instanceof ANumberTerm) {
			PNumber number = ((ANumberTerm) node).getNumber();
			if (number instanceof AIntegerNumber) {
				AIntegerNumber intNumber = (AIntegerNumber) number;
				String text = intNumber.getInteger().getText();

				if (text.startsWith("0'")) {
					char c = text.charAt(2);
					int cp;
					if (c == '\\') {
						// TODO: remove need for StringBuilder allocation
						StringBuilder b = new StringBuilder(1);
						unescapeCharacter(b, text, 3);
						cp = b.codePointAt(0);
					} else {
						cp = c;
					}

					TSign sign = intNumber.getSign();
					if (sign != null) {
						String signText = sign.getText();
						if ("-".equals(signText)) {
							cp = -cp;
						}
					}

					term = AIntegerPrologTerm.create(cp);
				} else {
					int radix;
					if (text.startsWith("0b")) {
						radix = 2;
						text = text.substring(2);
					} else if (text.startsWith("0o")) {
						radix = 8;
						text = text.substring(2);
					} else if (text.startsWith("0x")) {
						radix = 16;
						text = text.substring(2);
					} else {
						radix = 10;
					}

					TSign sign = intNumber.getSign();
					if (sign != null) {
						String signText = sign.getText();
						if ("-".equals(signText)) {
							text = signText + text;
						}
					}

					term = AIntegerPrologTerm.create(text, radix);
				}
			} else if (number instanceof AFloatNumber) {
				AFloatNumber floatNumber = (AFloatNumber) number;
				String text = floatNumber.getFloat().getText();
				double d = Double.parseDouble(text);

				TSign sign = floatNumber.getSign();
				if (sign != null) {
					String signText = sign.getText();
					if ("-".equals(signText)) {
						d = -d;
					}
				}

				term = new FloatPrologTerm(d);
			} else {
				throw new IllegalStateException("Unexpected subclass of PNumber: " + number.getClass().getCanonicalName());
			}
		} else if (node instanceof AVariableTerm) {
			term = new VariablePrologTerm(((AVariableTerm) node).getVariable().getText());
		} else if (node instanceof AAtomTerm) {
			String text = removeQuotes(((AAtomTerm) node).getName().getText());
			if ("[]".equals(text)) {
				term = ListPrologTerm.emptyList();
			} else {
				term = new CompoundPrologTerm(text);
			}
		} else if (node instanceof AListTerm) {
			List<PrologTerm> args = extractArgs(((AListTerm) node).getParams());
			term = ListPrologTerm.fromCollection(args);
		} else if (node instanceof ACompoundTerm) {
			ACompoundTerm acompound = (ACompoundTerm) node;
			// TODO: optimize list concatenation with '.' functor
			String functor = removeQuotes(acompound.getFunctor().getText());
			List<PrologTerm> args = extractArgs(acompound.getParams());
			term = transformToList(CompoundPrologTerm.fromCollection(functor, args));
		} else {
			throw new IllegalStateException("Unexpected subclass of PTerm: " + node.getClass().getCanonicalName());
		}

		return term;
	}

	private static PrologTerm transformToList(CompoundPrologTerm term) {
		if ((!".".equals(term.getFunctor()) && !"[|]".equals(term.getFunctor())) || term.getArity() != 2) {
			return term;
		}

		PrologTerm tail = term.getArgument(2);
		if (tail.isList()) {
			ListPrologTerm ltail = (ListPrologTerm) tail;
			List<PrologTerm> list = new ArrayList<>(ltail.size() + 1);
			list.add(term.getArgument(1));
			list.addAll(ltail);
			return ListPrologTerm.fromCollection(list);
		}

		return term;
	}

	private static List<PrologTerm> extractArgs(PParams params) {
		AParams aparams = (AParams) params;
		List<PrologTerm> terms = new ArrayList<>();
		terms.add(toPrologTerm(aparams.getTerm()));

		PMoreParams more = aparams.getMoreParams();
		while (more instanceof AMoreParams) {
			AMoreParams amore = (AMoreParams) more;
			terms.add(toPrologTerm(amore.getTerm()));
			more = amore.getMoreParams();
		}

		return terms;
	}

	private static String removeQuotes(String text) {
		if (text != null && !text.isEmpty()) {
			char first = text.charAt(0);
			if (first == '\'' || first == '"') {
				int len = text.length();
				StringBuilder b = new StringBuilder(len - 2);
				for (int i = 1; i < len - 1; i++) {
					char c = text.charAt(i);
					if (c == '\\') {
						i = unescapeCharacter(b, text, i + 1);
					} else if (c == first) {
						i++; // assume the next char is also equal to 'first'
						b.append(c);
					} else {
						b.append(c);
					}
				}
				return b.toString();
			}
		}

		return text;
	}

	private static int unescapeCharacter(StringBuilder b, String text, int i) {
		char c = text.charAt(i);
		switch (c) {
			case 'a':
				b.append('\u0007');
				break;
			case 'b':
				b.append('\b');
				break;
			case 't':
				b.append('\t');
				break;
			case 'n':
				b.append('\n');
				break;
			case 'v':
				b.append('\u000B');
				break;
			case 'f':
				b.append('\f');
				break;
			case 'r':
				b.append('\r');
				break;
			case 'e':
				b.append('\u001B');
				break;
			case 'd':
				b.append('\u007F');
				break;
			case '\n':
				// ignore escaped newline
				break;
			case 'x':
				i = getChar(b, text, i + 1, 16);
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
				i = getChar(b, text, i, 8);
				break;
			default:
				// all the other escape sequences just resolve to the 2nd char
				b.append(c);
				break;
		}

		return i;
	}

	private static int getChar(StringBuilder b, String text, int i, int radix) {
		int end = text.indexOf('\\', i);
		int value = Integer.parseInt(text.substring(i, end), radix);
		if (Character.isBmpCodePoint(value)) {
			b.append((char) value);
		} else {
			b.appendCodePoint(value);
		}

		return end;
	}
}
