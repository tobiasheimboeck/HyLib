import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.tasks.Jar
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test

allprojects {
    group = "dev.spacetivity.tobi.database"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
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
