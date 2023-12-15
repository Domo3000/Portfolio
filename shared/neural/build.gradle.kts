plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                val coroutineVersion = findProperty("coroutineVersion")
                val serializationVersion = findProperty("serializationVersion")

                implementation(project(":shared:connect4"))
                implementation(project(":shared:connect4ai"))

                implementation("org.jetbrains.kotlinx:kotlin-deeplearning-api:0.5.1")
                implementation("org.jetbrains.kotlinx:kotlin-deeplearning-impl:0.5.1")
                implementation("org.jetbrains.kotlinx:kotlin-deeplearning-tensorflow:0.5.1")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
