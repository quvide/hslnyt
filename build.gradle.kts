plugins {
    val agpVersion = "8.1.4"
    val kotlinVersion = "1.9.21"
    id("com.android.application") version agpVersion apply false
    id("com.android.library") version agpVersion apply false
    id("org.jetbrains.kotlin.android") version kotlinVersion apply false
}

buildscript {
    dependencies {
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    }
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}