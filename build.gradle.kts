import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
  }
}

plugins {
  id("org.jetbrains.kotlin.jvm") version "1.3.72"
  id("org.jetbrains.intellij") version "0.4.21"
}

group = "com.gitlab.lae.intellij.actions.tree"
version = "0.4-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  testImplementation("org.mockito:mockito-core:3.4.0")
  testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
}

tasks {
  withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.compilerArgs.addAll(listOf("--release", "8"))
  }

  withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "1.8"
    }
  }

  test {
    testLogging {
      exceptionFormat = TestExceptionFormat.FULL
    }
  }

  intellij {
    version = project.properties["intellijVersion"] as String? ?: "2019.3"
    updateSinceUntilBuild = false
  }
}
