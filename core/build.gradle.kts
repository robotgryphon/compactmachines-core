
import java.text.SimpleDateFormat
import java.util.*

val versionMain: String = System.getenv("VERSION") ?: "0.0.0"
val mcVersion = property("minecraft_version") as String
// val parchmentVersion = property("parchment_version") as String
val feather_version = property("feather_version") as String

plugins {
    java
    id("maven-publish")
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
}

minecraft {
    version(mcVersion)
}

sourceSets {
    named("main") {
        resources {
            //The API has no resources
            setSrcDirs(emptyList<String>())
        }
    }

    named("test") {
        resources {
            //The test module has no resources
            setSrcDirs(emptyList<String>())
        }
    }
}

base {
    group = "dev.compactmods.compactmachines"
    version = versionMain
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withJavadocJar()
}

repositories {
    mavenCentral() {
        content {
            includeGroup("com.aventrix.jnanoid")
        }
    }

    maven("https://maven.parchmentmc.org") {
        name = "ParchmentMC"
    }

    maven("https://maven.pkg.github.com/compactmods/feather") {
        name = "Feather"
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

val cmModules = listOf(
        project(":core-api"),
        project(":room-api"),
        project(":room-upgrade-api")
)

dependencies {
    cmModules.forEach {
        implementation(it)
        testImplementation(it)
    }

    compileOnly("com.aventrix.jnanoid", "jnanoid", "2.0.0")
    implementation("dev.compactmods", "feather", feather_version)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-proc:none")
    options.compilerArgs.addAll(arrayOf("-Xmaxerrs", "9000"))
}

tasks.withType<Jar> {
    manifest {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
        attributes(mapOf(
                "Specification-Title" to "Compact Machines - Core",
                "Specification-Version" to "1", // We are version 1 of ourselves
                "Implementation-Title" to "Compact Machines - Core",
                "Implementation-Timestamp" to now,
                "FMLModType" to "GAMELIBRARY"
        ))
    }
}

val PACKAGES_URL = System.getenv("GH_PKG_URL") ?: "https://maven.pkg.github.com/compactmods/compactmachines-core"
publishing {
    publications.register<MavenPublication>("core") {
        artifactId = "core"
        from(components.getByName("java"))
    }

    repositories {
        // GitHub Packages
        maven(PACKAGES_URL) {
            name = "GitHubPackages"
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}