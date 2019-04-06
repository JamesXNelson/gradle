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

package org.gradle.api.internal.component;

import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.component.SoftwareComponent;

import javax.annotation.Nullable;

/**
 * Created by James X. Nelson (James@WeTheInter.net) on 3/1/19 @ 5:28 AM.
 */
public interface MultiCapabilitySoftwareComponent extends SoftwareComponent {
    @Nullable
    ModuleVersionIdentifier findCapabilityForConfiguration(ModuleVersionIdentifier candidate, String configurationName);
}
