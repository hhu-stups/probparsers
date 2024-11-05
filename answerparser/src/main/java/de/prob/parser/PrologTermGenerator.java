/*
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.prob.parser;

import de.prob.core.sablecc.node.*;
import de.prob.prolog.term.*;

import java.util.ArrayDeque;
import java.util.Deque;

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
			term = toPrologTerm(((AExceptionResult) topnode).getTerm());
			throw new ResultParserException("ProB raised an exception: " + term);
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

	public static PrologTerm toPrologTermMustNotFail(String query, Start node) {
		PResult topnode = node.getPResult();
		if (topnode instanceof ACallBackResult || topnode instanceof AProgressResult) {
			throw new ResultParserException("Prolog query returned a callback/progress result, which isn't supported here: " + query);
		} else if (!(topnode instanceof AYesResult)) {
			final String message = "Prolog query unexpectedly failed: " + query;
			throw new ResultParserException(message);
		}
		return toPrologTerm(((AYesResult) topnode).getTerm());
	}

	public static PrologTerm toPrologTerm(PTerm topNode) {
		abstract class TermBuilder {
			abstract PrologTerm build();

			abstract boolean isFinished();

			abstract void addArg(PrologTerm term);
		}

		class Finished extends TermBuilder {
			final PrologTerm term;

			Finished(PrologTerm term) {
				this.term = term;
			}

			@Override
			PrologTerm build() {
				return this.term;
			}

			@Override
			boolean isFinished() {
				return true;
			}

			@Override
			void addArg(PrologTerm term) {
				throw new IllegalStateException();
			}
		}

		class ListBuilder extends TermBuilder {
			final int arity;
			final Deque<PrologTerm> args;

			ListBuilder(int arity) {
				this.arity = arity;
				this.args = new ArrayDeque<>(arity);
			}

			@Override
			PrologTerm build() {
				return ListPrologTerm.fromCollection(this.args);
			}

			@Override
			boolean isFinished() {
				return this.args.size() >= this.arity;
			}

			@Override
			void addArg(PrologTerm term) {
				this.args.push(term);
			}
		}

		class CompoundBuilder extends TermBuilder {
			final String functor;
			final int arity;
			final Deque<PrologTerm> args;

			CompoundBuilder(String functor, int arity) {
				this.functor = functor;
				this.arity = arity;
				this.args = new ArrayDeque<>(arity);
			}

			@Override
			PrologTerm build() {
				return DotListConversion.asListTermNonRecursive(CompoundPrologTerm.fromCollection(this.functor, this.args));
			}

			@Override
			boolean isFinished() {
				return this.args.size() >= this.arity;
			}

			@Override
			void addArg(PrologTerm term) {
				this.args.push(term);
			}
		}

		Deque<TermBuilder> termStack = new ArrayDeque<>();
		Deque<PTerm> nodeStack = new ArrayDeque<>();
		nodeStack.push(topNode);

		while (!nodeStack.isEmpty()) {
			PTerm node = nodeStack.pop();
			TermBuilder term;
			if (node instanceof ANumberTerm) {
				term = new Finished(extractNumber((ANumberTerm) node));
			} else if (node instanceof AVariableTerm) {
				term = new Finished(new VariablePrologTerm(((AVariableTerm) node).getVariable().getText()));
			} else if (node instanceof AAtomTerm) {
				String text = removeQuotes(((AAtomTerm) node).getName().getText());
				if ("[]".equals(text)) {
					term = new Finished(ListPrologTerm.emptyList());
				} else {
					term = new Finished(new CompoundPrologTerm(text));
				}
			} else if (node instanceof AStringTerm) {
				throw new ResultParserException("Double-quoted strings are currently not supported by answerparser");
			} else if (node instanceof AListTerm) {
				int arity = 1;
				AParams aparams = (AParams) ((AListTerm) node).getParams();
				nodeStack.push(aparams.getTerm());

				PMoreParams more = aparams.getMoreParams();
				while (more instanceof AMoreParams) {
					AMoreParams amore = (AMoreParams) more;
					nodeStack.push(amore.getTerm());
					more = amore.getMoreParams();
					arity++;
				}

				term = new ListBuilder(arity);
			} else if (node instanceof ACompoundTerm) {
				ACompoundTerm acompound = (ACompoundTerm) node;
				String functor = removeQuotes(acompound.getFunctor().getText());

				int arity = 1;
				AParams aparams = (AParams) acompound.getParams();
				nodeStack.push(aparams.getTerm());

				PMoreParams more = aparams.getMoreParams();
				while (more instanceof AMoreParams) {
					AMoreParams amore = (AMoreParams) more;
					nodeStack.push(amore.getTerm());
					more = amore.getMoreParams();
					arity++;
				}

				term = new CompoundBuilder(functor, arity);
			} else {
				throw new IllegalStateException("Unexpected subclass of PTerm: " + node.getClass().getCanonicalName());
			}

			if (termStack.isEmpty()) {
				termStack.push(term);
			} else {
				if (term.isFinished()) {
					TermBuilder top = termStack.peek();
					top.addArg(term.build());

					while (termStack.size() > 1 && top.isFinished()) {
						TermBuilder finished = termStack.pop();
						top = termStack.peek();
						top.addArg(finished.build());
					}
				} else {
					termStack.push(term);
				}
			}
		}

		TermBuilder term = termStack.pop();
		assert term.isFinished() && termStack.isEmpty() && nodeStack.isEmpty();
		return term.build();
	}

	private static PrologTerm extractNumber(ANumberTerm node) {
		PNumber number = node.getNumber();
		if (number instanceof AIntegerNumber) {
			AIntegerNumber intNumber = (AIntegerNumber) number;
			String text = intNumber.getInteger().getText();

			char first = text.charAt(0);
			int signOffset = first == '-' || first == '+' ? 1 : 0;
			boolean neg = first == '-';
			if (text.startsWith("0'", signOffset)) {
				char c = text.charAt(2 + signOffset);
				int cp;
				if (c == '\\') {
					// TODO: remove need for StringBuilder allocation
					StringBuilder b = new StringBuilder(1);
					unescapeCharacter(b, text, 3 + signOffset);
					cp = b.codePointAt(0);
				} else {
					cp = c;
				}

				if (neg) {
					cp = -cp;
				}

				return AIntegerPrologTerm.create(cp);
			} else {
				int radix;
				if (text.startsWith("0b", signOffset)) {
					radix = 2;
					text = text.substring(2 + signOffset);
					if (neg) {
						text = "-" + text;
					}
				} else if (text.startsWith("0o", signOffset)) {
					radix = 8;
					text = text.substring(2 + signOffset);
					if (neg) {
						text = "-" + text;
					}
				} else if (text.startsWith("0x", signOffset)) {
					radix = 16;
					text = text.substring(2 + signOffset);
					if (neg) {
						text = "-" + text;
					}
				} else {
					radix = 10;
				}

				return AIntegerPrologTerm.create(text, radix);
			}
		} else if (number instanceof AFloatNumber) {
			AFloatNumber floatNumber = (AFloatNumber) number;
			String text = floatNumber.getFloat().getText();
			double d = Double.parseDouble(text);
			return new FloatPrologTerm(d);
		} else {
			throw new IllegalStateException("Unexpected subclass of PNumber: " + number.getClass().getCanonicalName());
		}
	}

	private static String removeQuotes(String text) {
		int len = text.length();
		if (len > 0) {
			char first = text.charAt(0);
			if (first == '\'' || first == '"') {
				StringBuilder b = new StringBuilder(len - 2);
				for (int i = 1; i < len - 1; i++) {
					char c = text.charAt(i);
					if (c == '\\') {
						i = unescapeCharacter(b, text, i + 1);
					} else if (c == first) {
						// assume the next char is also equal to 'first', enforced by grammar
						i++;
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
