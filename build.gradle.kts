import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "it.pagopa.aca"

version = "0.0.1"

description = "pagopa-aca-service"

plugins {
  id("java")
  id("org.springframework.boot") version "3.0.5"
  id("io.spring.dependency-management") version "1.1.0"
  id("com.diffplug.spotless") version "6.18.0"
  id("org.openapi.generator") version "6.3.0"
  id("org.sonarqube") version "4.0.0.2929"
  id("com.dipien.semantic-version") version "2.0.0" apply false
  kotlin("plugin.spring") version "1.8.10"
  kotlin("jvm") version "1.8.10"
  jacoco
  application
}

java.sourceCompatibility = JavaVersion.VERSION_17

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "17" }

repositories { mavenCentral() }

dependencyManagement {
  imports { mavenBom("org.springframework.boot:spring-boot-dependencies:3.0.5") }
  // Kotlin BOM
  imports { mavenBom("org.jetbrains.kotlin:kotlin-bom:1.7.22") }
  imports { mavenBom("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4") }
}

dependencies {
  implementation("io.projectreactor:reactor-core")
  implementation("io.projectreactor.netty:reactor-netty")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-web-services")
  implementation("org.glassfish.jaxb:jaxb-runtime")
  implementation("jakarta.xml.bind:jakarta.xml.bind-api")
  implementation("io.swagger.core.v3:swagger-annotations:2.2.8")
  implementation("org.apache.httpcomponents:httpclient")
  implementation("com.google.code.findbugs:jsr305:3.0.2")
  implementation("org.projectlombok:lombok")
  implementation("org.openapitools:openapi-generator-gradle-plugin:6.5.0")
  implementation("org.openapitools:jackson-databind-nullable:0.2.6")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("org.springframework.boot:spring-boot-starter-aop")
  implementation("io.netty:netty-resolver-dns-native-macos:4.1.90.Final")
  implementation("com.diffplug.spotless:spotless-plugin-gradle:6.18.0")
  // Kotlin dependencies
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  runtimeOnly("org.springframework.boot:spring-boot-devtools")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.mockito:mockito-inline")
  testImplementation("io.projectreactor:reactor-test")
  // Kotlin dependencies
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
  // testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
}

configurations {
  implementation.configure {
    exclude(module = "spring-boot-starter-web")
    exclude("org.apache.tomcat")
    exclude("ch.qos.logback")
  }
}
// Dependency locking - lock all dependencies
dependencyLocking { lockAllConfigurations() }

sourceSets {
  main {
    kotlin { srcDirs("src/main/kotlin", "$buildDir/generated/src/main/kotlin") }
    resources { srcDirs("src/resources") }
  }
}

springBoot {
  mainClass.set("it.pagopa.aca.AcaApplicationKt")
  buildInfo { properties { additional.set(mapOf("description" to project.description)) } }
}

tasks.create("applySemanticVersionPlugin") {
  dependsOn("prepareKotlinBuildScriptModel")
  apply(plugin = "com.dipien.semantic-version")
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
  kotlin {
    toggleOffOn()
    targetExclude("build/**/*")
    ktfmt().kotlinlangStyle()
  }
  kotlinGradle {
    toggleOffOn()
    targetExclude("build/**/*.kts")
    ktfmt().googleStyle()
  }
  java {
    target("**/*.java")
    targetExclude("build/**/*")
    eclipse().configFile("eclipse-style.xml")
    toggleOffOn()
    removeUnusedImports()
    trimTrailingWhitespace()
    endWithNewline()
  }
}

tasks.withType(JavaCompile::class.java).configureEach { options.encoding = "UTF-8" }

tasks.withType(Javadoc::class.java).configureEach { options.encoding = "UTF-8" }

tasks.named<Jar>("jar") { enabled = false }
