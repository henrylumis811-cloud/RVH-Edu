plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.henrylumis.rvhgrader"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.henrylumis.rvhgrader"
        minSdk = 24
        targetSdk = 36
        versionCode = 105
        versionName = "1.1.9"
    }

    // CI can use a persistent signing key so subsequent APKs install as updates rather than
    // being rejected because GitHub-hosted runners generate a different debug key each run.
    // Never commit the keystore or passwords to source control.
    val ciKeystorePath = System.getenv("RVH_CI_KEYSTORE_PATH")
    val ciStorePassword = System.getenv("RVH_CI_STORE_PASSWORD")
    val ciKeyAlias = System.getenv("RVH_CI_KEY_ALIAS")
    val ciKeyPassword = System.getenv("RVH_CI_KEY_PASSWORD")
    val releaseKeystorePath = System.getenv("RVH_RELEASE_KEYSTORE_PATH")
    val releaseStorePassword = System.getenv("RVH_RELEASE_STORE_PASSWORD")
    val releaseKeyAlias = System.getenv("RVH_RELEASE_KEY_ALIAS")
    val releaseKeyPassword = System.getenv("RVH_RELEASE_KEY_PASSWORD")
    val releaseSigningConfigured = !releaseKeystorePath.isNullOrBlank() &&
        !releaseStorePassword.isNullOrBlank() && !releaseKeyAlias.isNullOrBlank() &&
        !releaseKeyPassword.isNullOrBlank()
    if (releaseSigningConfigured) {
        signingConfigs.create("releaseUpload") {
            storeFile = file(releaseKeystorePath!!)
            storePassword = releaseStorePassword
            keyAlias = releaseKeyAlias
            keyPassword = releaseKeyPassword
        }
    }
    if (!ciKeystorePath.isNullOrBlank() && !ciStorePassword.isNullOrBlank() &&
        !ciKeyAlias.isNullOrBlank() && !ciKeyPassword.isNullOrBlank()) {
        signingConfigs.create("stableDebug") {
            storeFile = file(ciKeystorePath)
            storePassword = ciStorePassword
            keyAlias = ciKeyAlias
            keyPassword = ciKeyPassword
        }
    }

    buildTypes {
        debug {
            if (!ciKeystorePath.isNullOrBlank() && !ciStorePassword.isNullOrBlank() &&
                !ciKeyAlias.isNullOrBlank() && !ciKeyPassword.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("stableDebug")
            }
        }
        release {
            isMinifyEnabled = false
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("releaseUpload")
            }
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
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
