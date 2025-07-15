import com.vanniktech.maven.publish.AndroidSingleVariantLibrary

plugins {
    alias(libs.plugins.android.library)
    id("com.vanniktech.maven.publish") version "0.34.0"
}

ext {
    version = "0.1.0"
}

android {
    namespace = "com.moneyoyo.ads.applovinadapter"
    compileSdk = 35

    defaultConfig {
        aarMetadata {
            minCompileSdk = 24
        }
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
            buildConfigField("String", "VERSION_NAME", "\"${version}\"")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("com.moneyoyo", "applovin-adapter", version.toString())

    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            sourcesJar = true,
            publishJavadocJar = false,
        )
    )

    pom {
        name.set("Moneyoyo AppLovin Adapter")
        description.set("AppLovin Adapter for Moneyoyo")
        inceptionYear.set("2025")
        url.set("https://moneyoyo.com")

        developers {
            developer {
                name.set("Moneyoyo")
                url.set("https://moneyoyo.com")
            }
        }

        licenses {
            license {
                name.set("Moneyoyo SDK EULA")
                url.set("https://github.com/Moneyoyo/ad-sdk?tab=License-1-ov-file")
            }
        }

        scm {
            connection.set("scm:git:git:github.com/Moneyoyo/android-applovin-mediation-adapter.git")
            developerConnection.set("scm:git:ssh://github.com/Moneyoyo/android-applovin-mediation-adapter.git")
            url.set("https://github.com/Moneyoyo/android-applovin-mediation-adapter")
        }
    }
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    api("com.applovin:applovin-sdk:[13,14)")
    implementation("com.moneyoyo:ads-sdk:0.2.0")
}