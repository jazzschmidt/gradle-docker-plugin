# Gradle Docker Plugin
[![CI](https://github.com/jazzschmidt/gradle-docker-plugin/actions/workflows/test.yml/badge.svg?branch=main)](https://github.com/jazzschmidt/gradle-docker-plugin/actions/workflows/test.yml)
<a href="https://plugins.gradle.org/plugin/com.github.jazzschmidt.gradle-docker-plugin"><img src="https://img.shields.io/badge/Gradle%20Plugin-1.0.0-brightgreen" /></a>

Simple to use Gradle Plugin to _build, tag_ and _push_ Docker Images.
Supports Multi-Module-Projects, build caching and runs on Java 8. 

## Usage

Apply the plugin as you're used to do:
```groovy
plugins {
    id 'com.github.jazzschmidt.gradle-docker-plugin' version '<version>'
}
```

> All projects that have a `docker` directory will be configured with the
> docker  tasks.

Any file in the `docker` directory will be used to assemble the Docker Image. 
The plugin adds a `dockerImage` extension, that can be used to add further
dependencies to the Docker build and define custom tags. This configuration
will add the tasks `dockerTagReg` and `dockerTagRegLatest` and corresponding
`dockerPushX` tasks.

```groovy
dockerImage {
    from(jar)
    
    tag "reg", "registry/${project.name}" // Omit the version to use project version
    tag "regLatest", "registry/${project.name}:latest"
}
```

Also, a `dockerRegistry` extension is available at the root project, that
conveniently adds tag configurations to the dockerized projects.

```groovy
// root project

dockerRegistry {
    uploadTo("reg", "my-registry.com")
}
```
