package de.prob.prolog.output;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.prob.prolog.term.PrologTerm;

/**
 * Term Output that writes the SWI FastRW format directly to a given OutputStream.
 * <p>
 * Due to API constraints (unknown total size when printing anything),
 * this will sometimes buffer bytes until {@link IPrologTermOutput#fullstop()} is called.
 */
public final class FastSwiTermOutput implements IPrologTermOutput {

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

	// tagged int representation
	private static final int TAG_BITS = 7;
	private static final int SIGN_BITS = 1;

	// tags
	private static final int PL_REC_VERSION = 3;
	private static final int REC_VSHIFT = 5;
	private static final int REC_32 = 0x01;
	private static final int REC_64 = 0x02;
	private static final int REC_INT = 0x04;    /* fast path - just an int */
	private static final int REC_ATOM = 0x08;   /* fast path - just an atom */
	private static final int REC_GROUND = 0x10;

	// types
	private static final int PL_TYPE_VARIABLE = 1;       /* variable */
	private static final int PL_TYPE_TAGGED_INTEGER = 4; /* tagged integer */
	private static final int PL_TYPE_CONS = 8;           /* list-cell */
	private static final int PL_TYPE_NIL = 9;            /* [] */
	private static final int PL_TYPE_EXT_ATOM = 11;      /* External (inlined) atom */
	private static final int PL_TYPE_EXT_WATOM = 12;     /* External (inlined) wide atom */
	private static final int PL_TYPE_EXT_COMPOUND = 13;  /* External (inlined) functor */
	private static final int PL_TYPE_EXT_FLOAT = 14;     /* float in standard-byte order */

	private final OutputStream out;
	private final Map<String, Integer> varCache;
	private final Deque<TermContext> termStack;
	private final ModifiableByteBuffer buffer;

	private boolean topLevel;
	private int stackSize;

	private int wordBytes;
	private ByteOrder endianness;
	private boolean windows;
	private boolean allowWAtom;
	private Charset cachedWAtomCharset;

	public FastSwiTermOutput(OutputStream out) {
		this.out = out;
		this.varCache = new HashMap<>();
		this.termStack = new ArrayDeque<>();
		this.buffer = new ModifiableByteBuffer();
		this.topLevel = true;
		this.stackSize = 0;

		this.wordBytes = is64Bit() ? 8 : 4;
		this.endianness = ByteOrder.nativeOrder();
		this.windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("windows");
		this.allowWAtom = true;
		this.cachedWAtomCharset = null;
	}

	/**
	 * Enables wide (non latin characters) atom support.
	 */
	public FastSwiTermOutput withWAtomSupport() {
		this.allowWAtom = true;
		this.cachedWAtomCharset = null;
		return this;
	}

	/**
	 * Disables wide (non latin characters) atom support.
	 */
	public FastSwiTermOutput withoutWAtomSupport() {
		this.allowWAtom = false;
		this.cachedWAtomCharset = null;
		return this;
	}

	/**
	 * Sets the wide (non latin characters) atom charset on SWI.
	 * This needs to be set last, as other with-ers might reset the charset.
	 */
	public FastSwiTermOutput withWAtomCharset(Charset charset) {
		this.cachedWAtomCharset = charset;
		return this;
	}

	/**
	 * Set the target word size to 64bit.
	 */
	public FastSwiTermOutput withTarget64bit() {
		this.wordBytes = 8;
		this.cachedWAtomCharset = null;
		return this;
	}

	/**
	 * Set the target word size to 32bit.
	 */
	public FastSwiTermOutput withTarget32bit() {
		this.wordBytes = 4;
		this.cachedWAtomCharset = null;
		return this;
	}

	/**
	 * Set the target endianness to big.
	 */
	public FastSwiTermOutput withTargetBigEndian() {
		this.endianness = ByteOrder.BIG_ENDIAN;
		this.cachedWAtomCharset = null;
		return this;
	}

	/**
	 * Set the target endianness to little.
	 */
	public FastSwiTermOutput withTargetLittleEndian() {
		this.endianness = ByteOrder.LITTLE_ENDIAN;
		this.cachedWAtomCharset = null;
		return this;
	}

	/**
	 * Set the target OS to windows.
	 */
	public FastSwiTermOutput withTargetWindows() {
		this.windows = true;
		return this;
	}

	/**
	 * Set the target OS to mac/linux.
	 */
	public FastSwiTermOutput withTargetNoWindows() {
		this.windows = false;
		return this;
	}

