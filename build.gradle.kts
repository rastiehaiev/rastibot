plugins {
    val kotlinVersion = "1.3.61"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("org.springframework.boot") version "2.2.2.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("com.bmuschko.docker-spring-boot-application") version "6.1.3"
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
}

group = "com.sbrati.rastibot"
version = "1.0.0-SNAPSHOT"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:Greenwich.RELEASE"))
    implementation(platform("com.google.cloud:libraries-bom:2.9.0"))

    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation("com.sbrati:spring-boot-starter-telegram:0.0.18")
    implementation("io.github.seik.kotlin-telegram-bot:telegram:4.5.0")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springframework.cloud:spring-cloud-gcp-starter-pubsub")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-ribbon")
    implementation("org.springframework.cloud:spring-cloud-starter-kubernetes:1.1.1.RELEASE")
    implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-ribbon:1.1.1.RELEASE")

    kapt("org.springframework.boot:spring-boot-configuration-processor")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

val imageName = "${project.properties["sbraticomDockerBaseImageName"].toString()}/${project.name}"

docker {
    springBootApplication {
        baseImage.set("openjdk:8-alpine")
        images.set(setOf("${imageName}:${project.version}", "${imageName}:latest"))
        jvmArgs.set(listOf("-Dspring.profiles.active=production", "-Xmx2048m"))
    }
}
