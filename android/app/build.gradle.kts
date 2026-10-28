import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Firma de release fuera del repo: ~/.android-keys/listpro-keystore.properties
// (storeFile, storePassword, keyAlias, keyPassword). Sin él, el release sale sin firmar.
val keystoreProps = Properties().apply {
    val f = File(System.getProperty("user.home"), ".android-keys/listpro-keystore.properties")
    if (f.exists()) f.inputStream().use(::load)
}

android {
    namespace = "com.listpro.xyon.pro"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.listpro.xyon.pro"
        minSdk = 27
        targetSdk = 35
        // La 1.0 (WebView + CSV) era versionCode 2
        versionCode = 12
        versionName = "2.2"
        // Servidor de ListPro. Para probar contra uno local: -PapiBase=http://10.0.2.2:3000 (solo debug permite http)
        val base = (project.findProperty("apiBase") as String?) ?: "https://listpro.xyoncloud.win"
        buildConfigField("String", "API_BASE", "\"$base\"")
        manifestPlaceholders["cleartext"] = (project.findProperty("allowCleartext") as String?) ?: "false"
    }

    signingConfigs {
        if (keystoreProps.isNotEmpty()) create("release") {
            storeFile = File(keystoreProps.getProperty("storeFile"))
            storePassword = keystoreProps.getProperty("storePassword")
            keyAlias = keystoreProps.getProperty("keyAlias")
            keyPassword = keystoreProps.getProperty("keyPassword")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["cleartext"] = "true"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
        // Igual de rápida que la release (R8, sin depuración) pero firmada con la clave de debug
        // para instalarla a mano mientras no hay firma de Play. Convive con la 1.0 de la tienda.
        create("preview") {
            initWith(getByName("release"))
            applicationIdSuffix = ".preview"
            versionNameSuffix = "-preview"
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += "release"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.work)
    implementation(libs.okhttp)
    implementation(libs.serialization.json)
    implementation(libs.coil.compose)
}
