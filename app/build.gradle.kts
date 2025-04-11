import java.util.Properties

val envProps = Properties().apply {
    val envFile = rootProject.file(".env") // or file("app/.env") if it's inside app/
    if (envFile.exists()) {
        load(envFile.inputStream())
    }
}

plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.testapp"
    compileSdk = 33

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.testapp"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "CV_KEY", "\"${envProps["CV_KEY"]}\"")
        buildConfigField("String", "GPT_KEY", "\"${envProps["GPT_KEY"]}\"")

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.google.apis:google-api-services-vision:v1-rev357-1.22.0")
    implementation("com.google.api-client:google-api-client-android:1.23.0")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.android.volley:volley:1.2.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
