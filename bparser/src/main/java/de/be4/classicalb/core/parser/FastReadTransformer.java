package de.be4.classicalb.core.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ListIterator;

import de.prob.prolog.output.StructuredPrologOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.IntegerPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.prolog.term.VariablePrologTerm;

// writes Prolog terms in SICStus (undocumented) fastrw format
// generates same output as fast_write(Stream,Term) after use_module(library(fastrw)).
// and can be read using fast_read(Stream,Term)

public class FastReadTransformer {

	private static final String EMPTY_MSG = "Cannot FastRead empty sentences.";
	private static final String MULTI_MSG = "Cannot FastRead multiple sentences.";
	public final static char ZERO = (char) 0;
	private final StringBuilder sb = new StringBuilder("D");
	private final Map<String, String> varnums = new HashMap<String, String>();
	private final StructuredPrologOutput spo;

	public FastReadTransformer(StructuredPrologOutput spo) {
		this.spo = spo;
	}

	public String write() {
		Collection<PrologTerm> sentences = spo.getSentences();
		if (sentences.isEmpty())
			throw new IllegalArgumentException(EMPTY_MSG);
		if (sentences.size() > 1)
			throw new IllegalArgumentException(MULTI_MSG);
		PrologTerm term = sentences.iterator().next();
		fastwrite(term);
		return sb.toString();
	}

	private void fastwrite(PrologTerm term) {
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
			throw new IllegalArgumentException("Illegal Prolog term for fastwrite"); 
		}
	}

	private void writeList(ListPrologTerm lp) {
		for (ListIterator<PrologTerm> i = lp.listIterator(); i.hasNext();) {
			sb.append('[');
			fastwrite(i.next());
		}
		sb.append(']');
	}

	private void writeVariable(VariablePrologTerm vp) {
		String name = getRenamedVariable(vp.getName());
		sb.append("_");
		sb.append(name);
		sb.append(ZERO);
	}

	private void writeInteger(IntegerPrologTerm ip) {
		sb.append("I");
		sb.append(ip.getValue());
		sb.append(ZERO);
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
			sb.append("A");
			sb.append(cp.getFunctor());
			sb.append(ZERO);
		} else {
			sb.append("S");
			sb.append(cp.getFunctor());
			sb.append(ZERO);
			sb.append((char) cp.getArity());
			for (int i = 1; i <= cp.getArity(); i++) {
				PrologTerm argument = cp.getArgument(i);
				fastwrite(argument);
			}
		}
	}

}
