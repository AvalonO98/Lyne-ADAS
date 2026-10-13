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

// 注意：APK 产物的最终文件名（LyneADAS-<版本名>-zh.apk）在打包阶段由
// .github/workflows/build.yml 完成，而不是在这里用 androidComponents 改写。
// 原因：AGP 8.7.3 的 com.android.build.api.variant.VariantOutput 上没有 outputFileName
// 成员（实测报 Unresolved reference），要在 Gradle 层改名只能用内部的
// BaseVariantOutputImpl，属于非公开 API。改在 CI 既稳妥又能保证发布出去的 APK 名字正确。
// 版本号仍然只有 appVersionName 这一个来源，CI 直接从 AGP 生成的
// app/build/outputs/apk/debug/output-metadata.json 读取，不会产生第二处定义。

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
