plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
    id("io.papermc.paperweight.patcher") version "1.3.9"
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

repositories {
    mavenCentral()
    maven(paperMavenPublicUrl) {
        content { onlyForConfigurations(configurations.paperclip.name) }
    }
}

dependencies {
    remapper("net.fabricmc:tiny-remapper:0.8.2:fat")
    decompiler("net.minecraftforge:forgeflower:1.5.605.7")
    paperclip("io.papermc:paperclip:3.0.2")
}

val upstreamRepos = HashMap<String, String>()
upstreamRepos["paper"] = "PaperMC/Paper"
upstreamRepos["purpur"] = "PurpurMC/Purpur"

paperweight {
    serverProject.set(project(":abomination-server"))

    remapRepo.set(paperMavenPublicUrl)
    decompileRepo.set(paperMavenPublicUrl)

    val upstream = providers.gradleProperty("upstream").get().toLowerCase()

    useStandardUpstream(upstream) {
        url.set("https://github.com/" + upstreamRepos[upstream])
        ref.set(providers.gradleProperty(upstream + "Ref"))

        withStandardPatcher {
            baseName(upstream)

            apiPatchDir.set(layout.projectDirectory.dir("patches/api"))
            apiOutputDir.set(layout.projectDirectory.dir("abomination-api"))

            serverPatchDir.set(layout.projectDirectory.dir("patches/server"))
            serverOutputDir.set(layout.projectDirectory.dir("abomination-server"))
        }
    }
}

for (area in listOf("Api", "Server")) {
    tasks.getByName("apply" + area + "Patches").doFirst {
        val upstream = providers.gradleProperty("upstream").get()
        val patchesDir = layout.projectDirectory.dir("patches/" + area.toLowerCase()).asFile

        // Clean up old patches first
        patchesDir.listFiles()?.forEach {
            val patchName = it.name.substring(it.name.indexOf("-") + 1)
            for (upstreamName in upstreamRepos.keys) {
                if (!(patchName.toLowerCase().startsWith(upstreamName.toLowerCase()))) continue;

                it.delete()
            }
        }

        val upstreamPatchesDir = File(patchesDir.parent, upstream.toLowerCase() + "-" + patchesDir.name)

        if (upstreamPatchesDir.isDirectory) {
            upstreamPatchesDir.listFiles()?.forEach {
                it.copyTo(File(patchesDir, it.name))
            }
        }
    }

    tasks.getByName("rebuild" + area + "Patches").doLast {
        val upstream = providers.gradleProperty("upstream").get()
        val patchesDir = layout.projectDirectory.dir("patches/" + area.toLowerCase()).asFile

        val upstreamPatchesDir = File(patchesDir.parent, upstream.toLowerCase() + "-" + patchesDir.name)

        if (!upstreamPatchesDir.isDirectory) {
            upstreamPatchesDir.mkdir()
        }

        // Clean up old patches first
        upstreamPatchesDir.listFiles()?.forEach {
            it.delete()
        }

        patchesDir.listFiles()?.forEach {
            val patchName = it.name.substring(it.name.indexOf("-") + 1)
            if (!(patchName.toLowerCase().startsWith(upstream.toLowerCase()))) return@forEach;

            it.renameTo(File(upstreamPatchesDir, it.name))
        }
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}

subprojects {
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
    }
}
