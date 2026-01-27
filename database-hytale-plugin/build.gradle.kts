import org.gradle.api.file.DuplicatesStrategy
import org.gradle.jvm.tasks.Jar

dependencies {
    implementation(project(":database-common"))
    implementation(project(":database-api"))
    compileOnly(libs.hytale.server)
    
    compileOnly(libs.lombok)
    compileOnly(libs.hikaricp)
    annotationProcessor(libs.lombok)
    annotationProcessor(project(":database-processor"))
}

tasks.named<Jar>("jar") {
    from(
        configurations.runtimeClasspath.get().map { file ->
            if (file.isDirectory) file else zipTree(file)
        }
    )
    dependsOn(":database-common:jar")
    from(rootProject.file("LICENSE"))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
