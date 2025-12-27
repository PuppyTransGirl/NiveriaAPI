plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("maven-publish")
}

val minecraftVersion: String by project
val placeholderApiVersion: String by project
val luckpermsVersion: String by project
val itemsAdderVersion: String by project
val worldguardVersion: String by project
val landsVersion: String by project
val bluemapVersion: String by project
val squaremapVersion: String by project
val dynmapVersion: String by project
val mongoDBVersion: String by project
val junitVersion: String by project
val mockbukkitVersion: String by project

group = "toutouchien.niveriaapi"
version = "2.1.0"

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.bluecolored.de/releases")
    maven("https://repo.mikeprimm.com/")
    maven("https://maven.devs.beer/")
    maven("https://jitpack.io")
    maven("https://api.modrinth.com/maven/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    paperweight.paperDevBundle("${minecraftVersion}-R0.1-SNAPSHOT")

    // Plugins
    compileOnly("me.clip:placeholderapi:${placeholderApiVersion}")
    compileOnly("net.luckperms:api:${luckpermsVersion}")
    compileOnly("dev.lone:api-itemsadder:${itemsAdderVersion}")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:${worldguardVersion}")
    compileOnly("com.github.angeschossen:LandsAPI:${landsVersion}")
    compileOnly("de.bluecolored:bluemap-api:${bluemapVersion}")
    compileOnly("xyz.jpenilla:squaremap-api:${squaremapVersion}")
    compileOnly("us.dynmap:dynmap-api:${dynmapVersion}")
    compileOnly("us.dynmap:DynmapCoreAPI:${dynmapVersion}")

    // Dependencies
    compileOnly("org.mongodb:mongodb-driver-sync:${mongoDBVersion}")

    // Test Dependencies
    testImplementation(paperweight.paperDevBundle("${minecraftVersion}-R0.1-SNAPSHOT"))
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:${mockbukkitVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter:${junitVersion}")
    testImplementation("org.mongodb:mongodb-driver-sync:${mongoDBVersion}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
            github("jpenilla", "TabTPS", "v1.3.29", "tabtps-paper-1.3.29.jar")
            modrinth("ServerLogViewer-Paper", "1.0.0")
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