plugins {
    kotlin("multiplatform")
}

fun kotlinw(target: String, version: String): String {
    val wrappersVersion = findProperty("kotlinWrappersVersion")
    return "org.jetbrains.kotlin-wrappers:kotlin-$target:$version$wrappersVersion"
}

kotlin {
    js {
        browser { }
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(libs.bundles.frontend)
            }
        }
    }
}
