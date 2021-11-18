plugins {
    java
    checkstyle
    distribution
    maven
    id("org.omegat.gradle") version "1.5.3"
}

version = "0.1.0"

omegat {
    version = "5.5.0"
    pluginClass = "tokyo.northside.omegat.mdict.MDict"
}

dependencies {
    packIntoJar("io.github.eb4j:mdict4j:0.1.2")
    packIntoJar("org.jsoup:jsoup:1.14.3")
    testImplementation("junit:junit:4.13.2")
}

repositories {
    mavenCentral()
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
