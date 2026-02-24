import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.11.0"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        local("/Users/cwoolley/Applications/IntelliJ IDEA.app")
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
        testFramework(TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.16")
}

// Create a no-op coroutines agent jar to replace the incompatible one
// provided by the IntelliJ Platform Gradle Plugin. The plugin's agent
// version causes NoSuchMethodError with IntelliJ 2025.3's bundled coroutines.
val createNoopCoroutinesAgent by tasks.registering(Jar::class) {
    dependsOn(tasks.named("compileTestJava"))
    archiveFileName.set("coroutines-javaagent.jar")
    destinationDirectory.set(layout.buildDirectory.dir("noop-agent"))
    from(tasks.named("compileTestJava").map { (it as JavaCompile).destinationDirectory }) {
        include("NoopAgent.class")
    }
    manifest {
        attributes("Premain-Class" to "NoopAgent")
    }
}

// Copy the no-op agent over the incompatible one before tests run
val replaceCoroutinesAgent by tasks.registering(Copy::class) {
    dependsOn(createNoopCoroutinesAgent)
    from(createNoopCoroutinesAgent.map { it.archiveFile })
    into(file(".intellijPlatform"))
}

tasks.test {
    dependsOn(replaceCoroutinesAgent)
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }
    }
}
