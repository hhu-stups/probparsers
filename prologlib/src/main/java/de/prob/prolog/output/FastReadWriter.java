package de.prob.prolog.output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.prob.prolog.term.AIntegerPrologTerm;
import de.prob.prolog.term.FloatPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

/**
 * Writes Prolog terms in SICStus or SWI (undocumented) fastrw format.
 * Generates same output as fast_write(Stream,Term) after use_module(library(fastrw)).
 * And can be read using fast_read(Stream,Term).
 */
public final class FastReadWriter {

	public enum PrologSystem {
		SICSTUS, SWI
	}

	private final PrologSystem flavor;
	private final OutputStream out;

	private int wordBytes;
	private ByteOrder endianness;
	private boolean windows;
	private boolean allowWAtom;
	private Charset cachedWAtomCharset;

	public FastReadWriter(PrologSystem flavor, OutputStream out) {
		this.flavor = Objects.requireNonNull(flavor, "flavor");
		this.out = Objects.requireNonNull(out, "out");
		this.wordBytes = is64Bit() ? 8 : 4;
		this.endianness = ByteOrder.nativeOrder();
		this.windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("windows");
		this.allowWAtom = true;
		this.cachedWAtomCharset = null;
	}

	public FastReadWriter(OutputStream out) {
		this(PrologSystem.SICSTUS, out);
	}

	/**
	 * Enables wide (non latin characters) atom support on SWI.
	 */
	public FastReadWriter withWAtomSupport() {
		this.allowWAtom = true;
		this.cachedWAtomCharset = null;
		return this;
	}

	/**
	 * Disables wide (non latin characters) atom support on SWI.
	 */
	public FastReadWriter withoutWAtomSupport() {
		this.allowWAtom = false;
		this.cachedWAtomCharset = null;
		return this;
	}

	/**
	 * Sets the wide (non latin characters) atom charset on SWI.
	 * This needs to be set last, as other with-ers might reset the charset.
	 */
	public FastReadWriter withWAtomCharset(Charset charset) {
		this.cachedWAtomCharset = charset;
		return this;
	}

	/**
	 * Set the target word size to 64bit on SWI.
	 */
	public FastReadWriter withTarget64bit() {
		this.wordBytes = 8;
		this.cachedWAtomCharset = null;
		return this;
	}

	/**
	 * Set the target word size to 32bit on SWI.
	 */
	public FastReadWriter withTarget32bit() {
		this.wordBytes = 4;
		this.cachedWAtomCharset = null;
		return this;
	}

	/**
	 * Set the target endianness to big on SWI.
	 */
	public FastReadWriter withTargetBigEndian() {
		this.endianness = ByteOrder.BIG_ENDIAN;
		this.cachedWAtomCharset = null;
		return this;
	}

	/**
	 * Set the target endianness to little on SWI.
	 */
	public FastReadWriter withTargetLittleEndian() {
		this.endianness = ByteOrder.LITTLE_ENDIAN;
		this.cachedWAtomCharset = null;
		return this;
	}

	/**
	 * Set the target OS to windows on SWI.
	 */
	public FastReadWriter withTargetWindows() {
		this.windows = true;
		return this;
	}

	/**
	 * Set the target OS to mac/linux on SWI.
	 */
	public FastReadWriter withTargetNoWindows() {
		this.windows = false;
		return this;
	}

	public void fastwrite(PrologTerm term) throws IOException {
		switch (this.flavor) {
			case SICSTUS:
				this.writeTermSicstus(term);
				break;
			case SWI:
				this.writeTermSWI(term);
				break;
			default:
				throw new AssertionError("unknown prolog system: " + this.flavor);
		}
	}

	public void flush() throws IOException {
		this.out.flush();
	}

