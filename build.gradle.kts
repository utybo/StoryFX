allprojects {
    repositories {
        jcenter() 
    }
}

plugins {
    kotlin("jvm") version "1.3.40" apply false
}

subprojects {
    version = "0.1-SNAPSHOT"
}