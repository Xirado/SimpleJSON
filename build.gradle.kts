plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.6.20"
}

group = "at.xirado"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.8.3")
    api("org.jetbrains:annotations:23.0.0")
    api("org.slf4j:slf4j-api:1.7.36")

    compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "at.xirado"
            artifactId = "simplejson"
            version = "1.0-SNAPSHOT"

            from(components["java"])
        }
    }
}