	private void writeTermSicstus(PrologTerm term) throws IOException {
		this.out.write('D'); // version

		// local variable name -> index table, it is impossible to share variables between different sentences
		Map<String, Integer> varCache = new HashMap<>();

		Deque<PrologTerm> q = new ArrayDeque<>();
		q.addFirst(term);
		while (!q.isEmpty()) {
			PrologTerm t = q.removeFirst();
			if (t.isList()) {
				// strings/lists of bytes can be written using "
				// but we always use the standard way
				ListPrologTerm l = (ListPrologTerm) t;
				if (l.isEmpty()) {
					this.out.write(']');
				} else {
					this.out.write('[');
					q.addFirst(l.tail());
					q.addFirst(l.head());
				}
			} else if (t.isCompound()) {
				this.out.write('S');
				this.writeStringSicstus(t.getFunctor());

				int arity = t.getArity();
				if (arity > 0xff) {
					throw new IllegalArgumentException("can only write terms with a max arity of 255, but got arity " + arity);
				}

				this.out.write(arity);
				for (int i = arity; i >= 1; i--) { // need reverse order because q is a stack
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

				this.out.write(b);
				this.writeStringSicstus(text);
			}
		}
	}

	private void writeStringSicstus(String s) throws IOException {
		this.out.write(s.getBytes(StandardCharsets.UTF_8));
		this.out.write(0);
	}

	private void writeTermSWI(PrologTerm term) throws IOException {
		final int PL_REC_VERSION = 3;
		final int REC_VSHIFT = 5;

		final int REC_32 = 0x01;
		final int REC_64 = 0x02;
		final int REC_SZ;
		if (this.wordBytes == 8) {
			REC_SZ = REC_64;
		} else if (this.wordBytes == 4) {
			REC_SZ = REC_32;
		} else {
			throw new AssertionError();
		}
		final int REC_INT = 0x04;
		final int REC_ATOM = 0x08;
		final int REC_GROUND = 0x10;

		final int REC_HDR = REC_SZ | (PL_REC_VERSION << REC_VSHIFT);

		final int PL_TYPE_VARIABLE = 1;      /* variable */
		final int PL_TYPE_CONS = 8;          /* list-cell */
		final int PL_TYPE_EXT_COMPOUND = 13; /* External (inlined) functor */
		final int PL_TYPE_EXT_FLOAT = 14;    /* float in standard-byte order */

		final int WORDS_PER_DOUBLE = (Double.BYTES + wordBytes - 1) / wordBytes;

		// fast path for primitives
		if (term instanceof AIntegerPrologTerm) {
			AIntegerPrologTerm intTerm = (AIntegerPrologTerm) term;
			try {
				long value = intTerm.longValueExact();
				// this can also deal with numbers that are larger than the max tagged int but still fit into a long
				this.out.write(REC_HDR | REC_INT | REC_GROUND);
				writeInt64SWI(this.out, value);
				return;
			} catch (ArithmeticException ignored) {}
		} else if (term.isAtom()) { // also includes the empty list
			this.out.write(REC_HDR | REC_ATOM | REC_GROUND);
			this.writeAtomSWI(this.out, term);
			return;
		}

		ByteArrayOutputStream data = new ByteArrayOutputStream();
		Map<String, Integer> varCache = new HashMap<>();
		int size = 0; // global stack size in words

		// write term data to data
		Deque<PrologTerm> q = new ArrayDeque<>();
		q.addFirst(term);
		while (!q.isEmpty()) {
			PrologTerm t = q.removeFirst();
			if (t.isVariable()) {
				data.write(PL_TYPE_VARIABLE);
				int varIndex = varCache.computeIfAbsent(t.getFunctor(), k -> varCache.size());
				writeSizeSWI(data, varIndex);
			} else if (t.isAtom()) { // also includes the empty list
				this.writeAtomSWI(data, t);
			} else if (t instanceof AIntegerPrologTerm) {
				this.writeIntSWI(data, (AIntegerPrologTerm) t);
			} else if (t instanceof FloatPrologTerm) {
				data.write(PL_TYPE_EXT_FLOAT);
				double value = ((FloatPrologTerm) t).getValue();
				ByteBuffer ieee754LE = ByteBuffer.allocate(8);
				ieee754LE.order(ByteOrder.LITTLE_ENDIAN);
				ieee754LE.putDouble(value);
				ieee754LE.flip();
				int len = ieee754LE.remaining();
				assert len == 8;
				data.write(ieee754LE.array(), ieee754LE.arrayOffset(), len);
				size += WORDS_PER_DOUBLE + 2;
			} else if (t.isList()) {
				ListPrologTerm l = (ListPrologTerm) t;
				data.write(PL_TYPE_CONS);
				q.addFirst(l.tail());
				q.addFirst(l.head());
				size += 3; // cons functor + head + tail
			} else if (t.isTerm()) {
				data.write(PL_TYPE_EXT_COMPOUND);
				int arity = t.getArity();
				writeSizeSWI(data, arity);
				this.writeAtomSWI(data, t);
				size += 1 + arity; // functor + arguments
				for (int i = arity; i >= 1; i--) {
					q.addFirst(t.getArgument(i));
				}
			} else {
				throw new IllegalArgumentException("unsupported prolog term " + t.getClass().getSimpleName());
			}
		}

		// magic code: REC_HDR (| REC_GROUND)
		int tag = REC_HDR;
		if (varCache.isEmpty()) {
			tag |= REC_GROUND;
		}
		this.out.write(tag);

		// code size
		writeSizeSWI(this.out, data.size());

		// (global) stack size
		writeSizeSWI(this.out, size);

		// if not ground: numvars
		if (!varCache.isEmpty()) {
			writeSizeSWI(this.out, varCache.size());
		}

		// data (code)
		this.out.write(data.toByteArray());
	}

	private void writeAtomSWI(OutputStream os, PrologTerm t) throws IOException {
		final int PL_TYPE_NIL = 9;        /* [] */
		final int PL_TYPE_EXT_ATOM = 11;  /* External (inlined) atom */
		final int PL_TYPE_EXT_WATOM = 12; /* External (inlined) wide atom */

		if (t.isList() && ((ListPrologTerm) t).isEmpty()) {
			os.write(PL_TYPE_NIL);
			return;
		}

		String atom = t.getFunctor();
		CharsetEncoder extendedAsciiEncoder = StandardCharsets.ISO_8859_1.newEncoder()
				.onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT);
		try {
			ByteBuffer result = extendedAsciiEncoder.encode(CharBuffer.wrap(atom));
			os.write(PL_TYPE_EXT_ATOM);

			int len = result.remaining();
			writeSizeSWI(os, len);
			os.write(result.array(), result.arrayOffset(), len);
		} catch (UnmappableCharacterException e) {
			if (this.allowWAtom) {
				os.write(PL_TYPE_EXT_WATOM);
				if (this.cachedWAtomCharset == null) {
					this.cachedWAtomCharset = this.wcharCharset();
				}
				byte[] bytes = atom.getBytes(this.cachedWAtomCharset);
				writeSizeSWI(os, bytes.length);
				os.write(bytes);
			} else {
				throw new IllegalArgumentException("atom contains non-latin characters", e);
			}
		}
	}

