package com.github.jazzschmidt

import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class DockerPluginIntegrationTest extends Specification {

    public static final String projectName = 'test-project-gdp'

    TemporaryFolder folder = new TemporaryFolder()

    File settingsFile
    File buildFile

    def setup() {
        folder.create()
        settingsFile = folder.newFile('settings.gradle')
        buildFile = folder.newFile('build.gradle')
    }

    def teardown() {
        folder.delete()
    }

    def 'builds and tags docker images'() {
        given:
        createProject(projectName) {
            """\
            plugins {
                id 'com.github.jazzschmidt.gradle-docker-plugin'
            }
            
            dockerRegistry {
                uploadTo("ECR", "amazonaws.ecr.com")
            }
            """.stripIndent()
        }

        createSubproject('dockerized') {
            """\
            version '1.2.3'
            
            dockerImage {
                tag "test", "docker-repo.com/dockerized"
            }
            """.stripIndent()
        }
        folder.newFolder("dockerized/docker")
        def file = folder.newFile("dockerized/docker/Dockerfile")
        createDockerfile(file)

        createSubproject('plain') {
            """\
            version '0.0.1'
            """
        }

        when:
        build("dockerTag")

        then:
        new File("${folder.root}/dockerized/docker/Dockerfile").exists()
        !new File("${folder.root}/plain/docker").exists()

        and:
        with("docker images".execute().text) {
            it.contains("\ndockerized ")
            it.contains("docker-repo.com/dockerized")
            it.contains("amazonaws.ecr.com/dockerized")
        }
        build("dockerClean")
        with("docker images".execute().text) {
            !it.contains("\ndockerized ")
            !it.contains("docker-repo.com/dockerized")
            !it.contains("amazonaws.ecr.com/dockerized")
        }
    }

    private static void createDockerfile(File file, Closure<String> cl = null) {
        file.text = cl ? cl() : """\
        FROM alpine:latest
        
        ENTRYPOINT sh
        """.stripIndent()
    }

    void createProject(String name = 'test-project', Closure<String> cl) {
        settingsFile << "rootProject.name = '$name'\n"
        buildFile << cl()
    }

    void createSubproject(String name, Closure<String> cl) {
        folder.newFolder(name)
        settingsFile << "include(':$name')\n"
        folder.newFile("$name/build.gradle") << cl()
    }

    def build(String... args) {
        createRunner()
                .withArguments(args)
                .build()
    }

    def buildAndFail(String... args) {
        createRunner()
                .withArguments(args)
                .buildAndFail()
    }

    def createRunner() {
        GradleRunner.create()
                .withProjectDir(folder.root)
                .withPluginClasspath()
                .withDebug(true)
                .withArguments("--stacktrace")
    }
}
