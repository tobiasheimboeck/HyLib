import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar

dependencies {
    // Nur database-api für die Annotationen
    implementation(project(":database-api"))
    
    // Annotation Processing API ist Teil von Java, keine extra Dependency nötig
}

tasks.named<Jar>("jar") {
    from(rootProject.file("LICENSE"))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

// Maven Publishing für GitHub Packages
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            groupId = project.group.toString()
            artifactId = "database-processor"
            version = project.version.toString()
            
            pom {
                name.set("Database Processor")
                description.set("Annotation processor for automatic codec generation")
                url.set("https://github.com/${project.findProperty("github.owner")}/${project.findProperty("github.repo")}")
                
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                
                developers {
                    developer {
                        id.set("TGamings")
                        name.set("Tobias Heimboeck")
                        email.set("tobias.heimboeck@nexalit.at")
                    }
                }
            }
        }
    }
    
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/${project.findProperty("github.owner")}/${project.findProperty("github.repo")}")
            credentials {
                username = project.findProperty("github.username") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("github.token") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
