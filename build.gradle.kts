plugins {
    java
}

group = "pl.tfij"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-context:5.3.13")
    implementation("org.springframework.boot:spring-boot-autoconfigure:2.6.1")
    implementation("io.micrometer:micrometer-core:1.8.1")
    implementation("org.slf4j:slf4j-api:1.7.32")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("org.awaitility:awaitility:4.1.1")
    testImplementation("org.springframework.boot:spring-boot-starter:2.6.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.6.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}