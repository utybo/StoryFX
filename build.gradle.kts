import nl.javadude.gradle.plugins.license.License
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

allprojects {
    repositories {
        jcenter() 
    }
}

plugins {
    kotlin("jvm") version "1.3.40" apply false
    id("com.github.hierynomus.license") version "0.15.0" apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.github.hierynomus.license")
    apply(plugin = "maven")

    version = "0.1-SNAPSHOT"

    tasks.withType(License::class) {
        header = rootProject.file("LICENSE_HEADER")
        mapping("kts", "JAVADOC_STYLE")
        exclude("**/*.gradle.kts")
        exclude("**/*.jpg")
        exclude("**/*.ttf")
    }

    val compileKotlin: KotlinCompile by tasks
    compileKotlin.kotlinOptions.jvmTarget = "1.8"
}

