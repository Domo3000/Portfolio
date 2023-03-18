plugins {
    kotlin("js")
}

kotlin {
    js {
        binaries.executable()
        browser {
            webpackTask {
                outputFileName = "connect4.js"
            }
        }
    }
}

dependencies {
    implementation(project(":connect4"))
}