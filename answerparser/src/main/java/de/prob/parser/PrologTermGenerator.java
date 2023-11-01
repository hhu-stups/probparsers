/*
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.prob.parser;

import de.prob.core.sablecc.node.*;
import de.prob.prolog.term.*;

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
			throw new ResultParserException(message, null);
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
			throw new ResultParserException("Prolog query returned a callback/progress result, which isn't supported here: " + query, null);
		} else if (!(topnode instanceof AYesResult)) {
			final String message = "Prolog query unexpectedly failed: " + query;
			throw new ResultParserException(message, null);
		}
		return toPrologTerm(((AYesResult) topnode).getTerm());
	}

	public static PrologTerm toPrologTerm(final PTerm node) {
		PrologTerm term;
		if (node instanceof ANumberTerm) {
			String text = ((ANumberTerm) node).getNumber().getText();
			if (text.indexOf('.') != -1 || text.indexOf('e') != -1 || text.indexOf('E') != -1) {
				term = new FloatPrologTerm(Double.parseDouble(text));
			} else {
				term = AIntegerPrologTerm.create(text);
			}
		} else if (node instanceof AAtomTerm) {
			String text = ((AAtomTerm) node).getName().getText();
			if ("[]".equals(text)) {
				term = ListPrologTerm.emptyList();
			} else {
				text = removeQuotes(text);
				term = new CompoundPrologTerm(text);
			}
		} else if (node instanceof ATerm) {
			ATerm aterm = (ATerm) node;
			int listSize = getListSize(node);
			if (listSize == 0) {
				term = ListPrologTerm.emptyList();
			} else if (listSize > 0) {
				PrologTerm[] list = new PrologTerm[listSize];
				fillListWithElements(list, aterm);
				term = new ListPrologTerm(list);
			} else {
				String functor = removeQuotes(aterm.getFunctor().getText());
				PrologTerm[] params = evalParameters((AParams) aterm.getParams());
				term = new CompoundPrologTerm(functor, params);
			}
		} else if (node instanceof AVariableTerm) {
			String text = removeQuotes(((AVariableTerm) node).getVariable().getText());
			term = new VariablePrologTerm(text);
		} else {
			throw new IllegalStateException("Unexpected subclass of PTerm: " + node.getClass().getCanonicalName());
		}
		return term;
	}

	private static String removeQuotes(final String text) {
		if (text != null && !text.isEmpty()) {
			char first = text.charAt(0);
			if (first == '\'' || first == '"') {
				int len = text.length();
				StringBuilder b = new StringBuilder(len - 2);
				for (int i = 1; i < len - 1; i++) {
					char c = text.charAt(i);
					if (c == '\\') {
						i = unescapeCharacter(b, text, i);
					} else {
						b.append(c);
					}
				}
				return b.toString();
			}
		}

		return text;
	}

	private static int unescapeCharacter(final StringBuilder b, final String text, int i) {
		char c;
		i++;
		c = text.charAt(i);
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
				i = getHex(i, text, b);
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
				i = getOct(i, text, b);
				break;
			default:
				b.append(c);
				break;
		}

		return i;
	}

	private static int getHex(int i, final String text, final StringBuilder builder) {
		int end = text.indexOf('\\', ++i);
		int value = Integer.parseInt(text.substring(i, end), 16);
		builder.append((char) value);
		return end;
	}

	private static int getOct(int i, final String text, final StringBuilder builder) {
		int end = text.indexOf('\\', i);
		int value = Integer.parseInt(text.substring(i, end), 8);
		builder.append((char) value);
		return end;
	}

	private static PrologTerm[] evalParameters(final AParams node) {
		PrologTerm[] params = evalParameters(1, node.getMoreParams());
		params[0] = toPrologTerm(node.getTerm());
		return params;
	}

	private static PrologTerm[] evalParameters(final int before, final PMoreParams moreParams) {
		PrologTerm[] params;
		if (moreParams instanceof AEmptyMoreParams) {
			params = new PrologTerm[before];
		} else if (moreParams instanceof AMoreParams) {
			AMoreParams nonempty = (AMoreParams) moreParams;
			params = evalParameters(before + 1, nonempty.getMoreParams());
			params[before] = toPrologTerm(nonempty.getTerm());
		} else {
			throw new IllegalStateException("Unexpected subclass of PMoreParams: " + moreParams.getClass().getCanonicalName());
		}
		return params;
	}

	private static int getArity(final AParams params) {
		PMoreParams moreParams = params.getMoreParams();
		int arity = 1;
		while (moreParams != null) {
			if (moreParams instanceof AEmptyMoreParams) {
				moreParams = null;
			} else {
				arity++;
				moreParams = ((AMoreParams) moreParams).getMoreParams();
			}
		}
		return arity;
	}

	private static int getListSize(PTerm term) {
		int size = 0;
		while (term != null) {
			boolean invalid;
			if (term instanceof AAtomTerm) {
				String atom = ((AAtomTerm) term).getName().getText();
				invalid = !"[]".equals(atom);
				term = null;
			} else if (term instanceof ATerm) {
				ATerm copoundTerm = (ATerm) term;
				String functor = copoundTerm.getFunctor().getText();
				if (".".equals(functor) || "'.'".equals(functor)) {
					AParams params = (AParams) copoundTerm.getParams();
					int arity = getArity(params);
					if (arity == 2) {
						size++;
						term = ((AMoreParams) params.getMoreParams()).getTerm();
						invalid = false;
					} else {
						invalid = true;
					}
				} else {
					invalid = true;
				}
			} else {
				invalid = true;
			}
			if (invalid) {
				size = -1;
				term = null;
			}
		}
		return size;
	}

	private static void fillListWithElements(final PrologTerm[] list, ATerm aterm) {
		for (int i = 0; i < list.length; i++) {
			AParams params = (AParams) aterm.getParams();
			list[i] = toPrologTerm(params.getTerm());
			if (i < list.length - 1) {
				// only get next element when this iteration was not the last
				aterm = (ATerm) ((AMoreParams) params.getMoreParams()).getTerm();
			}
		}
	}
}
