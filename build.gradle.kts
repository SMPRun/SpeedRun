plugins {
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "net.smprun"
version = "1.0"

repositories {

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
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/SMPRun/flavor")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
    maven {
        name = "GitHubPackagesAware"
        url = uri("https://maven.pkg.github.com/SMPRun/aware")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
    maven {
        name = "GitHubPackagesCommon"
        url = uri("https://maven.pkg.github.com/SMPRun/Common")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    compileOnly("net.smprun:common:1.3")
    
    compileOnly("gg.scala.aware:aware:2.1.0")
    
    implementation("gg.scala.flavor:flavor:0.2.1")
    
    implementation("org.reflections:reflections:0.10.2")
    
    compileOnly("net.megavex:scoreboard-library-api:2.4.1")
    
    compileOnly("org.mongodb:mongodb-driver-kotlin-coroutine:5.5.1")
    
    compileOnly("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    
    compileOnly("com.tcoded:FoliaLib:0.5.1")
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
    archiveClassifier.set("all")
    
    // Relocate to match Common's relocation
    relocate("com.tcoded.folialib", "net.smprun.libs.folialib")
    
    // Exclude dependencies provided by Common plugin
    dependencies {
        exclude(dependency("org.jetbrains.kotlin:.*"))
        // Keep Flavor and Reflections - we bundle them separately in Speedrun
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
