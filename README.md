# Gradle Docker Plugin
[![CI](https://github.com/jazzschmidt/gradle-docker-plugin/actions/workflows/test.yml/badge.svg?branch=main)](https://github.com/jazzschmidt/gradle-docker-plugin/actions/workflows/test.yml)
<a href="https://plugins.gradle.org/plugin/com.github.jazzschmidt.gradle-docker-plugin"><img src="https://img.shields.io/badge/Gradle%20Plugin-1.0.0-brightgreen" /></a>

Simple to use Gradle Plugin to _build, tag_ and _push_ Docker Images.
Supports Multi-Module-Projects, build caching, Java 8 and runs on **Gradle 7+**. 

## Usage

Apply the plugin:
```groovy
plugins {
    id 'com.github.jazzschmidt.gradle-docker-plugin' version '<version>'
}
```

> All projects that have a `docker` directory will be configured with the
> docker  tasks.

Suppose the following project structure:

- **:root: project**
    - build.gradle
    - **:example: project**
        - docker/
            - Dockerfile
            - config.toml
        - src/main/java
            - ...
        - build.gradle
    - **:plain: project**
        - build.gradle

With the following `build.gradle`:

```groovy
// Project :root:
plugins {
    id 'com.github.jazzschmidt.gradle-docker-plugin' version '1.0.0'
}

group 'com.example'
version '0.0.1'

dockerRegistry {
    uploadTo("registry", "my-registry.com")
}
```

```groovy
// Project :example:
plugins {
    id 'java'
}

group 'com.example'
version '0.0.1'

dockerImage {
    from(jar)
    
    tag "nexus", "nexus.local/${project.name}" // Omit the version to use project version
    tag "nexusLatest", "nexus.local/${project.name}:latest"
}
```

This will generate the following tasks in **:example: project** in the `docker` group:

- `docker` - builds the Docker image
- `dockerAssemble` - assembles resources for the image in `build/docker`
- `dockerClean` - cleans the resources and all tags
- `dockerTag` - creates all tags
- `dockerTagRegistry` - tags `my-registry.com/example:0.0.1`
- `dockerTagNexus` - tags `nexus.local/example:0.0.1`
- `dockerTagNexusLatest` - tags `nexus.local/example:latest`
- `dockerPush` - pushes all tags

and tasks for pushing specific tags:

- `dockerPushRegistry`
- `dockerPushNexus`
- `dockerPushNexusLatest`

Any file in the `docker` directory will be used to assemble the Docker Image. 
The plugin adds a `dockerImage` extension, that can be used to add further
dependencies to the Docker build and define custom tags. As for the example
above, `dockerAssemble` will create something like this:

- **:example: project**
    - build/docker
        - Dockerfile
        - config.toml
        - example-0.0.1.jar
