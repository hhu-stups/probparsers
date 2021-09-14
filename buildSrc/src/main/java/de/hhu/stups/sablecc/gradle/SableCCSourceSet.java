package de.hhu.stups.sablecc.gradle;

import org.gradle.api.Action;
import org.gradle.api.NonNullApi;
import org.gradle.api.file.SourceDirectorySet;

@NonNullApi
public interface SableCCSourceSet {
	String NAME = "sablecc";
	
	SourceDirectorySet getSableCC();
	SableCCSourceSet sableCC(final Action<? super SourceDirectorySet> configureAction);
}
