plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        binaries.executable()
        browser {
            webpackTask(Action {
                val version = findProperty("version")
                mainOutputFileName.set( "connect4-$version.js")
            })
        }
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":frontend:canvas:connect4"))
            }
        }
    }
}
