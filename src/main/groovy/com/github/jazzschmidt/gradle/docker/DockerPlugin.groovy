package com.github.jazzschmidt.gradle.docker


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

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
        imageSpec.name = project.name.toLowerCase()

        def assemble = project.tasks.register("dockerAssemble", Copy) {
            group = 'docker'

            with(imageSpec)
            into("${project.buildDir}/docker")

            doFirst {
                project.file("${project.buildDir}/docker").deleteDir()
            }
        }

        def build = project.tasks.register("docker", DockerBuildTask) {
            dependsOn assemble
            inputDir = project.file("${project.buildDir}/docker")
            image = imageSpec.name
        }

        project.tasks.register("dockerClean", DockerCleanTask)

        project.tasks.register("dockerTag") {
            group = 'docker'
            dependsOn build
        }

        project.tasks.register("dockerPush") {
            group = 'docker'
        }

        registry.onCreate { registry ->
            imageSpec.tag(registry.name, "${registry.url}/${imageSpec.name}")
        }

        imageSpec.onCreate { tag ->
            def id = tag.name.capitalize()
            def tagTask = project.tasks.register("dockerTag${id}", DockerTagTask) {
                dependsOn build
                targetTag = tag.tag
            }

            def pushTagTask = project.tasks.register("dockerPush${id}", DockerPushTask) {
                dependsOn tagTask
                setTag(tag.tag)
            }

            project.tasks.getByName("dockerTag").dependsOn(tagTask)
            project.tasks.getByName("dockerPush").dependsOn(pushTagTask)
        }
    }

    private static boolean isDockerizedProject(Project p) {
        p.file("docker").isDirectory()
    }
}
