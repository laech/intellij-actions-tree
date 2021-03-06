import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.5.20"
  id("org.jetbrains.intellij").version("1.1.2")
}

group = "com.gitlab.lae.intellij.actions.tree"
version = "0.5.2-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  testImplementation("org.mockito:mockito-core:3.11.2")
  testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "11"
  kotlinOptions.jdkHome = javaToolchains
    .compilerFor { languageVersion.set(JavaLanguageVersion.of(11)) }
    .get().metadata.installationPath.asFile.absolutePath
}

tasks.withType<Test> {
  testLogging {
    exceptionFormat = TestExceptionFormat.FULL
  }
}

intellij {
  version.set("2021.1")
}
