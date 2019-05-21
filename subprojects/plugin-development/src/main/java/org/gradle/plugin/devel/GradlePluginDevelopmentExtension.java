/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.plugin.devel;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration options for the {@link org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin}.
 *
 * <p>Below is a full configuration example. Since all properties have sensible defaults,
 * typically only selected properties will be configured.
 *
 * <pre class='autoTested'>
 *     apply plugin: "java-gradle-plugin"
 *
 *     gradlePlugin {
 *         pluginSourceSet project.sourceSets.customMain
 *         testSourceSets project.sourceSets.functionalTest
 *         plugins {
 *             helloPlugin {
 *                 id  = 'org.example.hello'
 *                 implementationClass = 'org.example.HelloPlugin'
 *             }
 *         }
 *     }
 * </pre>
 *
 * @see org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin
 * @since 2.13
 */
public class GradlePluginDevelopmentExtension {

    private SourceSet pluginSourceSet;
    private Set<SourceSet> testSourceSets = Collections.emptySet();
    private final NamedDomainObjectContainer<PluginDeclaration> plugins;
    private boolean automatedPublishing = true;
    private String publishComponent = "java";

    public GradlePluginDevelopmentExtension(Project project, SourceSet pluginSourceSet, SourceSet testSourceSet) {
        this(project, pluginSourceSet, new SourceSet[] {testSourceSet});
    }

    public GradlePluginDevelopmentExtension(Project project, SourceSet pluginSourceSet, SourceSet[] testSourceSets) {
        this.plugins = project.container(PluginDeclaration.class);
        this.pluginSourceSet = pluginSourceSet;
        testSourceSets(testSourceSets);
    }

    /**
     * Provides the source set that compiles the code under test.
     *
     * @param pluginSourceSet the plugin source set
     */
    public void pluginSourceSet(SourceSet pluginSourceSet) {
        this.pluginSourceSet = pluginSourceSet;
    }

    /**
     * Provides the source sets executing the functional tests with TestKit.
     * <p>
     * Calling this method multiple times with different source sets is not additive.
     *
     * @param testSourceSets the test source sets
     */
    public void testSourceSets(SourceSet... testSourceSets) {
        this.testSourceSets = Collections.unmodifiableSet(new HashSet<SourceSet>(Arrays.asList(testSourceSets)));
    }

    /**
     * Returns the source set that compiles the code under test. Defaults to {@code project.sourceSets.main}.
     *
     * @return the plugin source set
     */
    public SourceSet getPluginSourceSet() {
        return pluginSourceSet;
    }

    /**
     * Returns the source sets executing the functional tests with TestKit. Defaults to {@code project.sourceSets.test}.
     *
     * @return the test source sets
     */
    public Set<SourceSet> getTestSourceSets() {
        return testSourceSets;
    }

    /**
     * Returns the declared plugins.
     *
     * @return the declared plugins, never null
     */
    public NamedDomainObjectContainer<PluginDeclaration> getPlugins() {
        return plugins;
    }

    /**
     * Configures the declared plugins.
     *
     * @param action the configuration action to invoke on the plugins
     */
    public void plugins(Action<? super NamedDomainObjectContainer<PluginDeclaration>> action) {
        action.execute(plugins);
    }

    /**
     * Whether the plugin should automatically configure the publications for the plugins.
     * @return true if publishing should be automated, false otherwise
     */
    public boolean isAutomatedPublishing() {
        return automatedPublishing;
    }

    /**
     * Configures whether the plugin should automatically configure the publications for the plugins.
     * @param automatedPublishing whether to automated publication
     */
    public void setAutomatedPublishing(boolean automatedPublishing) {
        this.automatedPublishing = automatedPublishing;
    }

    /**
     * Iff {@link #isAutomatedPublishing()} returns true, a plugin publication is added to the publishing component name returned here.
     * @return the publishing component where our plugin manifest is published.  Default is "java"
     */
    public String getPublishComponent() {
        return publishComponent;
    }

    /**
     * Call to change which publishing component the plugin manifest should be published to.
     * @param publishComponent the name of the SoftwareComponent where we will add a publication for the plugin's manifest
     *
     * This value is only relevant so long as the default automatedPublishing=true has not been set to false.
     */
    public void setPublishComponent(String publishComponent) {
        this.publishComponent = publishComponent;
    }
}
