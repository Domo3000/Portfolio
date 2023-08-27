pluginManagement {
    repositories {
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
    }
}

fun VersionCatalogBuilder.kotlinw(target: String, version: String): () -> Unit {
    return { library(target, "org.jetbrains.kotlin-wrappers:kotlin-$target:$version-pre.620") }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val frontend = listOf(
                Pair("react", "18.2.0"),
                Pair("react-dom", "18.2.0"),
                Pair("emotion", "11.11.1"),
                Pair("csstype", "3.1.2")
            )

            frontend.forEach { kotlinw(it.first, it.second)() }

            bundle("frontend", frontend.map { it.first })

            val ktorVersion = extra["ktorVersion"]
            val coroutineVersion = extra["coroutineVersion"]

            val webhooks = listOf(
                "ktor-client",
                "ktor-client-json",
                "ktor-client-serialization",
                "ktor-client-logging",
                "ktor-client-websockets"
            )

            webhooks.forEach { name ->
                library(name, "io.ktor:$name-js:$ktorVersion")
            }

            val coroutines = Pair("coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
            library(coroutines.first, coroutines.second)

            bundle("webhooks", webhooks + coroutines.first)
        }
    }
}

rootProject.name = "Portfolio"

include(":shared")
include(":shared:js")
include(":canvas")
include(":shared-connect4")
include(":connect4")
include(":browser")
include(":canvas:automaton")
include(":canvas:kdtree")
include(":canvas:shuffle")
include(":canvas:labyrinth")
include(":connect4:debug")
include(":connect4:prod")
include(":server")