	private void handleTerm() {
		this.topLevel = false;
		TermContext ctx = this.termStack.peek();
		if (ctx instanceof ListContext) {
			// add list marker
			this.buffer.write(PL_TYPE_CONS);
			this.stackSize += 3; // cons functor + head + tail
		} else if (ctx instanceof CompoundContext) {
			CompoundContext c = (CompoundContext) ctx;
			// not an atom, write compound term prelude
			if (c.arity() == 0) {
				this.buffer.write(PL_TYPE_EXT_COMPOUND);
				c.setArityPos(this.buffer.size());
				this.buffer.write(0); // arity placeholder
				try {
					this.writeString(this.buffer, c.functor());
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
			// remember arity
			c.increaseArity();
		}
	}

	@Override
	public IPrologTermOutput openTerm(String functor, boolean ignoreIndentation) {
		// delay setting topLevel to false...
		if (!this.topLevel || !this.termStack.isEmpty()) {
			this.handleTerm();
		}
		this.termStack.push(new CompoundContext(functor));
		return this;
	}

	@Override
	public IPrologTermOutput closeTerm() {
		CompoundContext ctx = (CompoundContext) this.termStack.pop();
		// ...so we can check here if this was a top-level atom
		int arity = ctx.arity();
		if (arity < 0) {
			throw new IllegalArgumentException("invalid arity for compound term: " + arity);
		} else if (arity == 0) {
			// we have not written anything yet
			this.writeAtom(ctx.functor(), false);
		} else {
			// fix arity
			if ((arity & ~0x7f) == 0) {
				// size can be written as one byte, fast path
				this.buffer.set(ctx.arityPos(), arity);
			} else {
				// slow path
				int maxZips = (Integer.SIZE + 7 - 1) / 7;
				int actualZips = maxZips - Integer.numberOfLeadingZeros(arity) / 7;
				this.buffer.shiftRight(ctx.arityPos() + 1, actualZips - 1);
				int oldSize = this.buffer.size();
				try {
					this.buffer.setSize(ctx.arityPos());
					writeSize(this.buffer, arity);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				} finally {
					this.buffer.setSize(oldSize);
				}
			}
			this.stackSize += 1 + arity; // functor + terms
		}
		return this;
	}

	@Override
	public IPrologTermOutput printAtom(String content) {
		this.writeAtom(content, true);
		return this;
	}

	@Override
	public IPrologTermOutput printString(String content) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPrologTermOutput printNumber(long number) {
		if (this.topLevel && this.termStack.isEmpty()) {
			// fast path
			this.topLevel = false;
			try {
				this.out.write(REC_HDR() | REC_INT | REC_GROUND);
				writeInt64(this.out, number);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		} else {
			final long MAX_TAGGED_INT = (1L << (this.wordBytes * 8 - TAG_BITS - SIGN_BITS)) - 1;
			final long MIN_TAGGED_INT = -(1L << (this.wordBytes * 8 - TAG_BITS - SIGN_BITS));

			this.handleTerm();
			if (MIN_TAGGED_INT <= number && number <= MAX_TAGGED_INT) {
				this.buffer.write(PL_TYPE_TAGGED_INTEGER);
				try {
					writeInt64(this.buffer, number);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			} else {
				// TODO: support bigger integers
				throw new UnsupportedOperationException("int out of range (" + number + ")");
			}
		}
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(BigInteger number) {
		try {
			return this.printNumber(number.longValueExact());
		} catch (ArithmeticException ignored) {
		}

		// TODO: support bigger integers
		throw new UnsupportedOperationException("int out of range (" + number + ")");
	}

	@Override
	public IPrologTermOutput printNumber(double number) {
		final int WORDS_PER_DOUBLE = (Double.BYTES + this.wordBytes - 1) / this.wordBytes;

		this.handleTerm();
		this.buffer.write(PL_TYPE_EXT_FLOAT);
		// TODO: use Double#toRawLongBits directly
		ByteBuffer ieee754LE = ByteBuffer.allocate(Double.BYTES);
		ieee754LE.order(ByteOrder.LITTLE_ENDIAN);
		ieee754LE.putDouble(number);
		ieee754LE.flip();
		int len = ieee754LE.remaining();
		assert len == Double.BYTES;
		this.buffer.write(ieee754LE.array(), ieee754LE.arrayOffset(), len);
		this.stackSize += WORDS_PER_DOUBLE + 2;
		return this;
	}

	@Override
	public IPrologTermOutput openList() {
		// delay setting topLevel to false...
		if (!this.topLevel || !this.termStack.isEmpty()) {
			this.handleTerm();
		}
		this.termStack.push(ListContext.INSTANCE);
		return this;
	}

	@Override
	public IPrologTermOutput closeList() {
		@SuppressWarnings("unused")
		ListContext _ctx = (ListContext) this.termStack.pop();
		// ...so we can check here if this was an empty top-level list
		if (this.topLevel && this.termStack.isEmpty()) {
			// fast path
			this.topLevel = false;
			try {
				this.out.write(REC_HDR() | REC_ATOM | REC_GROUND);
				this.out.write(PL_TYPE_NIL);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		} else {
			this.buffer.write(PL_TYPE_NIL);
		}
		return this;
	}

	@Override
	public IPrologTermOutput tailSeparator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPrologTermOutput printVariable(String var) {
		this.handleTerm();
		this.buffer.write(PL_TYPE_VARIABLE);
		int index = this.varCache.computeIfAbsent(var, k -> this.varCache.size());
		try {
			writeSize(this.buffer, index);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
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

		if (!this.topLevel && this.buffer.size() == 0) {
			// fast path
			this.flush();
			this.reset();
			return this;
		}

		int tag = REC_HDR();
		if (this.varCache.isEmpty()) {
			tag |= REC_GROUND;
		}
		try {
			this.out.write(tag); // magic
			writeSize(this.out, this.buffer.size()); // code size
			writeSize(this.out, this.stackSize); // (global) stack size
			if (!varCache.isEmpty()) {
				writeSize(this.out, this.varCache.size()); // number of vars
			}
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
		this.topLevel = true;
		this.stackSize = 0;
	}

	private void writeAtom(String atom, boolean callHandleTerm) {
		if (this.topLevel && this.termStack.isEmpty()) {
			// fast path
			this.topLevel = false;
			try {
				this.out.write(REC_HDR() | REC_ATOM | REC_GROUND);
				this.writeString(this.out, atom);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		} else {
			if (callHandleTerm) {
				this.handleTerm();
			}
			try {
				this.writeString(this.buffer, atom);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	private void writeString(OutputStream os, String atom) throws IOException {
		CharsetEncoder extendedAsciiEncoder = StandardCharsets.ISO_8859_1.newEncoder()
				                                      .onMalformedInput(CodingErrorAction.REPORT)
				                                      .onUnmappableCharacter(CodingErrorAction.REPORT);
		try {
			ByteBuffer result = extendedAsciiEncoder.encode(CharBuffer.wrap(atom));
			os.write(PL_TYPE_EXT_ATOM);

			int len = result.remaining();
			writeSize(os, len);
			os.write(result.array(), result.arrayOffset(), len);
		} catch (CharacterCodingException e) {
			if (this.allowWAtom) {
				os.write(PL_TYPE_EXT_WATOM);
				byte[] bytes = atom.getBytes(this.wcharCharset());
				writeSize(os, bytes.length);
				os.write(bytes);
			} else {
				throw new IllegalArgumentException("atom contains non-latin characters", e);
			}
		}
	}

	private static void writeSize(OutputStream os, int val) throws IOException {
		// this routine takes size_t in C and thus is dependent on the word size
		// here it takes int, so we can hardcode the integer size in the definition of "zips"
		if ((val & ~0x7f) == 0) { // fast path and 0: just a single byte
			os.write(val);
		} else {
			// this is a varint encoding, but the MSB comes first
			// so we cannot do an early exit from the loop
			// and have to calculate the number of bytes at the beginning
			boolean leading = true;
			for (int zips = (Integer.SIZE + 7 - 1) / 7 - 1; zips >= 0; zips--) {
				int d = (val >>> zips * 7) & 0x7f;
				if (d != 0 || !leading) {
					if (zips != 0) {
						d |= 0x80;
					}
					os.write(d);
					leading = false;
				}
			}
		}
	}

	private static void writeInt64(OutputStream os, long value) throws IOException {
		int bytes;
		if (value == 0) {
			bytes = 1;
		} else if (value == Long.MIN_VALUE) {
			bytes = Long.BYTES;
		} else {
			int msb = Long.SIZE - 1 - Long.numberOfLeadingZeros(Math.abs(value));
			bytes = (msb + 9) / 8;
		}
		os.write(bytes);

		while (--bytes >= 0) {
			int b = (int) (value >> bytes * 8) & 0xff;
			os.write(b);
		}
	}

	private Charset wcharCharset() {
		if (this.cachedWAtomCharset == null) {
			if (this.windows) {
				// https://learn.microsoft.com/en-us/cpp/cpp/char-wchar-t-char16-t-char32-t?view=msvc-170
				// Windows always uses UTF-16LE
				this.cachedWAtomCharset = StandardCharsets.UTF_16LE;
			} else if (this.endianness == ByteOrder.BIG_ENDIAN) {
				// While on Linux UCS-4 (which is UTF-32) is used, but it depends on the system's endianness
				this.cachedWAtomCharset = Charset.forName("UTF-32BE");
			} else {
				// Ditto
				this.cachedWAtomCharset = Charset.forName("UTF-32LE");
			}
		}
		return this.cachedWAtomCharset;
	}

	private int REC_SZ() {
		if (this.wordBytes == 8) {
			return REC_64;
		} else if (this.wordBytes == 4) {
			return REC_32;
		} else {
			throw new AssertionError();
		}
	}

	private int REC_HDR() {
		return REC_SZ() | (PL_REC_VERSION << REC_VSHIFT);
	}

	private static boolean is64Bit() {
		String bits = System.getProperty("sun.arch.data.model", System.getProperty("com.ibm.vm.bitmode", System.getProperty("os.arch", "")));
		return bits.contains("64");
	}
}
