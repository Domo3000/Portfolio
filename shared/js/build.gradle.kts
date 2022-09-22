plugins {
    kotlin("js")
}

kotlin {
    js {
        browser { }
    }
}

fun kotlinw(target: String, version: String): String =
    "org.jetbrains.kotlin-wrappers:kotlin-$target:$version"

dependencies {
    implementation(kotlinw("react", "18.2.0-pre.388"))
    implementation(kotlinw("react-dom", "18.2.0-pre.388"))
    implementation(kotlinw("emotion", "11.10.4-pre.388"))
    implementation(kotlinw("csstype", "3.1.1-pre.388"))
}