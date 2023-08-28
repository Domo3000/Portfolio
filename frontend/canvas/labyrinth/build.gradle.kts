plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        binaries.executable()
        browser {
            webpackTask {
                val version = findProperty("version")
                outputFileName = "labyrinth-$version.js"
            }
        }
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(libs.bundles.frontend)
                implementation(project(":frontend"))
                implementation(project(":frontend:canvas"))
            }
        }
    }
}