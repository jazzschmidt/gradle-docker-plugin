package com.github.jazzschmidt

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerTagTask extends DockerTask {

    {
        showOutput true
        showCommand true
    }

    @Input
    String targetTag

    private DockerImageSpec imageSpec = project.extensions.getByType(DockerImageSpec)

    @TaskAction
    void tag() {
        execute "tag ${imageSpec.name} ${targetTag}"
    }

}
