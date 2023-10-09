import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  id("java")
  id("org.jetbrains.intellij").version("1.15.0")
  id("org.jetbrains.kotlin.jvm").version("1.9.10")
  id("com.diffplug.spotless").version("6.21.0")
}

group = "com.gitlab.lae.intellij.actions.tree"

version = "0.6.3-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
  testImplementation("org.mockito:mockito-core:3.11.2")
  testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
  testImplementation("nz.lae.stacksrc:stacksrc-junit5:0.5.0")
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
  testImplementation("org.junit.vintage:junit-vintage-engine:5.10.0")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")
}

kotlin { jvmToolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }

intellij { version.set("2023.2") }

spotless {
  kotlin { ktfmt() }
  kotlinGradle { ktfmt() }
}

tasks.test {
  useJUnitPlatform()
  systemProperty("junit.jupiter.extensions.autodetection.enabled", true)
  testLogging { exceptionFormat = TestExceptionFormat.FULL }
}

tasks.patchPluginXml { untilBuild.set("") }
