plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

dependencies {
    implementation(project(":shared:connect4"))
    implementation(project(":shared:connect4ai"))
    implementation(project(":shared:neural"))

    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.41.1")
    implementation("org.postgresql:postgresql:42.6.0")
    runtimeOnly("com.h2database:h2:2.1.214")

    testImplementation(kotlin("test"))
}
