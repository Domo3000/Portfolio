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
include(":browser")
include(":canvas:automaton")
include(":canvas:kdtree")
include(":canvas:shuffle")
include(":server")
