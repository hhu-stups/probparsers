package de.hhu.stups.sablecc.gradle;

import org.gradle.api.Action;
import org.gradle.api.NonNullApi;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

@NonNullApi
final class DefaultSableCCSourceSet implements SableCCSourceSet, HasPublicType {
	private final SourceDirectorySet sableCC;
	
	DefaultSableCCSourceSet(final String displayName, final ObjectFactory objectFactory) {
		super();
		
		this.sableCC = objectFactory.sourceDirectorySet(NAME, displayName + " SableCC source");
		this.sableCC.getFilter().include("**/*.scc");
		this.sableCC.getFilter().include("**/*.sablecc");
	}
	
	@Override
	public SourceDirectorySet getSableCC() {
		return this.sableCC;
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
