package com.github.jazzschmidt

import java.util.function.Consumer

class DockerRegistryExtension {

    private List<DockerRegistry> registries = []
    private List<Consumer<DockerRegistry>> createHandlers = []

    void uploadTo(String name, String url) {
        def registry = new DockerRegistry(name: name, url: url)
        registries.add(registry)
        createHandlers.each { it.accept(registry) }
    }

    void onCreate(Consumer<DockerRegistry> cl) {
        createHandlers.add(cl)
    }

    static class DockerRegistry {
        String name, url
    }

}
