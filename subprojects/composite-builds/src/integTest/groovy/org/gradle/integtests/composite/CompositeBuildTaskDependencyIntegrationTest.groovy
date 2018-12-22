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

package org.gradle.integtests.composite

import org.gradle.integtests.fixtures.build.BuildTestFile
import org.gradle.integtests.fixtures.build.TestInclusionMode
import spock.lang.Unroll

import static org.gradle.integtests.fixtures.build.TestInclusionMode.DEPENDENCY
import static org.gradle.integtests.fixtures.build.TestInclusionMode.DEFERRED_DEPENDENCY
import static org.gradle.integtests.fixtures.build.TestInclusionMode.WHEN_SELECTED
import static org.gradle.integtests.fixtures.build.TestInclusionMode.DEFERRED_WHEN_SELECTED

/**
 * Tests for composite build delegating to tasks in an included build.
 */
class CompositeBuildTaskDependencyIntegrationTest extends AbstractCompositeBuildIntegrationTest {
    BuildTestFile buildB

    def setup() {
        buildB = multiProjectBuild("buildB", ['b1', 'b2']) {
            buildFile << """
                allprojects {
                    task logProject {
                        doLast {
                            println "Executing build '" + project.rootProject.name + "' project '" + project.path + "' task '" + path + "'"
                        }
                    }
                }
"""
        }
        includedBuilds << buildB
    }

    BuildTestFile newBuild(String name) {
        singleProjectBuild(name) {
            buildFile.text = buildB.buildFile.text
        }
    }

    @Unroll
    def "can depend on task via #inclusion in root project of included build"() {
        when:
        switch (inclusion as TestInclusionMode) {
            case DEPENDENCY:
                buildA.buildFile << """
    task delegate {
        dependsOn gradle.includedBuild('buildB').task(':logProject')
    }
"""
                break
            case DEFERRED_DEPENDENCY:
                buildA.buildFile << """
    gradle.projectsEvaluated {
        task delegate {
            dependsOn gradle.includedBuild('buildB').task(':logProject')
        }
    }
"""
                break
            case WHEN_SELECTED:
                buildA.buildFile << """
    task delegate {
        whenSelected {
            dependsOn gradle.includedBuild('buildB').task(':logProject')
        }
    }
"""
                break
            case DEFERRED_WHEN_SELECTED:
                buildA.buildFile << """
    gradle.projectsEvaluated {
        task delegate {
            whenSelected {
                dependsOn gradle.includedBuild('buildB').task(':logProject')
            }
        }
    }
"""
                break
            default:
                throw new Error("$inclusion not handled")
        }

        execute(buildA, ":delegate")

        then:
        executed ":buildB:logProject"
        output.contains("Executing build 'buildB' project ':' task ':logProject'")

        where:
        inclusion << TestInclusionMode.values()
    }

    def "can depend on task in subproject of included build"() {
        when:
        buildA.buildFile << """
    task delegate {
        dependsOn gradle.includedBuild('buildB').task(':b1:logProject')
    }
"""

        execute(buildA, ":delegate")

        then:
        executed ":buildB:b1:logProject"
        output.contains("Executing build 'buildB' project ':b1' task ':b1:logProject'")
    }

    @Unroll
    def "can depend on multiple tasks of included build via #inclusion"() {
        when:
        switch (inclusion) {
            case DEPENDENCY:
                buildA.buildFile << """
    def buildB = gradle.includedBuild('buildB')
    task delegate {
        dependsOn 'delegate1', 'delegate2'
    }

    task delegate1 {
        dependsOn buildB.task(':logProject')
        dependsOn buildB.task(':b1:logProject')
    }

    task delegate2 {
        dependsOn buildB.task(':logProject')
    }
"""
                break
            case DEFERRED_DEPENDENCY:
                buildA.buildFile << """
    def buildB = gradle.includedBuild('buildB')
    gradle.projectsEvaluated {
        task delegate {
            dependsOn 'delegate1', 'delegate2'
        }

        task delegate1 {
            dependsOn buildB.task(':logProject')
            dependsOn buildB.task(':b1:logProject')
        }

        task delegate2 {
            dependsOn buildB.task(':logProject')
        }
    }
"""
                break
            case WHEN_SELECTED:
                buildA.buildFile << """
    def buildB = gradle.includedBuild('buildB')
    task delegate {
        whenSelected {
            dependsOn 'delegate1', 'delegate2'
        }
    }

    task delegate1 {
        whenSelected {
            dependsOn delegate2
            dependsOn buildB.task(':b1:logProject')
        }
    }

    task delegate2 {
        whenSelected {
            dependsOn buildB.task(':logProject')
        }
    }
"""
                break
            case DEFERRED_WHEN_SELECTED:
                buildA.buildFile << """
    def buildB = gradle.includedBuild('buildB')
    task delegate {
        gradle.projectsEvaluated {
            whenSelected {
                dependsOn 'delegate1', 'delegate2'
            }
        }
    }

    gradle.projectsEvaluated {
        task delegate1 {
            whenSelected {
                dependsOn delegate2
                dependsOn buildB.task(':b1:logProject')
            }
        }
    }

    task delegate2 {
        gradle.projectsEvaluated {
            whenSelected {
                dependsOn buildB.task(':logProject')
            }
        }
    }
"""
                break
            default:
                throw new Error("$inclusion not supported (yet)")
        }

        execute(buildA, ":delegate")

        then:
        executed ":buildB:logProject", ":buildB:b1:logProject"
        output.contains("Executing build 'buildB' project ':' task ':logProject'")
        output.contains("Executing build 'buildB' project ':b1' task ':b1:logProject'")
        !output.contains("Executing build 'buildB' project ':b1' task ':b2:logProject'")

        where:
        inclusion << TestInclusionMode.values()
    }

