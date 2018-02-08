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
package org.gradle.api.internal.artifacts.ivyservice.modulecache;

import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.internal.artifacts.ImmutableModuleIdentifierFactory;
import org.gradle.api.internal.artifacts.ivyservice.ArtifactCacheMetadata;
import org.gradle.api.internal.artifacts.ivyservice.CacheLockingManager;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.ModuleComponentRepository;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.result.AttributeContainerSerializer;
import org.gradle.api.internal.artifacts.repositories.metadata.IvyMutableModuleMetadataFactory;
import org.gradle.api.internal.artifacts.repositories.metadata.MavenMutableModuleMetadataFactory;
import org.gradle.internal.component.external.model.ModuleComponentResolveMetadata;
import org.gradle.util.BuildCommencedTimeProvider;

public class InMemoryModuleMetadataCache extends DefaultModuleMetadataCache {
    public InMemoryModuleMetadataCache(BuildCommencedTimeProvider timeProvider, CacheLockingManager cacheLockingManager, ArtifactCacheMetadata artifactCacheMetadata, ImmutableModuleIdentifierFactory moduleIdentifierFactory, AttributeContainerSerializer attributeContainerSerializer, MavenMutableModuleMetadataFactory mavenMetadataFactory, IvyMutableModuleMetadataFactory ivyMetadataFactory) {
        super(timeProvider, cacheLockingManager, artifactCacheMetadata, moduleIdentifierFactory, attributeContainerSerializer, mavenMetadataFactory, ivyMetadataFactory);
    }

    @Override
    public CachedMetadata cacheMissing(ModuleComponentRepository repository, ModuleComponentIdentifier id) {
        final ModuleComponentAtRepositoryKey key = createKey(repository, id);
        ModuleMetadataCacheEntry entry = ModuleMetadataCacheEntry.forMissingModule(timeProvider.getCurrentTime());
        DefaultCachedMetadata cachedMetaData = new DefaultCachedMetadata(entry, null, timeProvider);
        inMemoryCache.put(key, cachedMetaData);
        return cachedMetaData;
    }

    @Override
    public CachedMetadata cacheMetaData(ModuleComponentRepository repository, ModuleComponentIdentifier id, ModuleComponentResolveMetadata metadata) {
        ModuleComponentAtRepositoryKey key = createKey(repository, id);
        ModuleMetadataCacheEntry entry = createEntry(metadata);
        DefaultCachedMetadata cachedMetaData = new DefaultCachedMetadata(entry, metadata, timeProvider);
        inMemoryCache.put(key, cachedMetaData);
        return cachedMetaData;
    }

    @Override
    public CachedMetadata getCachedModuleDescriptor(ModuleComponentRepository repository, ModuleComponentIdentifier id) {
        final ModuleComponentAtRepositoryKey key = createKey(repository, id);
        return inMemoryCache.get(key);
    }
}
