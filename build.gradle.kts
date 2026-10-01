plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

version = "1.0.0"
group = "br.com.rabbitmqconsumer"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

tasks.named<Jar>("jar") {
    enabled = false
}

configurations.configureEach {
    exclude(group = "ch.qos.logback", module = "logback-classic")
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
}

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-jackson")

    implementation("org.springframework.boot:spring-boot-starter-log4j2")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-amqp-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}