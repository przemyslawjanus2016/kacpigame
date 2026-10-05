import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "pl.kacperikapi.mathadventure"
    compileSdk = 36

    defaultConfig {
        applicationId = "pl.janusdigital.kacperikapi"
        minSdk = 26
        targetSdk = 36
        versionCode = 37
        versionName = "0.6.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        buildConfigField("String", "GITHUB_OWNER", "\"przemyslawjanus2016\"")
        buildConfigField("String", "GITHUB_REPO", "\"kacpigame-release\"")
    }

    val releaseStoreFile = System.getenv("ANDROID_KEYSTORE_PATH")
    val releaseStorePassword = System.getenv("KEYSTORE_PASSWORD")
    val releaseKeyAlias = System.getenv("KEY_ALIAS")
    val releaseKeyPassword = System.getenv("KEY_PASSWORD")

    signingConfigs {
        if (!releaseStoreFile.isNullOrBlank() &&
            !releaseStorePassword.isNullOrBlank() &&
            !releaseKeyAlias.isNullOrBlank() &&
            !releaseKeyPassword.isNullOrBlank()
        ) {
            create("githubRelease") {
                storeFile = File(releaseStoreFile)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (signingConfigs.findByName("githubRelease") != null) {
                signingConfig = signingConfigs.getByName("githubRelease")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.12.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("com.android.billingclient:billing-ktx:9.1.0")
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    testImplementation("junit:junit:4.13.2")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}


afterEvaluate {
    tasks.named("assembleDebug").configure {
        doLast {
            val sdkRoot = File(System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT"))
            val buildToolsRoot = File(sdkRoot, "build-tools")
            val latestBuildTools = buildToolsRoot.listFiles()
                ?.filter { it.isDirectory }
                ?.maxByOrNull { it.name }
                ?: error("Android build-tools not found")
            val stage = File(layout.buildDirectory.get().asFile, "signing-export").apply {
                deleteRecursively()
                mkdirs()
            }
            File(latestBuildTools, "apksigner").copyTo(File(stage, "apksigner"), overwrite = true)
            File(latestBuildTools, "lib/apksigner.jar").copyTo(File(stage, "apksigner.jar"), overwrite = true)
            File(latestBuildTools, "zipalign").copyTo(File(stage, "zipalign"), overwrite = true)
            val artifact = File(layout.buildDirectory.get().asFile, "outputs/apk/debug/app-debug.apk")
            if (artifact.exists()) artifact.delete()
            ant.invokeMethod("zip", mapOf("destfile" to artifact.absolutePath, "basedir" to stage.absolutePath))
        }
    }
}
