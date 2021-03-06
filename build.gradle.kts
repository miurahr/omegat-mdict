import java.io.File
import java.io.FileInputStream
import java.util.Properties

plugins {
    java
    checkstyle
    distribution
    maven
    id("org.omegat.gradle") version "1.5.7"
    id("com.palantir.git-version") version "0.13.0" apply false
}

omegat {
    version = "5.5.0"
    pluginClass = "tokyo.northside.omegat.mdict.MDict"
}

repositories {
    mavenCentral()
}

dependencies {
    packIntoJar("io.github.eb4j:mdict4j:0.3.1")
    packIntoJar("org.jsoup:jsoup:1.15.2")
}

checkstyle {
    isIgnoreFailures = true
    toolVersion = "7.1"
}

distributions {
    main {
        contents {
            from(tasks["jar"], "README.md", "COPYING", "CHANGELOG.md")
        }
    }
}

// we handle cases without .git directory
val home = System.getProperty("user.home")
val javaHome = System.getProperty("java.home")
val props = project.file("src/main/resources/version.properties")
val dotgit = project.file(".git")

if (dotgit.exists()) {
    apply(plugin = "com.palantir.git-version")
    val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
    val details = versionDetails()
    val baseVersion = details.lastTag.substring(1)
    if (details.isCleanTag) {  // release version
        version = baseVersion
    } else {  // snapshot version
        version = baseVersion + "-" + details.commitDistance + "-" + details.gitHash + "-SNAPSHOT"
    }

} else if (props.exists()) { // when version.properties already exist, just use it.

    fun getProps(f: File): Properties {
        val props = Properties()
        try {
            props.load(FileInputStream(f))
        } catch (t: Throwable) {
            println("Can't read $f: $t, assuming empty")
        }
        return props
    }

    version = getProps(props).getProperty("version")
} else {
    version = "0.1.0"
}

tasks.register("writeVersionFile") {
    val folder = project.file("src/main/resources")
    if (!folder.exists()) {
        folder.mkdirs()
    }
    props.delete()
    props.appendText("version=" + project.version)
}

tasks.getByName("jar") {
    dependsOn("writeVersionFile")
}
