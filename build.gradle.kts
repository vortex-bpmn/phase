plugins {
    kotlin("jvm") version "2.2.21"
}

group = "at.phactum.vortex"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

allprojects {
    apply(plugin = "kotlin")
    dependencies {
        implementation("org.slf4j:slf4j-api:2.0.13")
        runtimeOnly("ch.qos.logback:logback-classic:1.5.6")
    }
}

kotlin {
    jvmToolchain(23)
}

tasks.test {
    useJUnitPlatform()
}