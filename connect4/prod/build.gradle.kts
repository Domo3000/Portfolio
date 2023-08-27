plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        binaries.executable()
        browser {
            webpackTask {
                val version = findProperty("version")
                outputFileName = "connect4-$version.js"
            }
        }
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":connect4"))
            }
        }
    }
}
