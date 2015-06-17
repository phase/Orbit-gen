package solar.dimensions.orbit.test;

import org.junit.*
import org.gradle.api.*
import org.gradle.testfixtures.*

class OrbitPluginTest {
    @Rule
    public TemporaryFolder myfolder = new TemporaryFolder();

    @Test
    public void pluginCreatesFiles() {
        def projectDir = new File(myfolder.newFolder("folder"), "example")
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build() //Build project with Example.java
        project.pluginManager.apply 'solar.dimensions.orbit' //Apply the plugin
        assertTrue(new File(project.getRootDir(), "src/main/java/solar/dimensions/api").exists()); //Does the file exist?
    }
}
