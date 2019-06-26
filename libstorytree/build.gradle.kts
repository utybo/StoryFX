plugins {
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
