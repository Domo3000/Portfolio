plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        binaries.executable()
        browser {
            webpackTask(Action {
                val version = findProperty("version")
                mainOutputFileName.set( "connect4about-$version.js")
            })
        }
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":frontend"))
                implementation(project(":frontend:requests"))
                implementation(project(":frontend:canvas"))
                implementation(project(":shared:connect4"))
                implementation(libs.bundles.frontend)
            }
        }
    }
}
