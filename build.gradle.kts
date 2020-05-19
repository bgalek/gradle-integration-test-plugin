import pl.allegro.tech.build.axion.release.domain.hooks.HookContext
import pl.allegro.tech.build.axion.release.domain.hooks.HooksConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

plugins {
    kotlin("jvm") version "1.3.72"
    id("jacoco")
    id("pl.allegro.tech.build.axion-release") version "1.11.0"
    id("com.github.kt3k.coveralls") version "2.10.1"
    id("com.gradle.plugin-publish") version "0.11.0"
    id("java-gradle-plugin")
    id("maven-publish")
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
}

repositories {
    jcenter()
}

ktlint {
    version.set("0.36.0")
    enableExperimentalRules.set(true)
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    testImplementation("org.assertj:assertj-core:3.16.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

scmVersion {
    versionCreator("versionWithBranch")
    hooks = HooksConfig().also {
        it.pre(
            "fileUpdate",
            mapOf(
                "files" to listOf("readme.md") as Any,
                "pattern" to KotlinClosure2<String, HookContext, String>({ v, _ -> v }),
                "replacement" to KotlinClosure2<String, HookContext, String>({ v, _ -> v })
            )
        )
        it.pre("commit", KotlinClosure2<String, ScmPosition, String>({ v, _ -> "Release: $v [ci skip]" }))
    }
}

group = "com.coditory.gradle"
version = scmVersion.version

tasks {
    withType<Test> {
        testLogging {
            events("passed", "failed", "skipped")
            setExceptionFormat("full")
        }
    }
    withType<Test> {
        useJUnitPlatform()
    }
    jacocoTestReport {
        reports {
            xml.isEnabled = true
            html.isEnabled = true
        }
    }
    coveralls {
        sourceDirs = listOf("src/main/kotlin")
    }
}

gradlePlugin {
    plugins {
        create("integrationTestPlugin") {
            id = "com.coditory.integration-test"
            implementationClass = "com.coditory.gradle.integration.IntegrationTestPlugin"
        }
    }
}

// Marking new version (incrementPatch [default], incrementMinor, incrementMajor)
// ./gradlew markNextVersion -Prelease.incrementer=incrementMinor
// Releasing the plugin:
// ./gradlew release && ./gradlew publishPlugins
pluginBundle {
    website = "https://github.com/coditory/gradle-integration-test-plugin"
    vcsUrl = "https://github.com/coditory/gradle-integration-test-plugin"
    description = "Gradle Plugin for integration tests"
    tags = listOf("test", "integration-test", "java-integration-test")

    (plugins) {
        "integrationTestPlugin" {
            displayName = "Integration test plugin"
        }
    }
}
