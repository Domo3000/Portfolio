pluginManagement {
    repositories {
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
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
