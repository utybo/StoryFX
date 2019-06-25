import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.javamodularity.moduleplugin") version "1.5.0"
    id("com.github.hierynomus.license") version "0.15.0"
    //kotlin("kapt") version "1.3.31"
}

repositories {
    jcenter()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
    compile(kotlin("scripting-jvm-host-embeddable"))
    compile(kotlin("scripting-common"))
    compile(kotlin("scripting-jvm"))
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
}


val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"
//val compileJava: JavaCompile by tasks
//compileJava.destinationDir = compileKotlin.destinationDir

license {
    header = rootProject.file("LICENSE_HEADER")
    mapping("kts", "JAVADOC_STYLE")
    exclude("**/*.gradle.kts")
}