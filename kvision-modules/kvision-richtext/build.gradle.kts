buildscript {
    extra.set("production", (findProperty("prod") ?: findProperty("production") ?: "false") == "true")
}

plugins {
    kotlin("js")
    id("maven-publish")
}

repositories()

kotlin {
    kotlinJsTargets()
}

dependencies {
    implementation(kotlin("stdlib-js"))
    api(rootProject)
    implementation(npm("trix", "1.2.2"))
    testImplementation(kotlin("test-js"))
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(kotlin.sourceSets.main.get().kotlin)
}

publishing {
    publications {
        create<MavenPublication>("kotlin") {
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            pom {
                defaultPom()
            }
        }
    }
}

setupPublication()

fun copyResources() {
    copy {
        from("$buildDir/processedResources/Js/main")
        into("${rootProject.buildDir}/js/packages/kvision-${project.name}/kotlin")
    }
    copy {
        from("$buildDir/processedResources/Js/main")
        into("${rootProject.buildDir}/js/packages/kvision-${project.name}/kotlin-dce")
    }
}

tasks {
    getByName("JsJar", Jar::class) {
        from("${rootProject.buildDir}/js/packages/kvision-${project.name}/package.json")
    }
    getByName("compileTestKotlinJs") {
        doLast {
            copyResources()
        }
    }
    getByName("processDceKotlinJs") {
        doLast {
            copyResources()
        }
    }
}