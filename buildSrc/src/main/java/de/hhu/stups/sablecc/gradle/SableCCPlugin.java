package de.hhu.stups.sablecc.gradle;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

public class SableCCPlugin implements Plugin<Project> {
	public static final String SABLECC_CONFIGURATION_NAME = "sableCC";
	public static final String SABLECC_TASK_VERB = "generate";
	public static final String SABLECC_TASK_TARGET = "SableCCSource";
	
	@Inject
	public SableCCPlugin() {
		super();
	}
	
	@Override
	public void apply(final Project project) {
		project.getPluginManager().apply(JavaPlugin.class);
		
		// Create the sableCC configuration (used to specify which SableCC version to use).
		project.getConfigurations().create(SABLECC_CONFIGURATION_NAME)
			.setVisible(false)
			.setDescription("The SableCC libraries to be used for this project.");
		
		// Use the sableCC configuration as the sableCCClasspath by convention (i. e. unless overridden by the user).
		project.getTasks().withType(SableCCTask.class).configureEach(new Action<SableCCTask>() {
			@Override
			public void execute(final SableCCTask sableCCTask) {
				sableCCTask.getConventionMapping().map("sableCCClasspath", new Callable<Object>() {
					@Override
					public Object call() {
						return project.getConfigurations().getByName(SABLECC_CONFIGURATION_NAME);
					}
				});
			}
		});
		
		project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(new Action<SourceSet>() {
			@Override
			public void execute(final SourceSet sourceSet) {
				// Create the sablecc source directory set.
				final String sourceSetDisplayName = ((DefaultSourceSet)sourceSet).getDisplayName();
				final SableCCSourceSet sableCCSourceSet = new DefaultSableCCSourceSet(sourceSetDisplayName, project.getObjects());
				new DslObject(sourceSet).getConvention().getPlugins().put(SableCCSourceSet.NAME, sableCCSourceSet);
				sableCCSourceSet.getSableCC().srcDir("src/" + sourceSet.getName() + "/sablecc");
				sourceSet.getAllSource().source(sableCCSourceSet.getSableCC());
				
				// Create the output directories for the generated Java source and resources.
				final Directory baseOutputDirectory = project.getLayout().getBuildDirectory().get().dir("generated-src/sablecc/" + sourceSet.getName());
				final Directory outputJavaDirectory = baseOutputDirectory.dir("java");
				sourceSet.getJava().srcDir(outputJavaDirectory);
				final Directory outputResourcesDirectory = baseOutputDirectory.dir("resources");
				sourceSet.getResources().srcDir(outputResourcesDirectory);
				
				// Create the generateSableCCSource task.
				final String sableCCTaskName = sourceSet.getTaskName(SABLECC_TASK_VERB, SABLECC_TASK_TARGET);
				project.getTasks().register(sableCCTaskName, SableCCTask.class, new Action<SableCCTask>() {
					@Override
					public void execute(final SableCCTask task) {
						task.setDescription("Compiles the " + sourceSetDisplayName + " SableCC grammars.");
						task.setSource(sableCCSourceSet.getSableCC());
						task.setDestinationJavaDir(outputJavaDirectory);
						task.setDestinationResourcesDir(outputResourcesDirectory);
					}
				});
				
				// Make compileJava and processResources depend on generateSableCCSource.
				project.getTasks().named(sourceSet.getCompileJavaTaskName(), new Action<Task>() {
					@Override
					public void execute(final Task task) {
						task.dependsOn(sableCCTaskName);
					}
				});
				project.getTasks().named(sourceSet.getProcessResourcesTaskName(), new Action<Task>() {
					@Override
					public void execute(final Task task) {
						task.dependsOn(sableCCTaskName);
					}
				});
			}
		});
	}
}
