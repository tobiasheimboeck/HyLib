import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar

dependencies {
    implementation(project(":database-api"))
    
    implementation(libs.mariadb.jdbc)
    implementation(libs.hikaricp)
    implementation(libs.gson)
    
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
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

// Maven Publishing für GitHub Packages
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            groupId = project.group.toString()
            artifactId = "database-common"
            version = project.version.toString()
            
            pom {
                name.set("Database Common")
                description.set("Default implementations for Database API")
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
