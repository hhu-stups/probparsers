package de.prob.prolog.output;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.prob.prolog.term.AIntegerPrologTerm;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.FloatPrologTerm;
import de.prob.prolog.term.PrologTerm;

/**
 * Writes Prolog terms in SICStus (undocumented) fastrw format.
 * Generates same output as fast_write(Stream,Term) after use_module(library(fastrw)).
 * And can be read using fast_read(Stream,Term).
 */
public final class FastReadWriter {

	private static final PrologTerm LIST_ELEMENT = new CompoundPrologTerm("list_element");
	private static final PrologTerm END_OF_LIST = new CompoundPrologTerm("end_of_list");

	private final OutputStream out;

	public FastReadWriter(OutputStream out) {
		this.out = Objects.requireNonNull(out, "out");
	}

	public void fastwrite(PrologTerm term) throws IOException {
		out.write('D');
		writeTerm(term);
	}

	public void flush() throws IOException {
		this.out.flush();
	}

	private void writeTerm(PrologTerm term) throws IOException {
		// local variable name -> index table, it is impossible to share variables between different sentences
		Map<String, Integer> varCache = new HashMap<>();
		Deque<PrologTerm> q = new ArrayDeque<>();
		q.addFirst(term);
		while (!q.isEmpty()) {
			PrologTerm t = q.removeFirst();
			if (t == END_OF_LIST) {
				out.write(']');
				continue;
			} else if (t == LIST_ELEMENT) {
				out.write('[');
				continue;
			}

			if (t.isList()) {
				// strings/lists of integers are written using "
				int arity = t.getArity();
				if (arity == 0) {
					out.write(']');
				} else {
					q.addFirst(END_OF_LIST);
					for (int i = arity; i >= 1; i--) {
						q.addFirst(t.getArgument(i));
						q.addFirst(LIST_ELEMENT);
					}
				}
			} else if (t.isTerm() && !t.isAtom()) {
				out.write('S');
				writeNullTerminated(t.getFunctor());

				int arity = t.getArity();
				if (arity > 0xff) {
					throw new IllegalArgumentException("can only write terms with a max arity of 255, but got arity " + arity);
				}

				out.write(arity);
				for (int i = arity; i >= 1; i--) {
					q.addFirst(t.getArgument(i));
				}
			} else {
				byte b;
				String text;
				if (t instanceof AIntegerPrologTerm) {
					b = 'I';
					text = t.getFunctor(); // '-'-prefix is supported
				} else if (t instanceof FloatPrologTerm) {
					b = 'F';
					text = t.getFunctor(); // this even works with numbers like 1.337E101
				} else if (t.isAtom()) {
					b = 'A';
					text = t.getFunctor(); // this should work with non-ascii chars as well
				} else if (t.isVariable()) {
					b = '_';
					text = String.valueOf(varCache.computeIfAbsent(t.getFunctor(), k -> varCache.size()));
				} else {
					throw new IllegalArgumentException("unsupported prolog term " + t.getClass().getSimpleName());
				}

				out.write(b);
				writeNullTerminated(text);
			}
		}
	}

	private void writeNullTerminated(String s) throws IOException {
		out.write(s.getBytes(StandardCharsets.UTF_8));
		out.write(0);
	}
}
