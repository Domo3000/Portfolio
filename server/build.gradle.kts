import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

application {
    mainClass.set("ApplicationKt")
}

dependencies {
    //val coroutineVersion = findProperty("coroutineVersion")
    val ktorVersion = findProperty("ktorVersion")

    implementation(project(":shared"))
    implementation(project(":shared-connect4"))

    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.2.11")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("portfolio")
    archiveClassifier.set("")
    archiveVersion.set("")
    for(element in listOf("browser", "connect4")) {
        dependsOn(":$element:browserProductionWebpack")
        val js = tasks.getByPath(":$element:browserProductionWebpack") as KotlinWebpack
        from(File(js.destinationDirectory, js.outputFileName))
    }
}

tasks.getByName<JavaExec>("run") {
    dependsOn(tasks.withType<ShadowJar>())
    classpath(tasks.withType<ShadowJar>())
}

apply(plugin = "com.github.johnrengelman.shadow")