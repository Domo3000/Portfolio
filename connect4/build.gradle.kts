plugins {
    kotlin("js")
    kotlin("plugin.serialization")
}

kotlin {
    js {
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
    val ktorVersion = findProperty("ktorVersion")
    val coroutineVersion = findProperty("coroutineVersion")

    implementation(project(":canvas"))
    implementation(project(":shared-connect4"))
    implementation("io.ktor:ktor-client-js:$ktorVersion")
    implementation("io.ktor:ktor-client-json-js:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization-js:$ktorVersion")
    implementation("io.ktor:ktor-client-logging-js:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutineVersion")
    testImplementation(kotlin("test"))
}