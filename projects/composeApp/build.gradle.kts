import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

/** Port for the local web dev server (`./gradlew :composeApp:wasmJsBrowserDevelopmentRun`). */
val localWebDevelopmentServerPort = 9495

/** Address of the local Ktor server; the web dev server forwards every `/api` request to it. */
val localKtorServerAddress = "http://localhost:9494"

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
}

compose.resources {
    publicResClass = false
    packageOfResClass = "com.anudeepananth.projects.resources"
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).copy(
                    port = localWebDevelopmentServerPort,
                    proxy = mutableListOf(
                        KotlinWebpackConfig.DevServer.Proxy(
                            context = mutableListOf("/api"),
                            target = localKtorServerAddress,
                        ),
                    ),
                )
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":projects:shared"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(ktorLibs.client.core)
        }
        wasmJsMain.dependencies {
            implementation(ktorLibs.client.js)
        }
    }
}
