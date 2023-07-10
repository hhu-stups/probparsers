package de.be4.classicalb.core.parser;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import de.prob.prolog.term.AIntegerPrologTerm;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.IntegerLongPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.prolog.term.VariablePrologTerm;

// writes Prolog terms in SICStus (undocumented) fastrw format
// generates same output as fast_write(Stream,Term) after use_module(library(fastrw)).
// and can be read using fast_read(Stream,Term)

public class FastReadWriter {
	private final Map<String, String> varnums = new HashMap<String, String>();
	private final OutputStream out;

	public FastReadWriter(OutputStream out) {
		this.out = out;
	}


	public void fastwrite(PrologTerm term) throws IOException {
		out.write('D');
		writeTerm(term);
	}

	public void writeTerm(PrologTerm term) throws IOException {
		if (term instanceof IntegerLongPrologTerm) {
			IntegerLongPrologTerm intTerm = (IntegerLongPrologTerm) term;
			writeLongInteger(intTerm);
		}else if (term instanceof AIntegerPrologTerm) {
			AIntegerPrologTerm intTerm = (AIntegerPrologTerm) term;
			writeInteger(intTerm);
		} else if (term instanceof CompoundPrologTerm) {
			writeCompound(term);
		} else if (term instanceof ListPrologTerm) {
			//ListPrologTerm list = (ListPrologTerm) term; writeList(list);
			writeList( (ListPrologTerm) term);
		} else if (term instanceof VariablePrologTerm) {
			writeVariable((VariablePrologTerm) term);
		} else {
			throw new IllegalArgumentException("Illegal Prolog term for writeTerm"); 
		}
	}

	private void writeList(ListPrologTerm lp) throws IOException {
		for (ListIterator<PrologTerm> i = lp.listIterator(); i.hasNext();) {
			out.write('[');
			writeTerm(i.next());
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
		writeText(ip.getValue().toString());
		out.write(0);
	}
	
	private void writeLongInteger(IntegerLongPrologTerm ip) throws IOException {
		out.write('I');
		writeText(String.valueOf(ip.longValueExact()));
		out.write(0);
	}

	private String getRenamedVariable(String name) {
		if (!varnums.containsKey(name)) {
			String newnum = String.valueOf(varnums.size());
			varnums.put(name, newnum);
		}
		return varnums.get(name);
	}

	private void writeCompound(PrologTerm cp) throws IOException {
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
			// TODO: we could implement tail-recursion and iterate on the last argument
			// However, as conjuncts nest usually in the left argument this does not help a lot
		}
	}

	private void writeText(final String text) throws IOException {
		out.write(text.getBytes(StandardCharsets.UTF_8));
	}
}
