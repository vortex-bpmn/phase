plugins {
    kotlin("jvm")
    application
    id("com.gradleup.shadow") version "9.3.1"
}

group = "at.phactum.vortex"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("at.phactum.vortex.phase.cli.CliKt")
}

tasks.shadowJar {
    mainClass.set("at.phactum.vortex.phase.cli.CliKt")
}

tasks.build {
    dependsOn("shadowJar")
}

dependencies {
    implementation(project(":phase-core"))
    implementation(project(":phase-vortex"))

    implementation("com.github.ajalt.clikt:clikt:5.0.1")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(23)
}

tasks.test {
    useJUnitPlatform()
}