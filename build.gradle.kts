plugins {
    `java-library`
    `maven-publish`
}

group = "at.xirado"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
    api("org.jetbrains:annotations:23.0.0")
    api("org.slf4j:slf4j-api:1.7.36")
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