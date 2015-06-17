package solar.dimensions.orbit.test;

import org.junit.*
import org.gradle.api.*
import org.gradle.testfixtures.*

class OrbitPluginTest {
    @Test
    public void pluginCreatesFiles() {
        Project project = ProjectBuilder.builder().withProjectDir(new File(testDir, "example")).build() //Build project with Example.java
        project.pluginManager.apply 'solar.dimensions.orbit' //Apply the plugin
        assertTrue(new File(project.getRootDir(), "src/main/java/solar/dimensions/api").exists()); //Does the file exist?
    }
}
