/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.api.internal.artifacts.ivyservice.moduleconverter.dependencies;

import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.internal.component.model.LocalOriginDependencyMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by James X. Nelson (James@WeTheInter.net) on 2/19/19 @ 12:26 AM.
 */
public class PluggableDependencyDescriptorFactory extends AbstractIvyDependencyDescriptorFactory {

    private final List<IvyDependencyDescriptorFactory> factories;

    public PluggableDependencyDescriptorFactory(ExcludeRuleConverter excludeRuleConverter) {
        super(excludeRuleConverter);
        factories = new ArrayList<>();
    }

    public void registerDependencyDescriptor(IvyDependencyDescriptorFactory factory) {
        synchronized (factories) {
            factories.add(factory);
        }
    }

    @Override
    public LocalOriginDependencyMetadata createDependencyDescriptor(ComponentIdentifier componentId, String clientConfiguration, AttributeContainer attributes, ModuleDependency dependency) {
        IvyDependencyDescriptorFactory factory = findDependencyDescriptor(dependency);
        if (factory != null) {
            return factory.createDependencyDescriptor(componentId, clientConfiguration, attributes, dependency);
        }
        return null;
    }
    private IvyDependencyDescriptorFactory findDependencyDescriptor(ModuleDependency dependency) {
        final IvyDependencyDescriptorFactory[] all;
        synchronized (factories) {
            all = factories.toArray(new IvyDependencyDescriptorFactory[0]);
        }
        for (IvyDependencyDescriptorFactory factory : all) {
            if (factory.canConvert(dependency)) {
                return factory;
            }
        }
        return null;
    }

    @Override
    public boolean canConvert(ModuleDependency dependency) {
        for (IvyDependencyDescriptorFactory factory : factories) {
            if (factory.canConvert(dependency)) {
                return true;
            }
        }
        return false;
    }
}
