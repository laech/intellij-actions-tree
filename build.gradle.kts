import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  kotlin("jvm") version "1.5.30"
  id("org.jetbrains.intellij").version("1.1.4")
}

group = "com.gitlab.lae.intellij.actions.tree"
version = "0.5.5"

repositories {
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  testImplementation("org.mockito:mockito-core:3.11.2")
  testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
}

kotlin {
  jvmToolchain {
    (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
  }
}

tasks.withType<Test> {
  testLogging {
    exceptionFormat = TestExceptionFormat.FULL
  }
}

intellij {

  // See idea-version in plugin.xml
  version.set("2021.2.1")

  // If true this sets until-build to same major version which is not good
  updateSinceUntilBuild.set(false)
}