    def "executes tasks only once for included build"() {
        when:
        buildA.buildFile << """
    def buildB = gradle.includedBuild('buildB')
    task delegate {
        dependsOn buildB.task(':b1:logProject')
        dependsOn buildB.task(':b2:logProject')
    }
"""
        buildB.buildFile << """
    project(":b1") {
        logProject.dependsOn(':b2:logProject')
    }
"""

        execute(buildA, ":delegate")

        then:
        executed ":buildB:b2:logProject", ":buildB:b1:logProject"
        output.contains("Executing build 'buildB' project ':b2' task ':b2:logProject'")
        output.contains("Executing build 'buildB' project ':b1' task ':b1:logProject'")
    }

    def "can depend on task from subproject of composing build"() {
        given:
        buildA.settingsFile << """
    include 'a1'
"""
        buildA.buildFile << """
    task("top-level") {
        dependsOn ':a1:delegate'
    }

    project(':a1') {
        task delegate {
            dependsOn gradle.includedBuild('buildB').task(':logProject')
        }
    }
"""

        when:
        execute(buildA, ":top-level")

        then:
        executed ":buildB:logProject"
        output.contains("Executing build 'buildB' project ':' task ':logProject'")

        when:
        execute(buildA, "delegate")

        then:
        executed ":buildB:logProject"
        output.contains("Executing build 'buildB' project ':' task ':logProject'")
        !output.contains("Executing build 'buildB' project ':' task ':b1:logProject'")
        !output.contains("Executing build 'buildB' project ':' task ':b2:logProject'")
    }

    @Unroll
    def "can depend on task with name in all included builds via #inclusion"() {
        when:
        BuildTestFile buildC = newBuild("buildC")
        includedBuilds << buildC

        switch (inclusion) {
            case DEPENDENCY:
                buildA.buildFile << """
    task delegate {
        dependsOn gradle.includedBuilds*.task(':logProject')
    }
"""
                break
            case DEFERRED_DEPENDENCY:
                buildA.buildFile << """
    task delegate { t ->
        gradle.projectsEvaluated {
            t.dependsOn gradle.includedBuilds*.task(':logProject')
        }
    }
"""
                break
            case WHEN_SELECTED:
                buildA.buildFile << """
    task delegate {
        whenSelected {
            dependsOn gradle.includedBuilds*.task(':logProject')
        }
    }
"""
                break
            case DEFERRED_WHEN_SELECTED:
                buildA.buildFile << """
    task delegate {
        gradle.projectsEvaluated {
            whenSelected {
                dependsOn gradle.includedBuilds*.task(':logProject')
            }
        }
    }
"""
                break
            default:
                throw new Error("$inclusion not supported (for now)")
        }

        execute(buildA, ":delegate")

        then:
        executed ":buildB:logProject", ":buildC:logProject"
        output.contains("Executing build 'buildB' project ':' task ':logProject'")
        !output.contains("Executing build 'buildB' project ':' task ':b1:logProject'")
        output.contains("Executing build 'buildC' project ':' task ':logProject'")

        where:
        inclusion << TestInclusionMode.values()
    }

    @Unroll
    def "substitutes dependency of included build when executed via task dependency and #inclusion"() {
        given:
        buildA.buildFile << """
    task delegate {
        ${
            switch (inclusion) {
                case DEPENDENCY:
                    return "dependsOn gradle.includedBuild('buildB').task(':jar')"
                case DEFERRED_DEPENDENCY:
                    return '''
gradle.projectsEvaluated {
    dependsOn gradle.includedBuild('buildB').task(':jar')
}'''
                case WHEN_SELECTED:
                    return '''
whenSelected {
    dependsOn gradle.includedBuild('buildB').task(':jar')
}'''
                case DEFERRED_WHEN_SELECTED:
                    return '''
gradle.projectsEvaluated {
    whenSelected {
        dependsOn gradle.includedBuild('buildB').task(':jar')
    }
}'''
                default:
                    throw new Error("$inclusion not yet supported!")
            }
        }
    }
"""
        buildB.buildFile << """
    allprojects {
        apply plugin: 'java'
    }

    ${
            switch (inclusion) {
                case DEPENDENCY:
                    return '''
    dependencies {
        implementation "org.test:b1:1.0"
    }'''
                case DEFERRED_DEPENDENCY:
                    return '''
    gradle.projectsEvaluated {
        dependencies {
            implementation "org.test:b1:1.0"
        }
    }'''
                case WHEN_SELECTED:
                    return '''
    tasks.compileJava.whenSelected {
        dependencies {
            implementation "org.test:b1:1.0"
        }
    }'''
                case DEFERRED_WHEN_SELECTED:
                    return '''
    gradle.projectsEvaluated {
        tasks.jar.whenSelected {
            dependencies {
                implementation "org.test:b1:1.0"
            }
        }
    }'''
                default:
                    throw new Error("Case $inclusion not supported!")
            }
        }
"""

        when:
        execute(buildA, ":delegate")

        then:
        executed ":buildB:b1:jar", ":buildB:jar"

        where:
        inclusion << TestInclusionMode.values()
    }

