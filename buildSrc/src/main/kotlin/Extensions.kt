@file:Suppress("NOTHING_TO_INLINE")

package com.yeahka.android.sgpos

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
//
// val Project.minSdk: Int
//     get() = intProperty("minSdk")
//
// val Project.targetSdk: Int
//     get() = intProperty("targetSdk")
//
// val Project.compileSdk: Int
//     get() = intProperty("compileSdk")
//
// val Project.groupId: String
//     get() = stringProperty("GROUP")
//
// val Project.versionName: String
//     get() = stringProperty("VERSION_NAME")
//
// val Project.versionCode: Int
//     get() {
//         val code = versionName.filter { it.isDigit() }
//         if (code.length != 6) {
//             error("Please input valid VERSION_NAME, such as 1.0.0(001), 6 numbers")
//         }
//         return code.toInt()
//     }

private fun Project.intProperty(name: String): Int {
    return (property(name) as String).toInt()
}

private fun Project.stringProperty(name: String): String {
    return property(name) as String
}

private inline fun <T> List<T>.sumByIndexed(selector: (Int, T) -> Int): Int {
    var index = 0
    var sum = 0
    for (element in this) {
        sum += selector(index++, element)
    }
    return sum
}

// inline infix fun <T> Property<T>.by(value: T) = set(value)

inline infix fun <T> Property<T>.by(provider: Provider<T>) = set(provider)

inline infix fun <T> SetProperty<T>.by(value: Set<T>) = set(value)
