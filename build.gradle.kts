plugins {
    kotlin("multiplatform") version "1.9.10" apply false
    kotlin("plugin.serialization") version "1.9.10" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xcontext-receivers")
            jvmTarget = "17"
        }
    }
}
