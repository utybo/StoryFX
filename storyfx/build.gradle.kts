plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    kotlin("jvm")
    id("com.github.hierynomus.license") version "0.15.0"
    id("org.openjfx.javafxplugin") version "0.0.7"
    application
}

repositories {
    jcenter()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("no.tornado:tornadofx:2.0.0-SNAPSHOT")
    implementation(project(":libstorytree"))
    implementation("org.kordamp.ikonli:ikonli-javafx:11.3.4")
    implementation("org.kordamp.ikonli:ikonli-material-pack:11.3.4")
    implementation("org.kordamp.ikonli:ikonli-materialdesign-pack:11.3.4")
    implementation("com.atlassian.commonmark:commonmark:0.12.1")

    /*
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    */
}

javafx {
    version = "12.0.1"
    modules("javafx.controls", "javafx.web")
}

application {
    // Define the main class for the application.
    mainClassName = "guru.zoroark.storyfx.StoryFxApp"
}

tasks.compileKotlin { kotlinOptions.jvmTarget = "1.8" }

val createProperties = tasks.register("createProperties") {
    dependsOn(tasks.processResources)
    doLast {
        val file = File("$buildDir/resources/main/version.properties")
        val properties = `java.util`.Properties()
        properties["version"] = project.version.toString()
        val writer = `java.io`.FileWriter(file)
        writer.use {
            properties.store(it, null)
        }
    }
}

tasks.classes {
    dependsOn(createProperties.get())
}

license {
    header = rootProject.file("LICENSE_HEADER")
    mapping("kts", "JAVADOC_STYLE")
    exclude("**/*.gradle.kts")
    exclude("**/*.jpg")
    exclude("**/*.ttf")
}