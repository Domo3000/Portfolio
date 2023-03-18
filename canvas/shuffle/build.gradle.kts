plugins {
    kotlin("js")
}

kotlin {
    js {
        binaries.executable()
        browser {
            webpackTask {
                outputFileName = "shuffle.js"
            }
            testTask {
                useKarma {
                    useFirefox()
                }
            }
        }
    }
}

dependencies {
    implementation(project(":canvas"))
    testImplementation(kotlin("test"))
}