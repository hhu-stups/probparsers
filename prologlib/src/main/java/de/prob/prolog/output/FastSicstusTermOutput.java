package de.prob.prolog.output;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import de.prob.prolog.term.PrologTerm;

/**
 * Term Output that writes the Sicstus FastRW format directly to a given OutputStream.
 * <p>
 * Due to API constraints (unknown term arity when calling {@link IPrologTermOutput#openTerm(String)}),
 * this will buffer bytes until {@link IPrologTermOutput#fullstop()} is called.
 */
public final class FastSicstusTermOutput implements IPrologTermOutput {

	private static abstract class TermContext {
	}

	private static final class ListContext extends TermContext {
		static final ListContext INSTANCE = new ListContext();
	}

	private static final class CompoundContext extends TermContext {

		private final String functor;
		private int arityPos;
		private int arity;

		CompoundContext(String functor) {
			this.functor = functor;
			this.arity = 0;
		}

		String functor() {
			return this.functor;
		}

		int arityPos() {
			return this.arityPos;
		}

		void setArityPos(int arityPos) {
			this.arityPos = arityPos;
		}

		void increaseArity() {
			this.arity++;
		}

		int arity() {
			return this.arity;
		}
	}

	private static final BigInteger BI_255 = BigInteger.valueOf(255);

	private final OutputStream out;
	private final Map<String, Integer> varCache;
	private final Deque<TermContext> termStack;
	private final ModifiableByteBuffer buffer;

	private boolean inAsciiList;

	public FastSicstusTermOutput(OutputStream out) {
		this.out = out;
		this.varCache = new HashMap<>();
		this.termStack = new ArrayDeque<>();
		this.buffer = new ModifiableByteBuffer();
		this.inAsciiList = false;
	}

	private void handleTerm() {
		// end ascii list with zero byte
		if (this.inAsciiList) {
			this.buffer.write(0);
			this.inAsciiList = false;
		}

		TermContext ctx = this.termStack.peek();
		if (ctx instanceof ListContext) {
			// add list marker
			this.buffer.write('[');
		} else if (ctx instanceof CompoundContext) {
			CompoundContext c = (CompoundContext) ctx;
			// not an atom, write compound term prelude
			if (c.arity() == 0) {
				this.buffer.write('S');
				this.buffer.writeNullTerminatedString(c.functor());
				c.setArityPos(this.buffer.size());
				this.buffer.write(0); // arity placeholder
			}
			// remember arity
			((CompoundContext) ctx).increaseArity();
		}
	}

	@Override
	public IPrologTermOutput openTerm(String functor, boolean ignoreIndentation) {
		this.handleTerm();
		this.termStack.push(new CompoundContext(functor));
		return this;
	}

	@Override
	public IPrologTermOutput closeTerm() {
		CompoundContext ctx = (CompoundContext) this.termStack.pop();
		int arity = ctx.arity();
		if (arity < 0 || arity > 0xff) {
			throw new IllegalArgumentException("invalid arity for compound term: " + arity);
		} else if (arity == 0) {
			// this is an atom
			this.buffer.write('A');
			this.buffer.writeNullTerminatedString(ctx.functor());
		} else {
			// fix placeholder arity
			this.buffer.set(ctx.arityPos(), arity);
		}
		return this;
	}

	@Override
	public IPrologTermOutput printAtom(String content) {
		this.handleTerm();
		this.buffer.write('A');
		this.buffer.writeNullTerminatedString(content);
		return this;
	}

	@Override
	public IPrologTermOutput printString(String content) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPrologTermOutput printNumber(long number) {
		if (this.termStack.peek() instanceof ListContext && 0 < number && number <= 255) {
			if (!this.inAsciiList) {
				this.buffer.write('"');
				this.inAsciiList = true;
			}
			this.buffer.write((byte) number);
		} else {
			this.handleTerm();
			this.buffer.write('I');
			this.buffer.writeNullTerminatedString(String.valueOf(number));
		}
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(BigInteger number) {
		if (this.termStack.peek() instanceof ListContext && number.signum() > 0 && number.compareTo(BI_255) <= 0) {
			if (!this.inAsciiList) {
				this.buffer.write('"');
				this.inAsciiList = true;
			}
			this.buffer.write(number.intValue());
		} else {
			this.handleTerm();
			this.buffer.write('I');
			this.buffer.writeNullTerminatedString(number.toString());
		}
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(double number) {
		this.handleTerm();
		this.buffer.write('F');
		this.buffer.writeNullTerminatedString(String.valueOf(number));
		return this;
	}

	@Override
	public IPrologTermOutput openList() {
		this.handleTerm();
		this.termStack.push(ListContext.INSTANCE);
		return this;
	}

	@Override
	public IPrologTermOutput closeList() {
		@SuppressWarnings("unused")
		ListContext _ctx = (ListContext) this.termStack.pop();
		if (this.inAsciiList) {
			this.buffer.write(0);
			this.inAsciiList = false;
		}
		this.buffer.write(']');
		return this;
	}

	@Override
	public IPrologTermOutput tailSeparator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPrologTermOutput printVariable(String var) {
		this.handleTerm();
		this.buffer.write('_');
		int index = this.varCache.computeIfAbsent(var, k -> this.varCache.size());
		this.buffer.writeNullTerminatedString(String.valueOf(index));
		return this;
	}

	@Override
	public IPrologTermOutput printTerm(PrologTerm term) {
		term.toTermOutput(this);
		return this;
	}

	@Override
	public IPrologTermOutput flush() {
		try {
			this.out.flush();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return this;
	}

	@Override
	public IPrologTermOutput fullstop() {
		if (!this.termStack.isEmpty()) {
			throw new IllegalStateException(this.termStack.size() + " unclosed term(s) or list(s)");
		}

		try {
			this.out.write('D'); // version
			this.out.write(this.buffer.bytes(), 0, this.buffer.size());
			this.out.flush();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		this.reset();
		return this;
	}

	public void reset() {
		this.varCache.clear();
		this.buffer.reset();
		this.inAsciiList = false;
	}
}
