plugins {
//    signing
    kotlin("jvm") version "1.9.22"
    id("hu.vissy.gradle.semanticVersioning") version "0.9"
//    `maven-publish`
}

repositories {
    mavenCentral()
}

group="hu.vissy"
version="1.0.0"


dependencies {
    kotlin("jvm")
//    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:1.9.22")
    api("com.google.code.gson:gson:2.10.1")
    testImplementation("junit:junit:4.13.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

//val signingKeyId = project.findProperty("signing.keyId")

semanticVersion {
    releaseTagPrefix = "v"
    allowDirtyLocal = true
}


tasks.named("jar") {
    dependsOn("versionInfo")
    doLast {
        logger.lifecycle("Building with version: ${project.version}")
    }
}



//publishing {
//    repositories {
//        maven {
//            url = uri(rootDir.resolve("local-plugin-repository"))
//            name="localPluginRepository"
//        }
//    }
//}

