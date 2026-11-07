plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.henrylumis.rvhgrader"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.henrylumis.rvhedu"
        minSdk = 24
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = 160
        versionName = "1.7.4"
    }

    // All installable variants use one persistent signing identity. A per-runner
    // debug key makes Android reject later APKs as updates (INSTALL_FAILED_UPDATE_INCOMPATIBLE).
    val signingStorePath = System.getenv("RVH_SIGNING_KEYSTORE_PATH")
    val signingStorePassword = System.getenv("RVH_SIGNING_STORE_PASSWORD")
    val signingKeyAlias = System.getenv("RVH_SIGNING_KEY_ALIAS")
    val signingKeyPassword = System.getenv("RVH_SIGNING_KEY_PASSWORD")
    val signingConfigured = !signingStorePath.isNullOrBlank() &&
        !signingStorePassword.isNullOrBlank() && !signingKeyAlias.isNullOrBlank() &&
        !signingKeyPassword.isNullOrBlank()

    if (signingConfigured) {
        signingConfigs.create("rvhStable") {
            storeFile = file(signingStorePath!!)
            storePassword = signingStorePassword
            keyAlias = signingKeyAlias
            keyPassword = signingKeyPassword
        }
    }

    buildTypes {
        debug {
            if (signingConfigured) signingConfig = signingConfigs.getByName("rvhStable")
        }
        release {
            isMinifyEnabled = false
            // CI refuses to publish an APK unless the persistent signing key is configured.
            if (signingConfigured) signingConfig = signingConfigs.getByName("rvhStable")
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
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
