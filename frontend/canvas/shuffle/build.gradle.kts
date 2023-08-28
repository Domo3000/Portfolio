plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    js {
        binaries.executable()
        browser {
            webpackTask {
                val version = findProperty("version")
                outputFileName = "shuffle-$version.js"
            }
            testTask {
                useKarma {
                    useFirefox()
                }
            }
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
