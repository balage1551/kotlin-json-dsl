import org.jetbrains.kotlinx.publisher.apache2
import org.jetbrains.kotlinx.publisher.githubRepo
import java.nio.file.Files

plugins {
    kotlin("jvm") version "1.9.22"
    id("hu.vissy.gradle.semanticVersioning") version "[1.0.0,2.0.0)"
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "1.9.10"
    id("org.jetbrains.kotlin.libs.publisher") version "1.8.10-dev-43"
}

repositories {
    mavenCentral()
}

group = "hu.vissy"
version = "1.0.0"


dependencies {
    kotlin("jvm")
//    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:1.9.22")
    api("com.google.code.gson:gson:2.10.1")
    testImplementation("junit:junit:4.13.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}


semanticVersion {
    releaseTagPrefix = "v"
    allowDirtyLocal = true
    forced = true
}



publishing {
    publications {
        create<MavenPublication>("Maven") {
            from(components["java"])
            groupId = "hu.vissy"
            artifactId = "kotlin-json-dsl"
            description = "Kotlin DSL wrapper for GSON"
        }
        withType<MavenPublication> {
            pom {
                packaging = "jar"
                name.set("kotlin-json-dsl")
                description.set("Kotlin DSL wrapper for GSON")
                url.set("https://github.com/balage1551/kotlin-json-dsl/")
                inceptionYear.set("2024")
                licenses {
                    license {
                        name.set("MIT license")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("Balage1551")
                        name.set("Balázs Vissy")
                        email.set("balage42-github@yahoo.com")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:balage1551/kotlin-json-dsl.git")
                    developerConnection.set("scm:git:ssh:git@github.com:balage1551/kotlin-json-dsl.git")
                    url.set("https://github.com/balage1551/kotlin-json-dsl")
                }
            }
        }
    }

    repositories {
        maven {
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }
}

tasks.jar {
    dependsOn("setSemanticVersion")

    enabled = true
    // Remove `plain` postfix from jar file name
    archiveClassifier.set("")

    doLast {
        logger.lifecycle("Building with version: ${project.version}")
    }
}


kotlinPublications {
    // Default group for all publications
    defaultGroup.set("hu.vissy")

    // Prefix for artifacts for all publications
    defaultArtifactIdPrefix.set("kotlin-json-dsl")

    // Set to false if you want to publish empty Javadoc JARs. Maven Central is OK with it
//    fairDokkaJars.set(false)

    // Signing credentials. You will not be able to publish an artifact to Maven Central without signing
    signingCredentials(
        findProperty("signing.keyId").toString(),
        project.file(project.findProperty("signing.secretKeyFile") as String).readText(),
        findProperty("signing.password").toString()
    )

    sonatypeSettings(
        findProperty("ossrh.username").toString(),
        findProperty("ossrh.password").toString(),
        "kotlin-json-dsl"
    )

    pom {
        // Use this convenience extension to set up all needed URLs
        // for the POM in case you're using GitHub
        githubRepo("balage1551", "kotlin-json-dsl")

        // The year your library was first time published to a public repository
        inceptionYear.set("2024")

        licenses {
            // You can contribute extension methods for licences other than Apache 2.0
            apache2()
        }

        developers {
            developer {
                id = "Balage1551"
                name = "Balázs Vissy"
                email = "balage42-github@yahoo.com"
            }
        }
    }

    localRepositories {
        // Default location for the local repository is build/artifacts/maven/
        defaultLocalMavenRepository()
    }

    publication {
        // By default, publication name is an artifact name. In this case, artifact name
        // will be kotlin-jupyter-kernel (prefix + name)
        publicationName.set("core")

        // Description that will appear in POM and on Maven Central search site
        description.set("Kotlin DSL wrapper of GSON.")
    }
}


tasks.named("releaseSonatypeStagingRepository") {
    finalizedBy("commitVersion")
}