package de.prob.prolog.term;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DotListConversion {

	private static final CompoundPrologTerm EMPTY_LIST_ATOM = new CompoundPrologTerm("[]");

	private DotListConversion() {
	}

	public static PrologTerm asListTerm(PrologTerm term) {
		Objects.requireNonNull(term, "term");

		if (term.isAtom() && "[]".equals(term.getFunctor())) {
			return ListPrologTerm.emptyList();
		} else if (term.isList()) {
			ListPrologTerm listTerm = (ListPrologTerm) term;
			if (listTerm.isEmpty()) {
				return ListPrologTerm.emptyList();
			} else {
				List<PrologTerm> args = new ArrayList<>();
				for (PrologTerm arg : listTerm) {
					args.add(asListTerm(arg));
				}

				return ListPrologTerm.fromCollection(args);
			}
		} else if (!term.isAtom() && term.isTerm()) {
			List<PrologTerm> args = new ArrayList<>();
			args.add(asListTerm(term.getArgument(1)));

			String functor = term.getFunctor();
			int arity = term.getArity();
			if (arity >= 2) {
				PrologTerm tail = asListTerm(term.getArgument(2));
				if (arity == 2 && (".".equals(functor) || "[|]".equals(functor))) {
					if (tail.isList()) {
						args.addAll((ListPrologTerm) tail);
						return ListPrologTerm.fromCollection(args);
					}
				}

				args.add(tail);
				for (int i = 3; i <= arity; i++) {
					args.add(asListTerm(term.getArgument(i)));
				}
			}

			return CompoundPrologTerm.fromCollection(functor, args);
		} else {
			return term;
		}
	}

	public static PrologTerm asListTermNonRecursive(PrologTerm term) {
		Objects.requireNonNull(term, "term");

		if (term.isAtom() && "[]".equals(term.getFunctor())) {
			return ListPrologTerm.emptyList();
		}

		int arity = term.getArity();
		if (arity == 2 && term.isTerm()) {
			String functor = term.getFunctor();
			if (".".equals(functor) || "[|]".equals(functor)) {
				PrologTerm tail = term.getArgument(2);
				if (tail.isList()) {
					// assume arity = list size
					List<PrologTerm> args = new ArrayList<>(arity + 1);
					args.add(term.getArgument(1));
					args.addAll((ListPrologTerm) tail);
					return ListPrologTerm.fromCollection(args);
				}
			}
		}

		return term;
	}

	public static PrologTerm asListConcatTerm(PrologTerm term) {
		return asListConcatTerm(term, ".");
	}

	public static PrologTerm asListConcatTermSWI(PrologTerm term) {
		return asListConcatTerm(term, "[|]");
	}

	public static PrologTerm asListConcatTerm(PrologTerm term, String listConcatFunctor) {
		Objects.requireNonNull(term, "term");
		Objects.requireNonNull(listConcatFunctor, "listConcatFunctor");

		if (term.isAtom() && "[]".equals(term.getFunctor())) {
			return EMPTY_LIST_ATOM;
		} else if (term.isList()) {
			ListPrologTerm listTerm = (ListPrologTerm) term;
			PrologTerm dotTerm = EMPTY_LIST_ATOM;
			int len = listTerm.size();
			if (len > 0) {
				for (int i = len - 1; i >= 0; i--) {
					PrologTerm head = asListConcatTerm(listTerm.get(i), listConcatFunctor);
					dotTerm = new CompoundPrologTerm(listConcatFunctor, head, dotTerm);
				}
			}

			return dotTerm;
		} else if (!term.isAtom() && term.isTerm()) {
			List<PrologTerm> args = new ArrayList<>();
			for (int i = 1, arity = term.getArity(); i <= arity; i++) {
				args.add(asListConcatTerm(term.getArgument(i), listConcatFunctor));
			}

			return CompoundPrologTerm.fromCollection(term.getFunctor(), args);
		} else {
			return term;
		}
	}
}
