import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
}

group = "me.shadr"
version = "1.0-SNAPSHOT"

kotlin.sourceSets.all {
    languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
}

repositories {
    mavenCentral()
}

dependencies {
    // Versions
    val kotlinTestVersion = "1.5.31"
    val kordVersion = "0.8.0-M7"
    val log4jVersion = "2.14.1"
    val gsonVersion = "2.8.8"
    val retrofitVersion = "2.9.0"
    val okHttpVersion = "4.9.2"
    val jColorVersion = "5.2.0"
    val mockitoVersion = "4.0.0"
    val jupiterVersion = "5.7.0"

    // Unit testing
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinTestVersion")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okHttpVersion")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-inline:$mockitoVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$jupiterVersion")

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
    implementation("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")

    // JColor
    implementation("com.diogonunes:JColor:$jColorVersion")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest { attributes["Main-Class"] = "runtime/BotMainKt" }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/INDEX.LIST")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
