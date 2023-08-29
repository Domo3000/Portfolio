plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    js {
        binaries.executable()
        browser {
            webpackTask(Action {
                val version = findProperty("version")
                mainOutputFileName.set( "shuffle-$version.js")
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
                implementation(project(":frontend:requests"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
