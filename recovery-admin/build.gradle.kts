plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.henrylumis.rvhrecovery"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.henrylumis.rvhedu.recoveryadmin"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.1"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
    val signingStorePath = System.getenv("RVH_SIGNING_KEYSTORE_PATH")
    val signingStorePassword = System.getenv("RVH_SIGNING_STORE_PASSWORD")
    val signingKeyAlias = System.getenv("RVH_SIGNING_KEY_ALIAS")
    val signingKeyPassword = System.getenv("RVH_SIGNING_KEY_PASSWORD")
    val signingConfigured = !signingStorePath.isNullOrBlank() && !signingStorePassword.isNullOrBlank() &&
        !signingKeyAlias.isNullOrBlank() && !signingKeyPassword.isNullOrBlank()
    if (signingConfigured) {
        signingConfigs.create("rvhStable") {
            storeFile = file(signingStorePath!!)
            storePassword = signingStorePassword
            keyAlias = signingKeyAlias
            keyPassword = signingKeyPassword
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            if (signingConfigured) signingConfig = signingConfigs.getByName("rvhStable")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
}
