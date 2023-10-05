plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    js {
        browser {
            testTask(Action {
                useKarma {
                    useFirefox()
                }
            })
        }
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":frontend"))
                implementation(project(":frontend:requests"))
                implementation(project(":frontend:canvas"))
                implementation(project(":shared:connect4"))
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
