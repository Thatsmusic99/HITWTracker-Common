plugins {
    id("java")
}

group = "io.github.thatsmusic99"
version = "1.0-alpha.1"

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
}

dependencies {

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    compileOnly("org.jetbrains:annotations:24.0.0")
    compileOnly("org.slf4j:slf4j-api:2.0.1")
    compileOnly("com.google.code.gson:gson:2.10")
    compileOnly("com.mojang:brigadier:1.1.8")
    compileOnly("net.kyori:adventure-api:4.13.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}