import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.tasks.Jar
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test

allprojects {
    group = "dev.spacetivity.tobi.hylib"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven {
            name = "hytale-release"
            url = uri("https://maven.hytale.com/release")
        }
        maven {
            name = "hytale-pre-release"
            url = uri("https://maven.hytale.com/pre-release")
        }
        // GitHub Packages Repository (für lokale Entwicklung)
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/${findProperty("github.owner")}/${findProperty("github.repo")}")
            credentials {
                username = findProperty("github.username") as String? ?: System.getenv("GITHUB_ACTOR")
                password = findProperty("github.token") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(rootProject.libs.versions.java.get().toInt()))
        }
        withSourcesJar()
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    tasks.withType<Javadoc>().configureEach {
        (options as StandardJavadocDocletOptions).encoding = "UTF-8"
    }


    tasks.named<Jar>("jar") {
        manifest {
            attributes(
                mapOf(
                    "Implementation-Version" to project.version
                )
            )
        }
    }
}
