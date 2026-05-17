plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

// Read pinned versions from gradle.properties
val composeVersion: String by project
val composeCompiler: String by project
val roomVersion: String by project
val kspVersion: String by project
val okhttpVersion: String by project
val coreKtxVersion: String by project
val activityComposeVersion: String by project
val lifecycleVersion: String by project
val material3Version: String by project
val materialVersion: String by project
val appcompatVersion: String by project
val datastoreVersion: String by project
val retrofitVersion: String by project

android {
    namespace = "com.example.hebrewassistant"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.hebrewassistant"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = composeCompiler
    }

    packaging {
        resources {
            excludes += setOf("META-INF/AL2.0", "META-INF/LGPL2.1")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:${coreKtxVersion}")
    implementation("androidx.activity:activity-compose:${activityComposeVersion}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${lifecycleVersion}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${lifecycleVersion}")
    implementation("androidx.compose.ui:ui:${composeVersion}")
    implementation("androidx.compose.ui:ui-tooling-preview:${composeVersion}")
    implementation("androidx.compose.material:material-icons-extended:${composeVersion}")
    implementation("androidx.compose.material3:material3:${material3Version}")
    implementation("com.google.android.material:material:${materialVersion}")
    implementation("androidx.appcompat:appcompat:${appcompatVersion}")
    implementation("androidx.datastore:datastore-preferences:${datastoreVersion}")
    implementation("androidx.room:room-runtime:${roomVersion}")
    implementation("androidx.room:room-ktx:${roomVersion}")
    ksp("androidx.room:room-compiler:${roomVersion}")
    implementation("com.squareup.retrofit2:retrofit:${retrofitVersion}")
    implementation("com.squareup.okhttp3:okhttp:${okhttpVersion}")

    debugImplementation("androidx.compose.ui:ui-tooling:${composeVersion}")
    debugImplementation("androidx.compose.ui:ui-test-manifest:${composeVersion}")
}
