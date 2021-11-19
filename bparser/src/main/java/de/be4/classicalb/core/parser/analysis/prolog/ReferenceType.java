package de.be4.classicalb.core.parser.analysis.prolog;

public enum ReferenceType {
	SEES("sees"),
	USES("uses"),
	REFINES("refines"),
	INCLUDES("includes"),
	EXTENDS("extends"),
	IMPORTS("imports"),
	REFERENCES("references"),
	;
	
	private final String description;
	
	private ReferenceType(final String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return this.description;
	}
}
