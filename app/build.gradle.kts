plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// 版本号的单一来源：defaultConfig 与产物文件名共用，避免两处改漏而不一致。
val appVersionCode = 1
val appVersionName = "1.0"

android {
    namespace = "com.lyne.adas.l1"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lyne.adas.l1"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName

        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        // TFLite/LiteRT ship native .so per ABI; never compress .tflite model assets.
        jniLibs { useLegacyPackaging = false }
    }
    androidResources {
        noCompress += "tflite"
    }
}

// 产物文件名：LyneADAS-<版本名>-zh.apk（例如 LyneADAS-1.0-zh.apk）。
// 用 androidComponents 的 outputFileName，而不是 archivesBaseName：后者在 AGP 8 已不适用于
// 单独控制 APK 文件名，outputFileName 才是正式 API。
// 注意：这里用 appVersionName（不含 debug 的 versionNameSuffix），因此 debug 与 release
// 产出同名文件，但位于各自 buildType 的输出目录下，不会冲突。
androidComponents {
    onVariants(selector().all()) { variant ->
        variant.outputs.forEach { output ->
            output.outputFileName.set("LyneADAS-$appVersionName-zh.apk")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.video)

    implementation(libs.play.services.location)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.litert)
    implementation(libs.litert.gpu)
    implementation(libs.litert.support)
    implementation(libs.litert.metadata)

    implementation(libs.kotlinx.coroutines.android)
}
