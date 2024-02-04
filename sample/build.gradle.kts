plugins {
    id("pt.jcosta.resourceplaceholders")
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "de.timfreiheit.plugin.test"
    compileSdk = 34

    defaultConfig {
        applicationId = "de.timfreiheit.plugin.test"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        manifestPlaceholders["testPlaceholder"] = "test"
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
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        debug {
            manifestPlaceholders["debug_placeholder"] = "debug value"
        }
    }

    flavorDimensions += listOf("env")
    productFlavors {
        create("staging") {
            dimension = "env"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"

            manifestPlaceholders["deeplink_confirmed_schema"] = "test-staging"
        }

        create("production") {
            dimension = "env"
            applicationIdSuffix = ".production"
            versionNameSuffix = "-production"

            manifestPlaceholders["deeplink_confirmed_schema"] = "test-production"
        }
    }

}

dependencies {
    implementation("android.arch.navigation:navigation-fragment:1.0.0")
    implementation("android.arch.navigation:navigation-ui:1.0.0")
}

resourcePlaceholders {
    files.set(
        listOf(
            "xml/shortcuts.xml",
            "navigation/graph.xml"
        )
    )
}

