import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Read local.properties for dev-only overrides (file is gitignored)
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    namespace = "com.example.dentalinkmobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.dentalinkmobile"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BASE_URL default: emulator loopback. Override in local.properties via:
        //   BASE_URL=http://192.168.x.x:8080/api/v1/
        // After Render deployment, set to the live Render URL here in release block.
        val baseUrl = localProps.getProperty("BASE_URL") ?: "http://10.0.2.2:8080/api/v1/"
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")

        // Default: cleartext NOT allowed. Debug build type overrides below.
        manifestPlaceholders["usesCleartextTraffic"] = false
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            // Allow HTTP on local LAN during development testing on physical device
            manifestPlaceholders["usesCleartextTraffic"] = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Release build uses HTTPS to Render — cleartext remains false from defaultConfig
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.okhttp.logging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}