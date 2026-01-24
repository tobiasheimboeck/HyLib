import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar

dependencies {
    implementation(project(":database-api"))
}

tasks.named<Jar>("jar") {
    from(
        configurations.runtimeClasspath.get().map { file ->
            if (file.isDirectory) file else zipTree(file)
        }
    )
    dependsOn(":database-api:jar")
    from(rootProject.file("LICENSE"))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks.named("sourcesJar"))
        }
    }

    repositories {
        maven {
            url = uri(
                if (project.version.toString().endsWith("SNAPSHOT")) {
                    "https://nexus.neptuns.world/repository/maven-snapshots/"
                } else {
                    "https://nexus.neptuns.world/repository/maven-releases/"
                }
            )

            credentials {
                username = findProperty("nexusUsername") as String?
                password = findProperty("nexusPassword") as String?
            }
        }
    }
}
