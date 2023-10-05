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

                implementation(project(":shared:connect4"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
