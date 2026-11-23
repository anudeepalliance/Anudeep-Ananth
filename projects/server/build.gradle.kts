plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(ktorLibs.plugins.ktor)
}

group = "com.anudeepananth.projects"
version = "1.0.0"

application {
    mainClass = "com.anudeepananth.projects.ApplicationKt"
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":projects:shared"))
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.statusPages)
    implementation(ktorLibs.server.callLogging)
    implementation(ktorLibs.server.compression)
    implementation(ktorLibs.server.sse)
    implementation(ktorLibs.server.autoHeadResponse)
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.client.core)
    implementation(ktorLibs.client.cio)
    implementation(ktorLibs.client.contentNegotiation)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.mongodb.driver.kotlin.coroutine)
    implementation(libs.mongodb.bson.kotlinx)
    implementation(libs.koog.agents.projects)
    implementation(libs.koog.prompt.executor.openrouter.projects)
    implementation(libs.logback.classic)

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.test {
    useJUnitPlatform()
}
