package de.prob.prolog.output;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Objects;

import de.prob.prolog.term.PrologTerm;

/**
 * @deprecated Use {@link FastSicstusTermOutput} or {@link FastSwiTermOutput} directly.
 */
@Deprecated
public final class FastReadWriter {

	@Deprecated
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

	private void writeTermSicstus(PrologTerm term) {
		FastSicstusTermOutput to = new FastSicstusTermOutput(this.out);
		to.printTerm(term);
		to.fullstop();
	}

	private void writeTermSWI(PrologTerm term) {
		FastSwiTermOutput to = new FastSwiTermOutput(this.out);
		if (this.wordBytes == 4) {
			to.withTarget32bit();
		} else if (this.wordBytes == 8) {
			to.withTarget64bit();
		} else {
			throw new AssertionError();
		}
		if (this.endianness == ByteOrder.BIG_ENDIAN) {
			to.withTargetBigEndian();
		} else if (this.endianness == ByteOrder.LITTLE_ENDIAN) {
			to.withTargetLittleEndian();
		} else {
			throw new AssertionError();
		}
		if (this.windows) {
			to.withTargetWindows();
		} else {
			to.withTargetNoWindows();
		}
		if (this.allowWAtom) {
			to.withWAtomSupport();
		} else {
			to.withoutWAtomSupport();
		}
		to.withWAtomCharset(this.cachedWAtomCharset);

		to.printTerm(term);
		to.fullstop();
	}

	private static boolean is64Bit() {
		String bits = System.getProperty("sun.arch.data.model", System.getProperty("com.ibm.vm.bitmode", System.getProperty("os.arch", "")));
		return bits.contains("64");
	}
}
