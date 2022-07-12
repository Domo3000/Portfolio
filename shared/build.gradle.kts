plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

private val reactVersion = findProperty("reactVersion") as String

fun kotlinw(target: String, version: String = reactVersion): String =
    "org.jetbrains.kotlin-wrappers:kotlin-$target:$version"

kotlin {
    jvm()

    js {
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(kotlinw("emotion", "11.9.3-pre.354"))
                implementation(kotlinw("csstype", "3.1.0-pre.354"))
                implementation(kotlinw("react-dom"))
                implementation(kotlinw("react"))
            }
        }
        val jsTest by getting {
            dependencies {
            }
        }
    }
}


