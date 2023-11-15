plugins {
    `kotlin-dsl`
}

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/google") }
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.gradlePlugin.android)
    implementation(libs.gradlePlugin.kotlin)
    // Fix: https://github.com/google/dagger/issues/3068
    //noinspection UseTomlInstead
    @Suppress("SpellCheckingInspection")
    implementation("com.squareup:javapoet:1.13.0")
}

// Target JVM 17.
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
}