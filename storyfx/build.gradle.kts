import com.badlogicgames.packr.Packr
import com.badlogicgames.packr.PackrConfig
import de.undercouch.gradle.tasks.download.Download
import org.apache.tools.ant.taskdefs.condition.Os
import org.mini2Dx.parcl.ParclExtension
import org.mini2Dx.parcl.domain.Exe
import org.mini2Dx.parcl.task.ExeBundleTask

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath(group = "org.mini2Dx", name = "parcl", version = "1.6.1")
        classpath(files("buildscriptdep/packr.jar"))
    }
}

apply(plugin = "org.mini2Dx.parcl")
plugins {
    id("org.openjfx.javafxplugin") version "0.0.8"
    id("de.undercouch.download")
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
    version = "12.0.2"
    modules("javafx.controls", "javafx.web")
}

application {
    // Define the main class for the application.
    mainClassName = "guru.zoroark.storyfx.StoryFxAppKt"
}

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

val win64JreUrl = "https://github.com/AdoptOpenJDK/openjdk12-binaries/releases/download/jdk-12.0.2%2B10/OpenJDK12U-jre_x64_windows_hotspot_12.0.2_10.zip"
val osx64JreUrl = "https://github.com/AdoptOpenJDK/openjdk12-binaries/releases/download/jdk-12.0.2%2B10/OpenJDK12U-jre_x64_mac_hotspot_12.0.2_10.tar.gz"
val linux64JreUrl = "https://github.com/AdoptOpenJDK/openjdk12-binaries/releases/download/jdk-12.0.2%2B10/OpenJDK12U-jre_x64_linux_hotspot_12.0.2_10.tar.gz"

fun generateDownloadTask(resourceName: String, resourceUrl: String) =
        tasks.register<Download>("download$resourceName") {
            val jreDir = "build/jre/downloadedJre.bin"
            src(resourceUrl)
            dest(jreDir)
            overwrite(false)
        }

val cleanJreDownload = tasks.register<Delete>("cleanJreDownload") {
    delete("build/jre")
}

val jreVersion = "jdk-12.0.2+10-jre"
val downloadWindowsJre = generateDownloadTask("WindowsJre", win64JreUrl)
val downloadLinuxJre = generateDownloadTask("LinuxJre", linux64JreUrl)
val downloadOsxJre = generateDownloadTask("OsxJre", osx64JreUrl)

val generateNativeJre = tasks.register<Copy>("generateNativeJre") {
    val isTar: Boolean
    dependsOn(when {
        Os.isFamily(Os.FAMILY_WINDOWS) -> downloadWindowsJre.also { isTar = false }
        Os.isFamily(Os.FAMILY_MAC) -> downloadOsxJre.also { isTar = true }
        Os.isFamily(Os.FAMILY_UNIX) -> downloadLinuxJre.also { isTar = true }
        else -> error("Unsupported build platform for native JREs")
    })

    from(if (isTar) tarTree("build/jre/downloadedJre.bin") else zipTree("build/jre/downloadedJre.bin"))
    eachFile {
        val strAr: Array<String> = relativePath.segments.copyOf()
        strAr[0] = "jre"
        relativePath = RelativePath(true, *strAr)
    }
    into(File(buildDir, "jreUnpacked"))
}

infix fun File.c(s: String) = File(this, s)

val packr = tasks.register("packr") {
    dependsOn("installDist", "generateNativeJre")
    doLast {
        val cfg = PackrConfig()
        with(cfg) {
            platform = PackrConfig.Platform.Windows64;
            jdk = (buildDir c "jreUnpacked").absolutePath
            executable = "storyfx"
            classpath = (buildDir c "install" c "storyfx" c "lib").listFiles().map { it.absolutePath }
            vmArgs = listOf<String>()

            mainClass = application.mainClassName
            outDir = buildDir c "packr" c "win64"
        }
        Packr().pack(cfg)
    }
}

