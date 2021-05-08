import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.jetbrains.kotlin.jvm").version("1.5.0")
  id("org.jetbrains.intellij").version("0.7.3")
}

group = "com.gitlab.lae.intellij.actions.tree"
version = "0.4.1-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  testImplementation("org.mockito:mockito-core:3.4.0")
  testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
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
  version = "2021.1"
  updateSinceUntilBuild = false
}
