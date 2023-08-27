plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        binaries.executable()
        browser {
            webpackTask {
                val version = findProperty("version")
                outputFileName = "main-$version.js"
            }
        }
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                val coroutineVersion = findProperty("coroutineVersion")

                implementation(libs.bundles.frontend)
                implementation(project(":shared:js"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:6.15.0-pre.620")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
            }
        }
    }
}
