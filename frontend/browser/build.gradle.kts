plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        binaries.executable()
        browser {
            webpackTask(Action {
                val version = findProperty("version")
                mainOutputFileName.set("main-$version.js")
            })
        }
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                val coroutineVersion = findProperty("coroutineVersion")

                implementation(libs.bundles.frontend)
                implementation(project(":frontend"))
                //implementation(project(":frontend:requests"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:6.15.0-pre.620")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
            }
        }
    }
}
