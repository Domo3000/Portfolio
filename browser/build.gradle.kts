plugins {
    kotlin("js")
}

kotlin {
    js {
        binaries.executable()
        browser {
            runTask {
                cssSupport.enabled = true
                outputFileName = "main.js"
            }
        }
    }
}

dependencies {
    implementation(project(":shared"))
}
