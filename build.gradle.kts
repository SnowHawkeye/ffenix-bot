import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
}

group = "me.shadr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Versions
    val kordVersion = "0.8.0-M7"
    val log4jVersion = "2.14.1"
    val gsonVersion = "2.8.8"
    val retrofitVersion = "2.9.0"
    val okHttpVersion = "4.9.0"
    val jColorVersion = "5.0.1"

    testImplementation(kotlin("test"))

    // Kord
    implementation("dev.kord:kord-core:$kordVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion") // required to fix logging

    // GSON
    implementation("com.google.code.gson:gson:$gsonVersion")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.retrofit2:adapter-rxjava2:$retrofitVersion")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")

    // JColor
    implementation("com.diogonunes:JColor:$jColorVersion")
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
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/INDEX.LIST")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
