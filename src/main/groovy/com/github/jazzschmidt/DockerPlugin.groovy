package com.github.jazzschmidt

import org.gradle.api.Plugin
import org.gradle.api.Project

class DockerPlugin implements Plugin<Project> {

    private DockerRegistryExtension registry

    @Override
    void apply(Project project) {
        // Add registry extension for declaring tag targets
        registry = project.extensions.create("dockerRegistry", DockerRegistryExtension)

        // Configure all projects that have a "docker" directory
        project.allprojects
                .findAll { isDockerizedProject(it) }
                .each { configureDocker(it) }
    }

    private void configureDocker(Project project) {
        def imageSpec = project.extensions.create("dockerImage", DockerImageSpec)
        imageSpec.from(project.file("docker"))
        imageSpec.name = project.name

        def assemble = project.task("dockerAssemble", type: DockerAssembleTask)

        def build = project.task("docker", type: DockerBuildTask) {
            dependsOn assemble
        }

        project.task("dockerClean", type: DockerCleanTask)

        project.task("dockerTag", type: DockerTagTask) {
            dependsOn build
            targetTag = "${imageSpec.name}:${-> project.version}"
        }

        def pushTask = project.task("dockerPush", group: 'docker')

        registry.onCreate { registry ->
            imageSpec.tag(registry.name, "${registry.url}/${imageSpec.name}:${-> project.version}")
        }

        imageSpec.onCreate { tag ->
            def id = tag.name.capitalize()
            def tagTask = project.task("dockerTag${id}", type: DockerTagTask) {
                dependsOn build
                targetTag = tag.tag
            }

            def pushTagTask = project.task("dockerPush${id}", type: DockerPushTask) {
                dependsOn tagTask
                setTag(tag.tag)
            }
            pushTask.dependsOn(pushTagTask)
        }
    }

    private static boolean isDockerizedProject(Project p) {
        p.file("docker").isDirectory()
    }
}
