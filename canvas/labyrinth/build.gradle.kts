plugins {
    kotlin("js")
}

kotlin {
    js {
        binaries.executable()
        browser { }
    }
}

dependencies {
    implementation(project(":canvas"))
    testImplementation(kotlin("test"))
}