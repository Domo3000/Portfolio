plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        binaries.executable()
        browser {
            webpackTask {
                val version = findProperty("version")
                outputFileName = "automaton-$version.js"
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
                implementation(project(":shared:js"))
                implementation(project(":canvas"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
