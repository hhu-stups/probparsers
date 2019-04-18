package de.hhu.stups.sablecc.gradle;

import groovy.lang.Closure;

import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;

public interface SableCCSourceSet {
	String NAME = "sablecc";
	
	SourceDirectorySet getSableCC();
	SableCCSourceSet sableCC(final Closure<?> configureClosure);
	SableCCSourceSet sableCC(final Action<? super SourceDirectorySet> configureAction);
}
