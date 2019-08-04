plugins {
    id("org.kodein.root")
}

buildscript {
    repositories {
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.12.9")
    }
}

allprojects {
    group = "org.kodein.memory"
    version = "0.1.0"
}

kodeinPublications {
    repo = "Kodein-Memory"
}
