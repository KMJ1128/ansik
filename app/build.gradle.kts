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

    // 💡 수정된 부분: Compose BOM 버전을 올려서 터치(clickable) 충돌 해결
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))

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

    // 🖼️ 웹 이미지 로딩 라이브러리
    implementation("io.coil-kt:coil-compose:2.6.0")

    implementation("sh.calvin.reorderable:reorderable:2.4.3")

    // Activity & ViewModel for Compose
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Tooling Preview용
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.appcompat:appcompat:1.7.1")

    // 확장 아이콘 (수정된 BOM에 의해 자동으로 알맞은 버전이 적용됨)
    implementation("androidx.compose.material:material-icons-extended")
}