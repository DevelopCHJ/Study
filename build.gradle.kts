plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.14"
}

group = "com.example"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-context:6.1.5")
    implementation("org.springframework:spring-beans:6.1.5")
    implementation("org.springframework:spring-core:6.1.5")

    implementation("org.openjfx:javafx-controls:21")
    implementation("org.openjfx:javafx-fxml:21")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("com.example.AppLauncher")
}


//// -------------------------------
//// FatJar 생성
//// -------------------------------
//tasks.register<Jar>("fatJar") {
//    archiveBaseName.set("${project.name}-all")
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//
//    manifest {
//        attributes["Main-Class"] = "com.example.AppLauncher"
//    }
//
//    from(sourceSets.main.get().output)
//
//    // runtime 의존성 포함
//    dependsOn(configurations.runtimeClasspath)
//    from({
//        configurations.runtimeClasspath.get()
//            .filter { it.name.endsWith("jar") }
//            .map { zipTree(it) }
//    })
//}
//
//// -------------------------------
//// JLink로 최소 런타임 생성
//// -------------------------------
//val javafxModulePath = configurations.runtimeClasspath.get()
//    .filter { it.name.startsWith("javafx") }
//    .joinToString(";") { it.absolutePath }
//tasks.register<Exec>("createRuntime") {
//    dependsOn("fatJar")
//
//    // IntelliJ에 설정된 JDK bin 경로를 활용
//    val javaHome = org.gradle.internal.jvm.Jvm.current().javaHome.absolutePath
//    println("Using JDK: $javaHome")
//
//    // 최소 runtime image 생성
//    commandLine(
//        "$javaHome/bin/jlink",
//        "--module-path", "$javaHome/jmods;$javafxModulePath",
//        "--add-modules", "java.base,java.desktop,java.logging,javafx.controls,javafx.fxml",
//        "--output", "build/image",
//        "--strip-debug",
//        "--compress", "2",
//        "--no-header-files",
//        "--no-man-pages"
//    )
//}
//
//// -------------------------------
//// jpackage로 exe 생성
//// -------------------------------
//tasks.register<Exec>("packageExe") {
//    dependsOn("createRuntime")
//
//    commandLine(
//        "jpackage",
//        "--input", "build/libs",
//        "--name", "Study",
//        "--main-jar", tasks.named<Jar>("fatJar").get().archiveFileName.get(),
//        "--main-class", "com.example.AppLauncher",
//        "--type", "exe",
//        "--win-shortcut",
//        "--win-menu",
//        "--dest", "build/installer",         // 옵션과 값을 분리
//        "--runtime-image", "build/image"    // 옵션과 값을 분리
//    )
//}

// -------------------------------
// FatJar 생성 (모든 의존성 포함)
// -------------------------------
tasks.register<Jar>("fatJar") {
    archiveBaseName.set("${project.name}-all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "com.example.AppLauncher"
    }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
}

// -------------------------------
// JPackage로 exe 생성 (JDK 포함)
// -------------------------------
tasks.register<Exec>("packageExe") {
    dependsOn("fatJar")

    val fatJarFile = tasks.named<Jar>("fatJar").get().archiveFile.get().asFile
    val javaHome = org.gradle.internal.jvm.Jvm.current().javaHome.absolutePath

    commandLine(
        "jpackage",
        "--input", fatJarFile.parent,
        "--name", "Study",
        "--main-jar", fatJarFile.name,
        "--main-class", "com.example.AppLauncher",
        "--type", "exe",
        "--win-shortcut",
        "--win-menu",
        "--dest", "build/installer",
        "--runtime-image", javaHome,   // JDK 전체 포함
        "--verbose"
    )
}
