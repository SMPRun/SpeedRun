plugins {
    kotlin("jvm") version "2.3.0-Beta1"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "net.smprun"
version = "1.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://repo.tcoded.com/releases") {
        name = "tcoded-releases"
    }
    maven("https://repo.aikar.co/content/groups/aikar/") {
        name = "aikar"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    compileOnly("net.smprun:common:1.0")
    
    compileOnly("net.megavex:scoreboard-library-api:2.4.1")
    
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.5.1")
    
    compileOnly("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    
    implementation("com.tcoded:FoliaLib:0.5.1")
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21")
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.shadowJar {
    mergeServiceFiles()
    relocate("com.tcoded.folialib", "net.smprun.libs.folialib")
    archiveClassifier.set("all")
    
    // Exclude Kotlin stdlib to avoid classloader conflicts with Common plugin
    dependencies {
        exclude(dependency("org.jetbrains.kotlin:.*"))
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}
