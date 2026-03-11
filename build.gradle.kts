import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec


plugins {
    java
    application
    `maven-publish`
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
    mavenCentral()  // 외부 라이브러리 다운로드
}

configurations {
    create("proguard")
}

dependencies {
    // YAML 파싱용 SnakeYAML
    implementation("org.yaml:snakeyaml:2.1")

    implementation("org.springframework:spring-context:6.1.5")
    implementation("org.springframework:spring-beans:6.1.5")
    implementation("org.springframework:spring-core:6.1.5")

    implementation("org.openjfx:javafx-controls:21")
    implementation("org.openjfx:javafx-fxml:21")


    // ProGuard
    add("proguard", "com.guardsquare:proguard-base:7.5.0")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("com.example.AppLauncher")
}


/*
  build 라는 행위가 끝나면 finalizedBy를 통하여 deployAllLibs 를 실행한다.
 */
tasks.named("build") {
    finalizedBy("deployAllLibs")
}

/*
 deployAllLibs 변수 이름으로 실행하는 것 같아 문법 확인 필요.
 */
val deployAllLibs by tasks.registering(Copy::class) {
    group = "deploy"
    description = "Copy all runtime dependencies to lib folder"

    from(configurations.runtimeClasspath)
    into(layout.buildDirectory.dir("libs"))
}

val javaLauncherProvider = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(21))
}

