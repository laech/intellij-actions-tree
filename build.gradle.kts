import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  id("java")
  id("org.jetbrains.intellij").version("1.14.1")
  id("org.jetbrains.kotlin.jvm").version("1.8.22")
  id("com.diffplug.spotless").version("6.19.0")
}

group = "com.gitlab.lae.intellij.actions.tree"

version = "0.6.1"

repositories { mavenCentral() }

dependencies {
  testImplementation("org.mockito:mockito-core:3.11.2")
  testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
}

kotlin { jvmToolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }

intellij { version.set("2023.1") }

spotless {
  kotlin { ktfmt() }
  kotlinGradle { ktfmt() }
}

tasks {
  test { testLogging { exceptionFormat = TestExceptionFormat.FULL } }
  patchPluginXml { untilBuild.set("") }
}
