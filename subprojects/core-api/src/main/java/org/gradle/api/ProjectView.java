/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.api;

import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.ArtifactHandler;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;

/**
 * Created by James X. Nelson (James@WeTheInter.net) on 12/31/18 @ 12:40 AM.
 */
public interface ProjectView {

    String getPath();
    ObjectFactory getObjects();
    ProviderFactory getProviders();
    Object findProperty(String name);
    Object property(String name);
    Logger getLogger();
    TaskContainer getTasks();
    ConfigurationContainer getConfigurations();
    RepositoryHandler getRepositories();
    DependencyHandler getDependencies();
    ArtifactHandler getArtifacts();
}
