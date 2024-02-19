package de.be4.classicalb.core.parser;

import de.prob.prolog.term.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

// writes Prolog terms in SICStus (undocumented) fastrw format
// generates same output as fast_write(Stream,Term) after use_module(library(fastrw)).
// and can be read using fast_read(Stream,Term)

public final class FastReadWriter {

	private final Map<String, Integer> varnums = new HashMap<>();
	private final OutputStream out;

	public FastReadWriter(OutputStream out) {
		this.out = out;
	}

	public void fastwrite(PrologTerm term) throws IOException {
		out.write('D');
		writeTerm(term);
	}

	private void writeTerm(PrologTerm term) throws IOException {
		if (term instanceof AIntegerPrologTerm) {
			writeInteger((AIntegerPrologTerm) term);
		} else if (term instanceof FloatPrologTerm) {
			writeFloat((FloatPrologTerm) term);
		} else if (term instanceof CompoundPrologTerm) {
			writeCompound((CompoundPrologTerm) term);
		} else if (term instanceof ListPrologTerm) {
			writeList((ListPrologTerm) term);
		} else if (term instanceof VariablePrologTerm) {
			writeVariable((VariablePrologTerm) term);
		} else {
			throw new IllegalArgumentException("Illegal Prolog term for writeTerm");
		}
	}

	private void writeList(ListPrologTerm lp) throws IOException {
		for (PrologTerm t : lp) {
			out.write('[');
			writeTerm(t);
		}
		out.write(']');
	}

	private void writeVariable(VariablePrologTerm vp) throws IOException {
		String name = getRenamedVariable(vp.getName());
		out.write('_');
		writeText(name);
		out.write(0);
	}

	private void writeInteger(AIntegerPrologTerm ip) throws IOException {
		out.write('I');
		writeText(ip.getFunctor());
		out.write(0);
	}

	private void writeFloat(FloatPrologTerm fp) throws IOException {
		throw new UnsupportedOperationException("float term not supported");
	}

	private String getRenamedVariable(String name) {
		return String.valueOf(varnums.computeIfAbsent(name, k -> varnums.size()));
	}

	private void writeCompound(CompoundPrologTerm cp) throws IOException {
		if (cp.isAtom()) {
			out.write('A');
			writeText(cp.getFunctor());
			out.write(0);
		} else {
			out.write('S');
			writeText(cp.getFunctor());
			out.write(0);
			out.write(cp.getArity());
			for (int i = 1; i <= cp.getArity(); i++) {
				PrologTerm argument = cp.getArgument(i);
				writeTerm(argument);
			}
		}
	}

	private void writeText(final String text) throws IOException {
		out.write(text.getBytes(StandardCharsets.UTF_8));
	}
}
