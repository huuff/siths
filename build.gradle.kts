plugins {
    kotlin("jvm") version "1.6.21"
}

group = "xyz.haff"
version = "0.1.0"

repositories {
    mavenCentral()
}

val kotestVersion = "5.3.1"
dependencies {
    implementation(kotlin("stdlib"))
    implementation("redis.clients:jedis:4.2.3")

    testImplementation(kotlin("test"))
    testImplementation("org.testcontainers:testcontainers:1.17.3")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-json:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:1.3.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.2")
    testImplementation("io.mockk:mockk:1.12.4")
}