plugins {
    kotlin("js")
}

kotlin {
    js {
        binaries.executable()
        browser {
            runTask {
                cssSupport.enabled = true
                outputFileName = "connect4.js"
            }
        }
    }
}

dependencies {
    val coroutineVersion = findProperty("coroutineVersion")

    implementation(project(":shared:js"))
    implementation(project(":shared-client"))
    implementation(project(":shared-connect4"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutineVersion")
}
