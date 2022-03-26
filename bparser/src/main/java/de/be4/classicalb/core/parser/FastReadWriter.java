package de.be4.classicalb.core.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ListIterator;
import java.io.PrintWriter;

import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.IntegerPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.prolog.term.VariablePrologTerm;

// In contrast to FastReadTransformer this class writes directly to an output file
// and thus avoiding building intermediate terms
// writes Prolog terms in SICStus (undocumented) fastrw format
// generates same output as fast_write(Stream,Term) after use_module(library(fastrw)).
// and can be read using fast_read(Stream,Term)

public class FastReadWriter {

	public final static char ZERO = (char) 0;
	private final Map<String, String> varnums = new HashMap<String, String>();
	private final PrintWriter out;

	public FastReadWriter(PrintWriter out) {
		this.out = out;
	}


	public void fastwrite(PrologTerm term) {
	     out.print('D');
	     writeTerm(term);
	}

	public void writeTerm(PrologTerm term) {
		if (term instanceof IntegerPrologTerm) {
			IntegerPrologTerm intTerm = (IntegerPrologTerm) term;
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

	private void writeList(ListPrologTerm lp) {
        for(ListIterator<PrologTerm> i = lp.listIterator(); i.hasNext();){
			out.print('[');
			writeTerm(i.next());
        }
		out.print(']');
	}

	private void writeVariable(VariablePrologTerm vp) {
		String name = getRenamedVariable(vp.getName());
		out.print("_");
		out.print(name);
		out.print(ZERO);
	}

	private void writeInteger(IntegerPrologTerm ip) {
		out.print("I");
		out.print(ip.getValue());
		out.print(ZERO);
	}

	private String getRenamedVariable(String name) {
		if (!varnums.containsKey(name)) {
			String newnum = String.valueOf(varnums.size());
			varnums.put(name, newnum);
		}
		return varnums.get(name);
	}

	private void writeCompound(PrologTerm cp) {
		if (cp.isAtom()) {
			out.print("A");
			out.print(cp.getFunctor());
			out.print(ZERO);
		} else {
			out.print("S");
			out.print(cp.getFunctor());
			out.print(ZERO);
			out.print((char) cp.getArity());
			for (int i = 1; i <= cp.getArity(); i++) {
				PrologTerm argument = cp.getArgument(i);
				writeTerm(argument);
			}
			// TODO: we could implement tail-recursion and iterate on the last argument
			// However, as conjuncts nest usually in the left argument this does not help a lot
		}
	}

}
