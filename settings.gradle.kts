plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "phase"
include("phase-core")
include("phase-html")
include("phase-api")
include("phase-vortex")