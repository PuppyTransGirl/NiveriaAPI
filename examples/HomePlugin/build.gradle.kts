plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

val minecraftVersion: String by project
val niveriaApiVersion: String by project

group = "toutouchien.homeplugin"
version = "1.0.0"

repositories {
    mavenLocal()

    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("${minecraftVersion}-R0.1-SNAPSHOT")

    compileOnly("toutouchien.niveriaapi:NiveriaAPI:${niveriaApiVersion}")
}

paperweight {
    addServerDependencyTo = configurations.named(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME).map { setOf(it) }
}

tasks {
    runServer {
        minecraftVersion(minecraftVersion)

        jvmArgs(
            "-Xmx4096M",
            "-Xms4096M",
            "-XX:+AllowEnhancedClassRedefinition",
            "-XX:HotswapAgent=core",
            "-Dcom.mojang.eula.agree=true"
        )

        downloadPlugins {
            modrinth("LuckPerms", "v5.5.17-bukkit")
        }
    }

    build {
        dependsOn("jar")
    }

    test {
        useJUnitPlatform()
    }

    javadoc {
        isFailOnError = false
        options.encoding = "UTF-8"
    }

    register<Jar>("javadocJar") {
        dependsOn(javadoc)
        archiveClassifier.set("javadoc")
        from(javadoc.get().destinationDir)
    }

    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    processResources {
        filteringCharset = "UTF-8"

        val props = mapOf(
            "version" to version,
            "minecraftVersion" to minecraftVersion
        )

        inputs.properties(props)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}

artifacts {
    add("archives", tasks.named("sourcesJar"))
    add("archives", tasks.named("javadocJar"))
}