plugins {
    kotlin("jvm") version "1.6.21"
    id("org.jetbrains.kotlinx.kover") version "0.5.0"
    id("maven-publish")
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("signing")
}

group = "xyz.haff"
version = "0.3.0"

repositories {
    mavenCentral()
}

tasks.wrapper {
    gradleVersion = "7.4"
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--add-opens", "java.base/java.time=ALL-UNNAMED")
    finalizedBy(tasks.koverHtmlReport)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                packaging = "jar"
                name.set(project.name)
                description.set("Small Redis utility library built on top of Jedis")

                url.set("https://github.com/huuff/siths")
                scm {
                    connection.set("scm:git:git://github.com/huuff/siths.git")
                    developerConnection.set("scm:git:git@github.com:huuff/siths.git")
                    url.set("https://github.com/huuff/siths/tree/master")
                }

                licenses {
                    license {
                        name.set("WTFPL - Do What The Fuck You Want To Public License")
                        url.set("http://www.wtfpl.net")
                    }
                }

                developers {
                    developer {
                        name.set("Francisco Sánchez")
                        email.set("haf@protonmail.ch")
                        organizationUrl.set("https://github.com/huuff")
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}


nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(properties["sonatype.user"] as String)
            password.set(properties["sonatype.password"] as String)
        }
    }
}

val kotestVersion = "5.3.1"
dependencies {
    implementation(kotlin("stdlib"))
    implementation("redis.clients:jedis:4.2.3")
    implementation("commons-codec:commons-codec:1.15")

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