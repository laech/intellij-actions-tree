import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  id("java")
  id("org.jetbrains.intellij.platform").version("2.2.1")
  id("org.jetbrains.kotlin.jvm").version("2.0.0")
  id("com.diffplug.spotless").version("6.25.0")
}

group = "com.gitlab.lae.intellij.actions.tree"

version = "0.7.1-SNAPSHOT"

repositories {
  mavenCentral()
  intellijPlatform { defaultRepositories() }
}

dependencies {
  intellijPlatform { intellijIdeaCommunity("2024.3") }
  testImplementation("org.mockito:mockito-core:3.11.2")
  testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
  testImplementation("nz.lae.stacksrc:stacksrc-junit5:0.5.1")
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
  testImplementation("org.junit.vintage:junit-vintage-engine:5.10.0")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")
}

kotlin { jvmToolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }

spotless {
  kotlin { ktfmt().googleStyle() }
  kotlinGradle { ktfmt() }
}

tasks.test {
  useJUnitPlatform()
  systemProperty("junit.jupiter.extensions.autodetection.enabled", true)
  testLogging { exceptionFormat = TestExceptionFormat.FULL }
}

intellijPlatform { pluginConfiguration { ideaVersion { untilBuild = provider { null } } } }
