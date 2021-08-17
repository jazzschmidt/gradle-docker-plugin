package com.github.jazzschmidt

import org.gradle.api.Project
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder as projectBuilder

class DockerPluginTest extends Specification {

    public static final String projectName = 'test-project-gdp'

    TemporaryFolder folder = new TemporaryFolder()
    Project project

    def setup() {
        folder.create()
        project = projectBuilder()
                .withName(projectName)
                .withProjectDir(folder.getRoot())
                .build().tap {
            version = '1.0.0'
        }
    }

    def applyPlugin() {
        project.plugins.apply(DockerPlugin)
    }

    def teardown() {
        folder.delete()
    }

    def 'configures projects with docker directory'() {
        given:
        def dockerizedProject = createSubproject('docker') { version = '1.0.1' }
        def plainProject = createSubproject('plain') { version = '1.0.0' }

        and:
        dockerizedProject.file("docker").mkdirs()

        when:
        applyPlugin()

        then:
        DockerImageSpec image = dockerizedProject.extensions.findByName("dockerImage") as DockerImageSpec
        image.name == 'docker'
        plainProject.extensions.findByName("dockerImage") == null

        and:
        dockerizedProject.tasks.findByName("dockerAssemble") != null
        dockerizedProject.tasks.findByName("docker") != null
    }

    def 'assembles docker image'() {
        given:
        project.file("docker").mkdir()
        createDockerfile(project)

        when:
        applyPlugin()
        DockerAssembleTask assembleTask = project.tasks.getByName("dockerAssemble") as DockerAssembleTask

        and:
        assembleTask.assemble()

        then:
        noExceptionThrown()
        project.file("build/docker/Dockerfile").exists()
    }

    def 'builds docker image'() {
        given:
        createDockerfile(project)

        when:
        applyPlugin()
        DockerAssembleTask assembleTask = project.tasks.getByName("dockerAssemble") as DockerAssembleTask
        DockerBuildTask buildTask = project.tasks.getByName("docker") as DockerBuildTask

        and:
        assembleTask.assemble()
        buildTask.build()

        then:
        noExceptionThrown()
    }

    def 'tags docker image'() {
        given:
        createDockerfile(project)

        when:
        applyPlugin()

        and:
        project.extensions.getByType(DockerRegistryExtension).with {
            uploadTo("test", "docker-repo.com")
        }

        and:
        DockerAssembleTask assembleTask = project.tasks.getByName("dockerAssemble") as DockerAssembleTask
        DockerBuildTask buildTask = project.tasks.getByName("docker") as DockerBuildTask
        DockerTagTask tagTask = project.tasks.getByName("dockerTag") as DockerTagTask
        DockerTagTask tagRepoTask = project.tasks.getByName("dockerTagTest") as DockerTagTask
        DockerCleanTask cleanTask = project.tasks.getByName("dockerClean") as DockerCleanTask

        and:
        assembleTask.assemble()
        buildTask.build()
        tagTask.tag()
        tagRepoTask.tag()

        and:
        String dockerTagsOutput = "docker images".execute().text

        and:
        cleanTask.clean()

        then:
        noExceptionThrown()
        project.tasks.getByName("dockerPush") != null
        project.tasks.getByName("dockerPushTest") != null

        and:
        with(dockerTagsOutput) {
            it.contains("\n$projectName ")
            it.contains("docker-repo.com/$projectName")
        }

        with("docker images".execute().text) {
            !it.contains("\n$projectName ")
            !it.contains("docker-repo.com/$projectName")
        }
    }

    private static void createDockerfile(Project project, Closure<String> cl = null) {
        project.file("docker").mkdir()
        def dockerfile = project.file("docker/Dockerfile")

        dockerfile.createNewFile()
        dockerfile.text = cl ? cl() : """\
        FROM alpine:latest
        
        ENTRYPOINT sh
        """.stripIndent()
    }

    private Project createSubproject(String name, @DelegatesTo(type = 'org.gradle.api.Project') Closure cl) {
        projectBuilder().withParent(project).withName(name).build().tap(cl)
    }

}
