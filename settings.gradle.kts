pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials {
                username = "mapbox"
                password =
                    "sk.eyJ1IjoiYXBpcHZheHMiLCJhIjoiY21jNHNjd3RvMGE5MTJpc2EzOWxtZXo4bSJ9.rqnsgFwXAnzPrII2XkBCSw"
            }
        }
    }
}

rootProject.name = "ContractFarmingApp"
include(":app")
