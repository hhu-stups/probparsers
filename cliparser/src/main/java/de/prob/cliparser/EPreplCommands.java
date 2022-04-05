package de.prob.cliparser;

public enum EPreplCommands {
	machine, formula, expression, predicate, substitution, 
	extendedexpression, extendedpredicate, extendedsubstitution,
	halt, 
	definition, resetdefinitions,
	ltl, ctl,
	version, gitsha, shortversion,
	extendedformula,
	
	// new commands to change parsingBehaviour, analog to command-line switches		
	verbose,
	fastprolog, compactpos, checkname, lineno;
}
