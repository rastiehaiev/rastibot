plugins {
    val kotlinVersion = "1.3.61"
    id("org.springframework.boot") version "2.2.2.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("com.bmuschko.docker-spring-boot-application") version "6.1.3"
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
}

repositories {
    maven {
        url = uri(project.properties["sbraticomUrl"].toString())
        credentials {
            username = project.properties["sbraticomUsername"].toString()
            password = project.properties["sbraticomPassword"].toString()
        }
    }
    jcenter()
    maven("https://jitpack.io")
    mavenLocal()
}

group = "com.sbrati.rastibot"
version = "2.0.12"

dependencies {
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:Greenwich.RELEASE"))
    implementation(platform("com.google.cloud:libraries-bom:2.9.0"))
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation("com.sbrati:spring-boot-starter-gcp-logging:1.0.5")
    implementation("com.sbrati:spring-boot-starter-kotlin-telegram:2.0.32")
    implementation("com.sbrati:spring-boot-starter-kotlin-telegram-gcp-pubsub:1.0.3")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    kapt("org.springframework.boot:spring-boot-configuration-processor")
}

kapt.includeCompileClasspath = false

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

val sbratiImageName = "${project.properties["sbraticomDockerBaseImageName"].toString()}/${project.name}"

docker {
    springBootApplication {
        baseImage.set("openjdk:11-jdk-slim")
        images.set(setOf(
                "${sbratiImageName}:${project.version}",
                "${sbratiImageName}:latest"
        ))
        jvmArgs.set(listOf("-Xmx1024m"))
    }
}
