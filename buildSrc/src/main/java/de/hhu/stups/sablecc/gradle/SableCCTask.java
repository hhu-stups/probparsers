package de.hhu.stups.sablecc.gradle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.NonNullApi;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.IgnoreEmptyDirectories;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.NormalizeLineEndings;

@NonNullApi
public class SableCCTask extends SourceTask {
	private FileCollection sableCCClasspath;
	private String maxHeapSize;
	private final DirectoryProperty destinationJavaDir;
	private final DirectoryProperty destinationResourcesDir;
	
	public SableCCTask() {
		super();
		
		this.sableCCClasspath = null;
		this.destinationJavaDir = this.getProject().getObjects().directoryProperty();
		this.destinationResourcesDir = this.getProject().getObjects().directoryProperty();
	}
	
	@IgnoreEmptyDirectories
	@NormalizeLineEndings
	@PathSensitive(PathSensitivity.NONE)
	@Override
	public FileTree getSource() {
		return super.getSource();
	}
	
	@Classpath
	public FileCollection getSableCCClasspath() {
		return this.sableCCClasspath;
	}
	
	public void setSableCCClasspath(final FileCollection sableCCClasspath) {
		this.sableCCClasspath = sableCCClasspath;
	}
	
	@Input
	@Optional
	public String getMaxHeapSize() {
		return this.maxHeapSize;
	}
	
	public void setMaxHeapSize(final String maxHeapSize) {
		this.maxHeapSize = maxHeapSize;
	}
	
	@OutputDirectory
	public Directory getDestinationJavaDir() {
		return this.destinationJavaDir.get();
	}
	
	public void setDestinationJavaDir(final Directory destinationJavaDir) {
		this.destinationJavaDir.set(destinationJavaDir);
	}
	
	@OutputDirectory
	public Directory getDestinationResourcesDir() {
		return this.destinationResourcesDir.get();
	}
	
	public void setDestinationResourcesDir(final Directory destinationResourcesDir) {
		this.destinationResourcesDir.set(destinationResourcesDir);
	}
	
	@TaskAction
	void execute() {
		// Delete any previously generated source files, so that no longer existing token and node classes aren't kept around.
		this.getProject().delete(this.getDestinationJavaDir(), this.getDestinationResourcesDir());
		this.getProject().mkdir(this.getDestinationJavaDir());
		this.getProject().mkdir(this.getDestinationResourcesDir());
		
		// Call SableCC to generate the source files.
		this.getProject().javaexec(spec -> {
			spec.setClasspath(this.getSableCCClasspath());
			spec.setMaxHeapSize(this.getMaxHeapSize());
			spec.getMainClass().set("org.sablecc.sablecc.SableCC");
			final List<String> args = new ArrayList<>();
			args.add("-d");
			args.add(destinationJavaDir.get().getAsFile().getPath());
			for (final File file : this.getSource().getFiles()) {
				args.add(file.getPath());
			}
			spec.setArgs(args);
		});
		
		// Move generated dat files from Java source directory to resources directory.
		this.getProject().copy(spec -> {
			spec.from(this.getDestinationJavaDir());
			spec.into(this.getDestinationResourcesDir());
			spec.include("**/*.dat");
		});
		this.getProject().delete(this.getProject().fileTree(this.getDestinationJavaDir(), files -> files.include("**/*.dat")));
	}
}
