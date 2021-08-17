package com.github.jazzschmidt

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerCleanTask extends DockerTask {

    @Input
    DockerImageSpec imageSpec = project.extensions.getByType(DockerImageSpec)

    @TaskAction
    void clean() {
        List<String> tags = project.tasks.withType(DockerTagTask).collect { it.targetTag }
        execute "rmi ${imageSpec.name} ${tags.join(' ')}"
        project.file("${project.buildDir}/docker").deleteDir()
    }

}
