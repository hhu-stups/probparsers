package de.hhu.stups.sablecc.gradle;

import javax.inject.Inject;

import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;

@NonNullApi
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
		project.getTasks().withType(SableCCTask.class).configureEach(sableCCTask ->
			sableCCTask.getConventionMapping().map("sableCCClasspath", () -> project.getConfigurations().getByName(SABLECC_CONFIGURATION_NAME))
		);
		
		project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets().all(sourceSet -> {
			// Create the sablecc source directory set.
			final String sourceSetDisplayName = ((DefaultSourceSet)sourceSet).getDisplayName();
			final SableCCSourceSet sableCCSourceSet = new DefaultSableCCSourceSet(sourceSetDisplayName, project.getObjects());
			sourceSet.getExtensions().add(SableCCSourceSet.class, SableCCSourceSet.NAME, sableCCSourceSet);
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
			project.getTasks().register(sableCCTaskName, SableCCTask.class, task -> {
				task.setDescription("Compiles the " + sourceSetDisplayName + " SableCC grammars.");
				task.setSource(sableCCSourceSet.getSableCC());
				task.setDestinationJavaDir(outputJavaDirectory);
				task.setDestinationResourcesDir(outputResourcesDirectory);
			});
			
			// Make compileJava and processResources depend on generateSableCCSource.
			project.getTasks().named(sourceSet.getCompileJavaTaskName(), task -> task.dependsOn(sableCCTaskName));
			project.getTasks().named(sourceSet.getProcessResourcesTaskName(), task -> task.dependsOn(sableCCTaskName));
		});
	}
}
