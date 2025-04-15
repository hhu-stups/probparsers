package de.prob.prolog.output;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
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

	/**
	 * Simple growable byte buffer that allows direct access to internal buffer and arbitrary writes.
	 */
	static final class Buffer {

		private byte[] buffer;
		private int size;

		Buffer() {
			this.buffer = new byte[32];
			this.size = 0;
		}

		byte[] bytes() {
			return this.buffer;
		}

		int size() {
			return this.size;
		}

		void setSize(int size) {
			this.size = size;
		}

		void reset() {
			this.size = 0;
		}

		private void ensureCapacity(int minCapacity) {
			if (minCapacity > this.buffer.length) {
				this.buffer = Arrays.copyOf(this.buffer, this.buffer.length * 2);
			}
		}

		void write(byte value) {
			this.ensureCapacity(this.size + 1);
			this.buffer[this.size++] = value;
		}

		void write(byte[] values, int off, int len) {
			this.ensureCapacity(this.size + len);
			System.arraycopy(values, off, this.buffer, this.size, len);
			this.size += len;
		}

		void writeNullTerminatedString(String s) {
			byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
			// TODO: should check for zero byte here
			this.write(bytes, 0, bytes.length);
			this.write((byte) 0);
		}

		void set(int pos, byte value) {
			this.ensureCapacity(pos + 1);
			this.buffer[pos] = value;
		}

		void set(int pos, byte[] values, int off, int len) {
			this.ensureCapacity(pos + len);
			System.arraycopy(values, off, this.buffer, pos, len);
		}
	}

	private static abstract class TermContext {}

	private static final class ListContext extends TermContext {}

	private static final class CompoundContext extends TermContext {

		private final int tagPos;
		private final int arityPos;
		private int arity;

		CompoundContext(int tagPos, int arityPos) {
			this.tagPos = tagPos;
			this.arityPos = arityPos;
			this.arity = 0;
		}

		int tagPos() {
			return this.tagPos;
		}

		int arityPos() {
			return this.arityPos;
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
	private final Buffer buffer;

	private boolean inAsciiList;

	public FastSicstusTermOutput(OutputStream out) {
		this.out = out;
		this.varCache = new HashMap<>();
		this.termStack = new ArrayDeque<>();
		this.buffer = new Buffer();
		this.inAsciiList = false;
	}

	private void handleTerm() {
		// end ascii list with zero byte
		if (this.inAsciiList) {
			this.buffer.write((byte) 0);
			this.inAsciiList = false;
		}

		TermContext ctx = this.termStack.peek();
		if (ctx instanceof ListContext) {
			// add list marker
			this.buffer.write((byte ) '[');
		} else if (ctx instanceof CompoundContext) {
			// remember arity
			((CompoundContext) ctx).increaseArity();
		}
	}

	@Override
	public IPrologTermOutput openTerm(String functor, boolean ignoreIndentation) {
		this.handleTerm();
		int tagPos = this.buffer.size();
		this.buffer.write((byte) 'S');
		this.buffer.writeNullTerminatedString(functor);
		this.termStack.push(new CompoundContext(tagPos, this.buffer.size()));
		this.buffer.write((byte) 0); // arity, will be set later
		return this;
	}

	@Override
	public IPrologTermOutput closeTerm() {
		CompoundContext ctx = (CompoundContext) this.termStack.pop();
		int arity = ctx.arity();
		if (arity == 0) {
			// convert started 'S' term back into 'A' term
			this.buffer.set(ctx.tagPos(), (byte) 'A');
			this.buffer.setSize(ctx.arityPos());
		} else if (arity < 0 || arity > 0xff) {
			throw new IllegalArgumentException("invalid arity for compound term: " + arity);
		} else {
			// fix placeholder arity
			this.buffer.set(ctx.arityPos(), (byte) arity);
		}
		return this;
	}

	@Override
	public IPrologTermOutput printAtom(String content) {
		this.handleTerm();
		this.buffer.write((byte) 'A');
		this.buffer.writeNullTerminatedString(String.valueOf(content));
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
				this.buffer.write((byte) '"');
				this.inAsciiList = true;
			}
			this.buffer.write((byte) number);
		} else {
			this.handleTerm();
			this.buffer.write((byte) 'I');
			this.buffer.writeNullTerminatedString(String.valueOf(number));
		}
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(BigInteger number) {
		if (this.termStack.peek() instanceof ListContext && number.signum() > 0 && number.compareTo(BI_255) <= 0) {
			if (!this.inAsciiList) {
				this.buffer.write((byte) '"');
				this.inAsciiList = true;
			}
			this.buffer.write((byte) number.intValue());
		} else {
			this.handleTerm();
			this.buffer.write((byte) 'I');
			this.buffer.writeNullTerminatedString(number.toString());
		}
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(double number) {
		this.handleTerm();
		this.buffer.write((byte) 'F');
		this.buffer.writeNullTerminatedString(String.valueOf(number));
		return this;
	}

	@Override
	public IPrologTermOutput openList() {
		this.handleTerm();
		this.termStack.push(new ListContext());
		return this;
	}

	@Override
	public IPrologTermOutput closeList() {
		@SuppressWarnings("unused")
		ListContext _ctx = (ListContext) this.termStack.pop();
		if (this.inAsciiList) {
			this.buffer.write((byte) 0);
			this.inAsciiList = false;
		}
		this.buffer.write((byte) ']');
		return this;
	}

	@Override
	public IPrologTermOutput tailSeparator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPrologTermOutput printVariable(String var) {
		this.handleTerm();
		this.buffer.write((byte) '_');
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
