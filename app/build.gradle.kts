import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val releaseSigningFile = rootProject.file("keystore.properties")
val releaseSigning = Properties().apply {
    if (releaseSigningFile.isFile) {
        releaseSigningFile.inputStream().use(::load)
    }
}
val releaseStorePath = releaseSigning.getProperty("storeFile").orEmpty()
val releaseStorePassword = releaseSigning.getProperty("storePassword").orEmpty()
val releaseKeyAlias = releaseSigning.getProperty("keyAlias").orEmpty()
val releaseKeyPassword = releaseSigning.getProperty("keyPassword").orEmpty()
val hasReleaseSigning = releaseStorePath.isNotBlank() &&
    releaseStorePassword.isNotBlank() &&
    releaseKeyAlias.isNotBlank() &&
    releaseKeyPassword.isNotBlank() &&
    rootProject.file(releaseStorePath).isFile

android {
    namespace = "dev.ubai.promptpaste"
    compileSdk = 37

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    defaultConfig {
        applicationId = "dev.ubai.promptpaste"
        minSdk = 26
        targetSdk = 37
        versionCode = 8
        versionName = "1.6.0"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(releaseStorePath)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        getByName("release") {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.06.01")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")

    testImplementation("junit:junit:4.13.2")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
