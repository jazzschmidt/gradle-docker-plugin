package com.github.jazzschmidt

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException

abstract class DockerTask extends DefaultTask {

    {
        group = 'docker'
    }

    private boolean showCommand = false
    private boolean showOutput = false

    protected void execute(String args) {
        if (showCommand)
            println "docker $args"

        def cmd = "docker $args".execute(null, project.projectDir)
        def stdErr = ""

        if (showOutput) {
            cmd.in.eachLine {
                println it
            }

            cmd.err.eachLine {
                stdErr += "$it\n"
                println it
            }
        } else {
            stdErr = cmd.err.text
        }

        if (cmd.waitFor() != 0) {
            throw new GradleException(stdErr)
        }
    }

    void showCommand(boolean showCommand = true) {
        this.showCommand = showCommand
    }

    void showOutput(boolean showOutput = true) {
        this.showOutput = showOutput
    }

}
