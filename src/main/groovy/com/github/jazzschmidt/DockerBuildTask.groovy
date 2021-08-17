package com.github.jazzschmidt

import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

class DockerBuildTask extends DockerTask {

    {
        showOutput true
        showCommand true
    }

    @InputFiles
    DockerImageSpec imageSpec = project.extensions.getByType(DockerImageSpec)

    @TaskAction
    void build() {
        execute "build -t ${imageSpec.name} ${project.buildDir}/docker"
    }

}
