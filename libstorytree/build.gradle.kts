import com.hierynomus.gradle.license.tasks.LicenseCheck
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.github.hierynomus.license") version "0.15.0"
    //kotlin("kapt") version "1.3.31"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation(kotlin("scripting-jvm-host-embeddable"))
    implementation(kotlin("scripting-common"))
    implementation(kotlin("scripting-jvm"))
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"

license {
    header = rootProject.file("LICENSE_HEADER")
    mapping("kts", "JAVADOC_STYLE")
    exclude("**/*.gradle.kts")
}