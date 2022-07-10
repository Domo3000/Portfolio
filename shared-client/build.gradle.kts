plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    js {
        browser()
    }

    sourceSets {
        val ktorVersion = findProperty("ktorVersion")

        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:$ktorVersion")
                implementation("io.ktor:ktor-client-json-js:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization-js:$ktorVersion")
                implementation("io.ktor:ktor-client-logging-js:$ktorVersion")
                implementation("io.ktor:ktor-client-websockets:$ktorVersion")
            }
        }
        val jsTest by getting {
            dependencies {
            }
        }
    }
}


