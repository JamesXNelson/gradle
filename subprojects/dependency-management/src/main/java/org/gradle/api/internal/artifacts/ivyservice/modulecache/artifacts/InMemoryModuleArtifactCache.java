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
package org.gradle.api.internal.artifacts.ivyservice.modulecache.artifacts;

import org.gradle.api.internal.artifacts.ivyservice.CacheLockingManager;
import org.gradle.util.BuildCommencedTimeProvider;

import java.io.File;
import java.math.BigInteger;
import java.util.List;

public class InMemoryModuleArtifactCache extends DefaultModuleArtifactCache {
    public InMemoryModuleArtifactCache(String persistentCacheFile, BuildCommencedTimeProvider timeProvider, CacheLockingManager cacheLockingManager) {
        super(persistentCacheFile, timeProvider, cacheLockingManager);
    }

    @Override
    public void store(ArtifactAtRepositoryKey key, File artifactFile, BigInteger moduleDescriptorHash) {
        inMemoryCache.put(key, createEntry(artifactFile, moduleDescriptorHash));
    }

    @Override
    public void storeMissing(ArtifactAtRepositoryKey key, List<String> attemptedLocations, BigInteger descriptorHash) {
        inMemoryCache.put(key, createMissingEntry(attemptedLocations, descriptorHash));
    }

    @Override
    public CachedArtifact lookup(ArtifactAtRepositoryKey key) {
        return inMemoryCache.get(key);
    }

    @Override
    public void clear(ArtifactAtRepositoryKey key) {
        inMemoryCache.remove(key);
    }
}
