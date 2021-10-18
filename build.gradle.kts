import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
}

group = "me.shadr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


dependencies {
    testImplementation(kotlin("test"))
    implementation("com.jessecorbett:diskord-bot:2.1.1")

    implementation("io.ktor:ktor-server-core:1.6.4")
    implementation("io.ktor:ktor-server-netty:1.6.4")
    implementation("ch.qos.logback:logback-classic:1.2.5")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

tasks.withType<Jar>() {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest { attributes["Main-Class"] = "bot/BotMainKt" }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
