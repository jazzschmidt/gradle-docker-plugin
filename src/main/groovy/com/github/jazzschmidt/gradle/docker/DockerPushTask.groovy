package com.github.jazzschmidt.gradle.docker

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerPushTask extends DockerTask {

    @Input
    String tag

    @TaskAction
    void push() {
        execute "push $tag"
    }
}
