package com.github.jazzschmidt


import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

class DockerBuildTask extends DockerTask {

    {
        outputs.upToDateWhen {
            "docker inspect --type image -f\"--\" ${image}".execute().waitFor() == 0
        }
    }

    @Input
    String image

    @InputDirectory
    File inputDir

    @TaskAction
    void build() {
        execute "build -t ${image} -t ${image}:${project.version} ${inputDir}"
    }

}