val proguardJar by tasks.registering(JavaExec::class) {
    group = "build"
    description = "Obfuscate JAR using ProGuard"

    dependsOn(tasks.named("jar")) // jar 생성 후 실행

    // ProGuard main class
    mainClass.set("proguard.ProGuard")

    // ProGuard classpath
    classpath = configurations["proguard"]

        val inputJar = file("$buildDir/libs/${project.name}-${version}.jar")
        val outputJar = file("$buildDir/libs/${project.name}-${version}-obf.jar")

        // Java 21 기준 jmod 절대 경로
        val javaHome = javaLauncherProvider.get().metadata.installationPath
        val javaBaseJmod = file("$javaHome/jmods/java.base.jmod") // lib 제거
        if (!javaBaseJmod.exists()) throw GradleException("java.base.jmod not found at $javaBaseJmod")

    args(
        "-injars", inputJar.absolutePath,
        "-outjars", outputJar.absolutePath,
        "-libraryjars", javaBaseJmod.absolutePath,
        "-dontwarn",
        "-dontoptimize",
        "-dontobfuscate",
        "-keepclasseswithmembers public class * { public static void main(java.lang.String[]); }",
        "-keepclassmembers class com.example.MainApp { public void start(javafx.stage.Stage); }",
        "-keep class com.example.MainApp extends javafx.application.Application"
    )

//    args(
//        "@proguard.pro" // 파일에 정의된 규칙 모두 사용
//    )
//        val proguardFile = file("proguard.pro")
//        val tempProguard = file("$buildDir/proguard-temp.pro")
//
//        tempProguard.writeText("\n-injars ${inputJar.absolutePath}\n-outjars ${outputJar.absolutePath}\n-libraryjars ${javaBaseJmod.absolutePath}\n")
//
//        tempProguard.appendText(
//            proguardFile.readText()  // 원본 규칙 복사
//        )

//        // 1. 기존 tempProguard 덮어쓰기
//        tempProguard.writeText(
//            proguardFile.readText()  // 원본 규칙 복사
//        )
//
//        // 2. injars/outjars/libraryjars 추가
//        tempProguard.appendText("\n-injars ${inputJar.absolutePath}\n-outjars ${outputJar.absolutePath}\n-libraryjars ${javaBaseJmod.absolutePath}\n")

//        tempProguard.appendText(
//            """
//        -injars ${inputJar.absolutePath}
//        -outjars ${outputJar.absolutePath}
//        -libraryjars ${javaBaseJmod.absolutePath}
//    """.trimIndent()
//        )

//        args("@${tempProguard.absolutePath}")

    javaLauncher.set(javaLauncherProvider)
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "com.example.AppLauncher"
        )
    }
}
// -------------------------------
// JPackage로 exe 생성 (JDK 포함)
// -------------------------------
tasks.register<Exec>("packageExe") {
    group = "build"
    dependsOn("creteKey")
//    dependsOn("proguardJar") // 난독화된 JAR을 사용

    val fatJarFile = file("build/libs/Study-1.0.0.jar")
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
//
//tasks.register<Exec>("runObfJar") {
//    group = "application"
//    description = "Run the already obfuscated JAR"
//    workingDir = project.projectDir
//
//    workingDir = project.projectDir
//
//    val separator = if (System.getProperty("os.name").lowercase().contains("win")) ";" else ":"
//
//    // libs 폴더에 있는 모든 JAR 포함 (JavaFX 포함)
//    val libsJars = fileTree("libs") { include("*.jar") }.map { it.absolutePath }
//
//    // 난독화 JAR
//    val obfJar = file("build/libs/Study-1.0.0-obf.jar").absolutePath
//
//    // 전체 클래스패스 구성
//    val classpath = (libsJars + obfJar).joinToString(separator)
//
//    // main 클래스는 난독화된 main 클래스명으로 변경 필요
//    commandLine(
//        "java",
//        "-cp",
//        classpath,
//        "com.example.AppLauncher" // 실제 난독화된 메인 클래스 이름
//    )
//}
//
//tasks.register<Exec>("runApp") {
//    group = "application"
//
//    workingDir = project.projectDir
//
//    val separator = if (System.getProperty("os.name").lowercase().contains("win")) ";" else ":"
//
//    // libs 폴더에 있는 모든 JAR 포함 (JavaFX 포함)
//    val libsJars = fileTree("libs") { include("*.jar") }.map { it.absolutePath }
//
//    // 난독화 JAR
//    val obfJar = file("build/libs/Study-1.0.0-obf.jar").absolutePath
//
//    // 전체 클래스패스 구성
//    val classpath = (libsJars + obfJar).joinToString(separator)
//
//    // 실제 난독화된 main 클래스 이름
//    commandLine(
//        "java",
//        "-cp",
//        classpath,
//        "com.example.AppLauncher"  // <- 난독화된 Main 클래스 이름 확인
//    )
//    // Optional: 환경 변수 설정
//    environment("JAVA_TOOL_OPTIONS", "-Djava.net.preferIPv4Stack=true")
//    environment("_JAVA_OPTIONS", "-Xmx1024M")
//}

/////////////////////////////// 임의값 생성 로직 ///////////////////

//// 새로 추가하는 buildscript


fun generateKey(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    val random = SecureRandom()

    return (1..16)
        .map { chars[random.nextInt(chars.length)] }
        .joinToString("")
}


//tasks.register("encryptYaml") {
//
//    doLast {
//
//        val key = generateKey()
//        println("Generated KEY = $key")
//
//        val yaml = Yaml()
//
//        val file = file("src/main/resources/application.yml")
//        val data = yaml.load<MutableMap<String, Any>>(file.readText())
//
//        val database = data["database"] as MutableMap<String, Any>
//        val password = database["password"] as String
//
//        val encrypted = crypto.AesUtil.encrypt(key, password)
//
//        database["password"] = "ENC($encrypted)"
//
//        val outFile = file("$buildDir/generated/application.yml")
//
//        outFile.parentFile.mkdirs()
//        outFile.writeText(yaml.dump(data))
//
//        extra["runtimeKey"] = key
//    }
//}

tasks.register("creteKey") {
    doLast {
        // 1. 임의 키 생성 (AES)
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(128)
        val secretKey = keyGen.generateKey()
        println("SecretKey length: ${secretKey.encoded.size}")
        val encodedKey = Base64.getEncoder().encodeToString(secretKey.encoded)
        println("Generated Key: $encodedKey")

        // 2. yml 파일 읽기
        //val ymlFile = file("src/main/resources/config.yml")
        val ymlFile = layout.buildDirectory.file("resources/main/config.enc").get().asFile
        val ymlContent = ymlFile.readText()

        // 3. AES 암호화
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(ymlContent.toByteArray(Charsets.UTF_8))
        val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes)

        // 4. 암호화된 파일 저장 (exe에 포함될 resources)
        //val outputFile = file("$buildDir/resources/main/config.enc")
        val outputFile = layout.buildDirectory.file("resources/main/config.enc").get().asFile
        outputFile.writeText(encryptedBase64)

        println("Encrypted config saved to: ${outputFile.absolutePath}")

        // 5. 임의값을 exe 패키징 과정에 전달 (환경변수, 혹은 build config)
        project.extensions.extraProperties["ENCRYPTION_KEY"] = encodedKey
    }
}

tasks.named<ProcessResources>("processResources") {
    exclude("config.yml")  // 이 파일은 jar에 포함되지 않음
}
tasks.named<JavaExec>("run") {

    dependsOn("creteKey")

    doFirst {
        // 이제 creteKey에서 만든 ENCRYPTION_KEY가 존재
        val key = project.extensions.extraProperties["ENCRYPTION_KEY"]
        systemProperty("ENCRYPTION_KEY", key)
        println("AES_KEY from Gradle extraProperties: $key")

    }
}
