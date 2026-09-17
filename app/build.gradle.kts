import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(FileInputStream(file))
}

fun normalizedUrl(value: String?): String? = value
    ?.trim()
    ?.takeIf { it.isNotBlank() }
    ?.let { if (it.endsWith("/")) it else "$it/" }

val debugServerUrl = normalizedUrl(localProperties.getProperty("SERVER_URL"))
    ?: "http://34.50.8.4:8088/"
val releaseServerUrl = normalizedUrl(localProperties.getProperty("RELEASE_SERVER_URL"))
    ?: debugServerUrl

val socialLoginProperties = Properties().apply {
    val file = rootProject.file("social-login.properties")
    if (file.exists()) load(FileInputStream(file))
}
val kakaoNativeAppKey = socialLoginProperties.getProperty("KAKAO_NATIVE_APP_KEY").orEmpty()
val naverLoginClientId = socialLoginProperties.getProperty("NAVER_LOGIN_CLIENT_ID").orEmpty()
val naverLoginClientSecret = socialLoginProperties.getProperty("NAVER_LOGIN_CLIENT_SECRET").orEmpty()

android {
    namespace = "com.kmj.ansik"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kmj.ansik"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = kakaoNativeAppKey
        manifestPlaceholders["USES_CLEARTEXT_TRAFFIC"] = "true"

        buildConfigField("String", "SERVER_URL", "\"$debugServerUrl\"")
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoNativeAppKey\"")
        buildConfigField("String", "NAVER_LOGIN_CLIENT_ID", "\"$naverLoginClientId\"")
        buildConfigField("String", "NAVER_LOGIN_CLIENT_SECRET", "\"$naverLoginClientSecret\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "SERVER_URL", "\"$debugServerUrl\"")
            manifestPlaceholders["USES_CLEARTEXT_TRAFFIC"] = "true"
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "SERVER_URL", "\"$releaseServerUrl\"")
            manifestPlaceholders["USES_CLEARTEXT_TRAFFIC"] = "true"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.naver.maps:map-sdk:3.23.3")
    implementation("io.github.fornewid:naver-map-compose:1.5.7")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("sh.calvin.reorderable:reorderable:2.4.3")

    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("com.kakao.sdk:v2-user:2.25.0")
    implementation("com.navercorp.nid:oauth:5.12.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
