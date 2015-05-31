package org.quartzpowered.apigen.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ApigenPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.tasks.create('apigen', )
    }
}
