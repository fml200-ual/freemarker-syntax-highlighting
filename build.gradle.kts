plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.20.1"
}

group = "dev.freemarker"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // Same policy as the JSP plugin: a local IDE when idePath/IDE_PATH is set, otherwise the
    // JetBrains repository.
    val idePath = providers.gradleProperty("idePath")
            .orElse(providers.environmentVariable("IDE_PATH"))

    intellijPlatform {
        if (idePath.isPresent) {
            local(idePath)
        } else {
            intellijIdeaCommunity("253.33813.120")
        }
    }
    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    buildSearchableOptions.set(false)
    instrumentCode.set(false)

    pluginConfiguration {
        id.set("dev.freemarker.highlight")
        name.set("FreeMarker Highlight")
        version.set(project.version.toString())

        ideaVersion {
            sinceBuild.set("253")
            untilBuild.set("263.*")
        }
    }
}

tasks.test {
    useJUnit()
    systemProperty("ftl.golden.update", providers.systemProperty("ftl.golden.update").getOrElse("false"))
    testLogging {
        events("passed", "failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
