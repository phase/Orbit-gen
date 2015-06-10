package solar.dimensions.orbit.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project
import org.gradle.api.Task;

public class OrbitPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        Task orbitTask = project.tasks.create('orbit', ApigenTask)

        def compileTask = project.tasks['compileJava']
        orbitTask.dependsOn(compileTask)
    }
}
