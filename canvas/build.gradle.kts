plugins {
    kotlin("js")
}

kotlin {
    js {
        browser { }
    }
}

dependencies {
    implementation(project(":shared:js"))
}