    def "A whenSelected node across composites can modify an already-visited task's dependencies"() {

        when:
        buildA.buildFile << """
    task delegate {
        whenSelected {
            dependsOn indirect
        }
    }
    task indirect {
        whenSelected {
          // not required until delegate.whenSelected has been called
          delegate.whenSelected {
              delegate.dependsOn gradle.includedBuild('buildB').task(':logProject')
          }
        }
    }
"""

        execute(buildA, ":delegate")

        then:
        executed ":buildB:logProject"
        output.contains("Executing build 'buildB' project ':' task ':logProject'")


    }

    def "reports failure when included build does not exist for composite"() {
        when:
        buildA.buildFile << """
    task delegate {
        dependsOn gradle.includedBuild('does-not-exist').task(':anything')
    }
"""

        and:
        fails(buildA, ":delegate")

        then:
        failure.assertHasDescription("A problem occurred evaluating root project 'buildA'.")
        failure.assertHasCause("Included build 'does-not-exist' not found in build 'buildA'.")
    }

    def "reports failure when task does not exist for included build"() {
        when:
        buildA.buildFile << """
    task delegate {
        dependsOn gradle.includedBuild('buildB').task(':does-not-exist')
    }
"""

        and:
        fails(buildA, ":delegate")

        then:
        failure.assertHasDescription("Could not determine the dependencies of task ':delegate'.")
        failure.assertHasCause("Task with path ':does-not-exist' not found in project ':buildB'.")
    }

    def "reports failure when task path is not qualified for included build"() {
        when:
        buildA.buildFile << """
    task delegate {
        dependsOn gradle.includedBuild('buildB').task('logProject')
    }
"""

        and:
        fails(buildA, "delegate")

        then:
        failure.assertHasDescription("A problem occurred evaluating root project 'buildA'.")
        failure.assertHasCause("Task path 'logProject' is not a qualified task path (e.g. ':task' or ':project:task')")
    }

    def "reports failure when task path is substring of task in included build"() {
        given:
        buildA.buildFile << """
    task delegate {
        dependsOn gradle.includedBuild('buildB').task(':logP')
    }
    task subDelegate {
        dependsOn gradle.includedBuild('buildB').task(':b1:logP')
    }
"""

        when:
        fails(buildA, ":delegate")

        then:
        failure.assertHasDescription("Could not determine the dependencies of task ':delegate'.")
        failure.assertHasCause("Task with path ':logP' not found in project ':buildB'.")

        when:
        fails(buildA, ":subDelegate")

        then:
        failure.assertHasDescription("Could not determine the dependencies of task ':subDelegate'.")
        failure.assertHasCause("Task with path ':b1:logP' not found in project ':buildB'.")
    }

    def "reports failure when attempting to access included build when build is not a composite"() {
        when:
        buildB.buildFile << """
    task delegate {
        dependsOn gradle.includedBuild('does-not-exist').task(':anything')
    }
"""

        and:
        fails(buildB, ":delegate")

        then:
        failure.assertHasDescription("A problem occurred evaluating root project 'buildB'.")
        failure.assertHasCause("Included build 'does-not-exist' not found in build 'buildB'.")
    }

    @Unroll
    def "included build cannot reference tasks in #scenario"() {
        when:
        BuildTestFile buildC = singleProjectBuild("buildC") {
            buildFile << """
    task illegal {
        dependsOn gradle.includedBuild('$buildName').task(':logProject')
    }
"""
        }
        includedBuilds << buildC

        buildA.buildFile << """
    task delegate {
        dependsOn gradle.includedBuild('buildC').task(':illegal')
    }
    task logProject {}
"""

        then:
        fails(buildA, ":delegate")

        and:
        failure.assertHasDescription("A problem occurred evaluating project ':buildC'.")
        failure.assertHasCause("Included build '${buildName}' not found in build 'buildC'.")

        where:
        scenario  | buildName
        "sibling" | "buildB"
        "parent"  | "buildA"
    }
}
