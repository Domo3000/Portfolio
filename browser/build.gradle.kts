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
    implementation(project(":shared:js"))
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:6.3.0-pre.388")
}
