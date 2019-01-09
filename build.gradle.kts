import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    maven
    java
    kotlin("jvm") version "1.2.50"
}

group = "io.disassemble"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("org.ow2.asm", "asm-all", "6.0_BETA")
    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}