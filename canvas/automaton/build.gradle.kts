plugins {
    kotlin("js")
}

kotlin {
    js {
        binaries.executable()
        browser {
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