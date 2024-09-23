plugins {
    kotlin("jvm") version "1.9.23"
    application
}

group = "com.mtereshchuk"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.javalin:javalin:5.3.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
    implementation("org.slf4j:slf4j-simple:2.0.16")

    testImplementation("io.javalin:javalin-testtools:5.3.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.5.21")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.5.21")
}

application {
    mainClass = "com.mtereshchuk.account.App"
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
