package de.hhu.stups.sablecc.gradle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.JavaExecSpec;

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
	
	@Classpath
	public FileCollection getSableCCClasspath() {
		return this.sableCCClasspath;
	}
	
	public void setSableCCClasspath(final FileCollection sableCCClasspath) {
		this.sableCCClasspath = sableCCClasspath;
	}
	
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
		//this.getProject().delete(this.getDestinationJavaDir(), this.getDestinationResourcesDir());
		//this.getProject().mkdir(this.getDestinationJavaDir());
		//this.getProject().mkdir(this.getDestinationResourcesDir());
		
		// Call SableCC to generate the source files.
		this.getProject().javaexec(new Action<JavaExecSpec>() {
			@Override
			public void execute(final JavaExecSpec spec) {
				spec.setClasspath(SableCCTask.this.getSableCCClasspath());
				spec.setMaxHeapSize(SableCCTask.this.getMaxHeapSize());
				spec.setMain("org.sablecc.sablecc.SableCC");
				final List<String> args = new ArrayList<>();
				args.add("-d");
				args.add(destinationJavaDir.get().getAsFile().getPath());
				for (final File file : SableCCTask.this.getSource().getFiles()) {
					args.add(file.getPath());
				}
				spec.setArgs(args);
			}
		});
		
		// Move generated dat files from Java source directory to resources directory.
		this.getProject().copy(new Action<CopySpec>() {
			@Override
			public void execute(final CopySpec spec) {
				spec.from(SableCCTask.this.getDestinationJavaDir());
				spec.into(SableCCTask.this.getDestinationResourcesDir());
				spec.include("**/*.dat");
			}
		});
		this.getProject().delete(this.getProject().fileTree(this.getDestinationJavaDir(), new Action<ConfigurableFileTree>() {
			@Override
			public void execute(final ConfigurableFileTree files) {
				files.include("**/*.dat");
			}
		}));
	}
}
