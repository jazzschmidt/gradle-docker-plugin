package com.github.jazzschmidt

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class DockerAssembleTask extends DefaultTask {

    @InputFiles
    DockerImageSpec imageSpec = project.extensions.getByType(DockerImageSpec)

    @OutputDirectory
    File dockerDir = project.file("${project.buildDir}/docker")

    @TaskAction
    void assemble() {
        dockerDir.mkdirs()
        project.copy { with(imageSpec); into(dockerDir) }
    }

}
