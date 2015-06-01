package org.quartzpowered.apigen.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project
import org.gradle.api.Task;

public class ApigenPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        Task apigenTask = project.tasks.create('apigen', ApigenTask)

        def compileTask = project.tasks['compileJava']
        apigenTask.dependsOn(compileTask)
    }
}
