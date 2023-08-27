plugins {
    kotlin("multiplatform")
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
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":canvas"))
                implementation(project(":shared:js"))
                implementation(project(":shared-connect4"))
                implementation(libs.bundles.frontend)
                implementation(libs.bundles.webhooks)
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
