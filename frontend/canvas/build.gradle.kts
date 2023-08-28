plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        browser { }
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(libs.bundles.frontend)
                implementation(project(":frontend"))
            }
        }
    }
}
