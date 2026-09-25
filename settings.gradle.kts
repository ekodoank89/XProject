pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Repo resmi API Xposed — satu-satunya sumber artefak
        // de.robv.android.xposed:api (tidak tersedia di Google/MavenCentral)
        maven { url = uri("https://api.xposed.info/") }
    }
}

rootProject.name = "XProject"
include(":app")
