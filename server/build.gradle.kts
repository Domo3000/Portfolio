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
    implementation("io.ktor:ktor-server-netty-jvm:2.2.4")
    testImplementation("io.ktor:ktor-server-tests-jvm:2.2.4")
    testImplementation("io.ktor:ktor-server-test-host-jvm:2.2.4")
    val ktorVersion = findProperty("ktorVersion")

    implementation(project(":shared"))
    implementation(project(":shared:connect4"))

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.20")

    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-caching-headers:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlin-deeplearning-api:0.5.1")
    implementation("org.jetbrains.kotlinx:kotlin-deeplearning-impl:0.5.1")
    implementation("org.jetbrains.kotlinx:kotlin-deeplearning-tensorflow:0.5.1")

    implementation("org.jetbrains.kotlin-wrappers:kotlin-css:1.0.0-pre.388")
    implementation("ch.qos.logback:logback-classic:1.2.11") // TODO 1.4.1

    testImplementation(kotlin("test"))
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("portfolio")
    archiveClassifier.set("")
    archiveVersion.set("v1")

    val debug = System.getenv("DEBUG")
    val projects = mutableListOf("browser", "canvas:automaton", "canvas:kdtree", "canvas:shuffle", "canvas:labyrinth")
    val environment = if(debug == "true") {
        projects.add("canvas:connect4:debug")
        "Development"
    } else {
        projects.add("canvas:connect4:prod")
        "Production"
    }

    for(project in projects) {
        dependsOn(":frontend:$project:jsBrowser${environment}Webpack")
        val js = tasks.getByPath(":frontend:$project:jsBrowser${environment}Webpack") as KotlinWebpack
        into("assets") { from(File(js.outputDirectory.asFile.get(), js.mainOutputFileName.get())) }
    }

    manifest {
        attributes(Pair("Implementation-Version", "1.15"))
    }

    minimize {
        exclude(dependency("org.jetbrains.kotlin:kotlin-reflect"))
    }
}

tasks.getByName<JavaExec>("run") {
    dependsOn(tasks.withType<ShadowJar>())
    classpath(tasks.withType<ShadowJar>())
}

apply(plugin = "com.github.johnrengelman.shadow")