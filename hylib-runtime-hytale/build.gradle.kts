import org.gradle.api.file.DuplicatesStrategy
import org.gradle.jvm.tasks.Jar

// Disable sources jar for this plugin
tasks.named("sourcesJar") {
    enabled = false
}

dependencies {
    compileOnly(project(":database-api"))
    implementation(project(":database-common"))

    implementation(project(":hytale-api"))
    implementation(project(":hytale-common"))
    compileOnly(libs.hytale.server)
    
    compileOnly(libs.lombok)
    compileOnly(libs.hikaricp)
    annotationProcessor(libs.lombok)
}

tasks.named<Jar>("jar") {
    isZip64 = true
    // Include all runtime dependencies except Hytale Server and lombok
    from(
        configurations.runtimeClasspath.get().filter { file ->
            val fileName = file.name.lowercase()
            // Exclude only Hytale Server JARs and lombok
            // Our project modules (hytale-api, hytale-common, database-*) will be included
            !fileName.contains("server") && 
            !fileName.contains("lombok")
        }.map { file ->
            if (file.isDirectory) {
                file
            } else {
                zipTree(file).matching {
                    // Exclude unnecessary metadata files that cause too many entries
                    exclude("META-INF/versions/**")
                    exclude("META-INF/maven/**")
                    exclude("META-INF/DEPENDENCIES")
                    exclude("META-INF/LICENSE*")
                    exclude("META-INF/NOTICE*")
                    exclude("META-INF/ECLIPSEF.SF")
                    exclude("META-INF/ECLIPSEF.RSA")
                    exclude("META-INF/*.SF")
                    exclude("META-INF/*.RSA")
                    exclude("META-INF/*.DSA")
                    exclude("module-info.class")
                }
            }
        }
    )
    dependsOn(":database-common:jar")
    dependsOn(":database-api:jar")
    dependsOn(":hytale-api:jar")
    dependsOn(":hytale-common:jar")
    from(rootProject.file("LICENSE"))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
