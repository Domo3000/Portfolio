plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        binaries.executable()
        browser {
            webpackTask(Action {
                val version = findProperty("version")
                mainOutputFileName.set( "kdtree-$version.js")
            })
            testTask(Action {
                useKarma {
                    useFirefox()
                }
            })
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
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}