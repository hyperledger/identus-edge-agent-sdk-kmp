plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "io.iohk.atala.prism.sampleapp"
    compileSdk = 33

    defaultConfig {
        applicationId = "io.iohk.atala.prism.sampleapp"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildToolsVersion = "33.0.0"
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.6.0")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("io.iohk.atala.prism.walletsdk:apollo:1.0.0-local") /*{
        exclude("com.github.stephenc.jcip:jcip-annotations:1.0-1")
    }*/

    implementation("io.iohk.atala.prism.walletsdk:mercury:1.0.0-local")
    implementation("io.iohk.atala.prism.walletsdk:castor:1.0.0-local")
    implementation("io.iohk.atala.prism.walletsdk:pluto:1.0.0-local")
    implementation("io.iohk.atala.prism.walletsdk:prism-agent:1.0.0-local")
    implementation("io.iohk.atala.prism.walletsdk:domain:1.0.0-local")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
