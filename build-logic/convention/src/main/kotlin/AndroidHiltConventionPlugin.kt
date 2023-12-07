/*
 * Copyright 2022 The Android Open Source Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.internal.tasks.databinding.DataBindingGenBaseClassesTask
import com.google.samples.apps.nowinandroid.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
                apply("dagger.hilt.android.plugin")
            }

            dependencies {
                "implementation"(libs.findLibrary("hilt.android").get())
                "ksp"(libs.findLibrary("hilt.compiler").get())
                "kspAndroidTest"(libs.findLibrary("hilt.compiler").get())
                "kspTest"(libs.findLibrary("hilt.compiler").get())
            }
            // Fix: [Ksp] InjectProcessingStep was unable to process 'test' because 'error.NonExistentClass' could not be resolved.
            // https://github.com/google/dagger/issues/4097#issuecomment-1763781846
            // Note, This is a temporary fix and needs to wait for the official official fix
            val androidComponents =
                project.extensions.getByType(AndroidComponentsExtension::class.java)
            // Fix: when a module do not have dataBindingGenBaseClasses will crash
            /**
             * [org.gradle.api.NamedDomainObjectCollection.findByName] to replace [org.gradle.api.NamedDomainObjectCollection.getByName]
             * */
            androidComponents.onVariants { variant ->
                afterEvaluate {
                    project.tasks.getByName("ksp" + variant.name.capitalized() + "Kotlin") {
                        val dataBindingTask =
                            project.tasks.findByName("dataBindingGenBaseClasses" + variant.name.capitalized()) as? DataBindingGenBaseClassesTask
                        if (dataBindingTask != null) {
                            (this as AbstractKotlinCompileTool<*>).setSource(dataBindingTask.sourceOutFolder)
                        }
                    }
                }
            }
        }


    }

}