	private void writeIntSWI(OutputStream os, AIntegerPrologTerm t) throws IOException {
		final int TAG_BITS = 7;
		final int SIGN_BITS = 1;
		final long MAX_TAGGED_INT = (1L << (this.wordBytes * 8 - TAG_BITS - SIGN_BITS)) - 1;
		final long MIN_TAGGED_INT = -(1L << (this.wordBytes * 8 - TAG_BITS - SIGN_BITS));

		final int PL_TYPE_TAGGED_INTEGER = 4; /* tagged integer */

		long value;
		try {
			value = t.longValueExact();
			if (value <= MAX_TAGGED_INT && value >= MIN_TAGGED_INT) {
				os.write(PL_TYPE_TAGGED_INTEGER);
				writeInt64SWI(os, value);
				return;
			}
		} catch (ArithmeticException ignored) {
		}

		// TODO: support bigger integers
		throw new IllegalArgumentException("int out of range (" + t.getFunctor() + ")");
	}

	private static void writeSizeSWI(OutputStream os, int val) throws IOException {
		// this routine takes size_t in C and thus is dependent on the word size
		// here it takes int, so we can hardcode the integer size in the definition of "zips"
		if ((val & ~0x7f) == 0) { // fast path and 0: just a single byte
			os.write(val);
		} else {
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

	private static void writeInt64SWI(OutputStream os, long value) throws IOException {
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
		if (this.windows) {
			// https://learn.microsoft.com/en-us/cpp/cpp/char-wchar-t-char16-t-char32-t?view=msvc-170
			// Windows always uses UTF-16LE
			return StandardCharsets.UTF_16LE;
		} else if (this.endianness == ByteOrder.BIG_ENDIAN) {
			// While on Linux UCS-4 (which is UTF-32) is used, but it depends on the system's endianness
			return Charset.forName("UTF-32BE");
		} else {
			// Dito
			return Charset.forName("UTF-32LE");
		}
	}

	private static boolean is64Bit() {
		String bits = System.getProperty("sun.arch.data.model", System.getProperty("com.ibm.vm.bitmode", System.getProperty("os.arch", "")));
		return bits.contains("64");
	}
}
