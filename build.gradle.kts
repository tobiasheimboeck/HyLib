import org.gradle.external.javadoc.StandardJavadocDocletOptions

allprojects {
    group = "net.neptunsworld.elytra.database"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    dependencies {
        compileOnly("com.google.code.gson:gson:2.10.1")
        compileOnly("org.projectlombok:lombok:1.18.22")
        annotationProcessor("org.projectlombok:lombok:1.18.22")

        compileOnly("org.mariadb.jdbc:mariadb-java-client:3.0.7")
        compileOnly("com.zaxxer:HikariCP:5.0.1")
        compileOnly("org.redisson:redisson:3.20.1")
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        javaCompiler = javaToolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<Javadoc>().configureEach {
        (options as StandardJavadocDocletOptions).encoding = "UTF-8"
    }

    val sourcesJar by tasks.registering(Jar::class) {
        from(sourceSets.main.get().allJava)
        archiveClassifier.set("sources")
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
