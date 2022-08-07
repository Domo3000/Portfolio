plugins {
    kotlin("js")
}

kotlin {
    js {
        binaries.executable()
        browser {
            runTask {
                outputFileName = "kdtree.js"
            }
        }
    }
}

dependencies {
    implementation(project(":shared-canvas"))
    testImplementation(kotlin("test"))
}