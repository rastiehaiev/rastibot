plugins {
    val kotlinVersion = "1.5.10"
    id("org.springframework.boot") version "2.7.1"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("com.bmuschko.docker-spring-boot-application") version "6.1.3"
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
}

val spaceLibsUrl: String by project
val spaceLibsUsername: String by project
val spaceLibsPassword: String by project

repositories {
    maven {
        url = uri(spaceLibsUrl)
        credentials {
            username = spaceLibsUsername
            password = spaceLibsPassword
        }
    }
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/space/maven")
    }
    jcenter()
    maven("https://jitpack.io")
    mavenLocal()
}

group = "com.sbrati.rastibot"
version = "3.0.0"

dependencies {
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:Greenwich.RELEASE"))
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation("com.sbrati:spring-boot-starter-kotlin-telegram:5.0.6")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("org.jetbrains:space-sdk-jvm:80470-beta")
    implementation("io.ktor:ktor-client-core:1.5.4")
    implementation("io.ktor:ktor-client-apache:1.5.4")

    implementation("org.postgresql:postgresql:42.2.6")
    implementation("org.flywaydb:flyway-core:5.2.4")

    kapt("org.springframework.boot:spring-boot-configuration-processor")
}

kapt.includeCompileClasspath = false

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

val spaceRastibotBaseImageName: String by project
val sbratiImageName = "${spaceRastibotBaseImageName}/${project.name}"

docker {
    springBootApplication {
        baseImage.set("openjdk:11-jdk-slim")
        images.set(
            setOf(
                "${sbratiImageName}:${project.version}",
                "${sbratiImageName}:latest"
            )
        )
        jvmArgs.set(listOf("-Xmx1024m"))
    }
}
