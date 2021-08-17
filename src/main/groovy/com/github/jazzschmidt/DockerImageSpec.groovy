package com.github.jazzschmidt

import org.gradle.api.internal.file.FileCollectionFactory
import org.gradle.api.internal.file.copy.DefaultCopySpec
import org.gradle.api.tasks.util.PatternSet
import org.gradle.internal.Factory
import org.gradle.internal.reflect.Instantiator

import javax.inject.Inject
import java.util.function.Consumer

class DockerImageSpec extends DefaultCopySpec {

    String name

    private Map<String, String> tags = [:]
    private List<Consumer<TagDefinition>> createHandlers = []

    @Inject
    DockerImageSpec(FileCollectionFactory fileCollectionFactory, Instantiator instantiator, Factory<PatternSet> patternSetFactory) {
        super(fileCollectionFactory, instantiator, patternSetFactory)
    }

    void tag(String taskId, String tag) {
        tags.put(taskId, tag)
        createHandlers.each { it.accept(new TagDefinition(name: taskId, tag: tag)) }
    }

    void onCreate(Consumer<TagDefinition> cl) {
        createHandlers.add(cl)
    }

    static class TagDefinition {
        String name, tag
    }
}
