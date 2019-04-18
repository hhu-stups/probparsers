package de.hhu.stups.sablecc.gradle;

import groovy.lang.Closure;

import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.util.ConfigureUtil;

final class DefaultSableCCSourceSet implements SableCCSourceSet, HasPublicType {
	private final SourceDirectorySet sableCC;
	
	DefaultSableCCSourceSet(final String displayName, final ObjectFactory objectFactory) {
		super();
		
		this.sableCC = objectFactory.sourceDirectorySet(NAME, displayName + " SableCC source");
		this.sableCC.getFilter().include("**/*.scc");
	}
	
	@Override
	public SourceDirectorySet getSableCC() {
		return this.sableCC;
	}
	
	@Override
	public SableCCSourceSet sableCC(final Closure<?> configureClosure) {
		ConfigureUtil.configure(configureClosure, this.getSableCC());
		return this;
	}
	
	@Override
	public SableCCSourceSet sableCC(final Action<? super SourceDirectorySet> configureAction) {
		configureAction.execute(this.getSableCC());
		return this;
	}
	
	@Override
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(SableCCSourceSet.class);
	}
}
