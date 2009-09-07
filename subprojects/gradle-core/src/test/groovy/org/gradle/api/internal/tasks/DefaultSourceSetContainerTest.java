/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.internal.tasks;

import org.gradle.api.internal.IConventionAware;
import org.gradle.api.tasks.SourceSet;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Test;

public class DefaultSourceSetContainerTest {
    private final DefaultSourceSetContainer container = new DefaultSourceSetContainer(null);

    @Test
    public void createsASourceSet() {
        SourceSet set = container.create("main");
        assertThat(set, instanceOf(DefaultSourceSet.class));
        assertThat(set, instanceOf(IConventionAware.class));
        assertThat(set.getName(), equalTo("main"));
    }
}
