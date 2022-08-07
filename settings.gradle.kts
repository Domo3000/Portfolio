pluginManagement {
    repositories {
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
    }
}

rootProject.name = "Portfolio"

include(":shared")
include(":shared-canvas")
include(":shared-client")
include(":browser")
include(":shared-connect4")
include(":connect4")
include(":shuffle")
include(":kdtree")
include(":server")
