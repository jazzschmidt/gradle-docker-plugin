package com.github.jazzschmidt

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerCleanTask extends DockerTask {

    @Input
    DockerImageSpec imageSpec = project.extensions.getByType(DockerImageSpec)

    @TaskAction
    void clean() {
        List<String> tags = project.tasks.withType(DockerTagTask).collect { it.targetTag }
        "docker rmi -f ${imageSpec.name} ${imageSpec.name}:${project.version} ${tags.join(' ')}".execute().waitFor()
        project.file("${project.buildDir}/docker").deleteDir()
    }

}
