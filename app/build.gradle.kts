plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    alias(libs.plugins.compose.compiler)
}

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
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")


    // 서버 통신용 Retrofit & JSON 파싱
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")


    implementation("com.naver.maps:map-sdk:3.23.3")
    // 🗺️ 네이버 지도 Compose SDK
    implementation("io.github.fornewid:naver-map-compose:1.5.7")

    // 🖼️ 웹 이미지 로딩 라이브러리 (장소 썸네일용 - 중복 제거됨)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // 🚀 수정된 부분: 그룹명 오타 수정 (composereorderable)
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")

    // Activity & ViewModel for Compose
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Tooling Preview용
    debugImplementation("androidx.compose.ui:ui-tooling")
}