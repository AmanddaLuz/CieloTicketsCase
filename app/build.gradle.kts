import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

val appVersionName = rootProject.file("VERSION").readText().trim()
val semanticVersion = requireNotNull(
    Regex("""^(\d+)\.(\d+)\.(\d+)(?:[-+][0-9A-Za-z.-]+)?$""").matchEntire(appVersionName),
) {
    "VERSION must follow Semantic Versioning"
}
val majorVersion = semanticVersion.groupValues[1].toLong()
val minorVersion = semanticVersion.groupValues[2].toLong()
val patchVersion = semanticVersion.groupValues[3].toLong()
require(minorVersion < 100 && patchVersion < 100)
val calculatedVersionCode = majorVersion * 10_000 + minorVersion * 100 + patchVersion
require(calculatedVersionCode in 1L..Int.MAX_VALUE.toLong())

fun buildConfigString(value: String): String =
    "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "br.com.amandaluz.cielotickets"
    compileSdk = 36

    defaultConfig {
        applicationId = "br.com.amandaluz.cielotickets"
        minSdk = 24
        targetSdk = 29
        versionCode = calculatedVersionCode.toInt()
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "CIELO_CLIENT_ID",
            buildConfigString(localProperties.getProperty("CIELO_CLIENT_ID", "")),
        )
        buildConfigField(
            "String",
            "CIELO_ACCESS_TOKEN",
            buildConfigString(localProperties.getProperty("CIELO_ACCESS_TOKEN", "")),
        )
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".xml"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        htmlReport = true
        xmlReport = true
        warningsAsErrors = false
        disable += setOf("ExpiredTargetSdkVersion", "OldTargetApi")
    }

}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.material)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.zxing.core)
    ksp(libs.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.json)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.room.testing)

    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.room.testing)
}

detekt {
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
    ignoreFailures = false
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "17"
    reports {
        xml.required.set(true)
        html.required.set(true)
        sarif.required.set(true)
        txt.required.set(false)
    }
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.R",
                    "*.R$*",
                    "*.BuildConfig",
                    "*.Manifest*",
                    "*.*Activity",
                    "*.*Activity$*",
                    "*.*Fragment",
                    "*.*Fragment$*",
                    "*.*View",
                    "*.*View$*",
                    "*.*Adapter",
                    "*.*Adapter$*",
                    "*.*ViewHolder",
                    "*.*ViewHolder$*",
                    "*.*Config",
                    "*.*Config$*",
                    "*.databinding.*",
                    "*.data.local.dao.*",
                    "*.data.local.db.*",
                    "*.data.local.entity.*",
                    "*.data.local.repository.RoomPurchaseRepositoryImpl",
                    "*.payment.cielo.CieloCallbackUriParser",
                    "*.payment.cielo.CieloPaymentIntentLauncherImpl",
                    "*.payment.cielo.CieloPaymentRequestEncoderImpl",
                    "*.feature.receipt.QrCodeBitmapRenderer",
                    "*.di.*",
                    "*.navigation.*",
                    "*.ui.binding.*",
                    "*.ui.lifecycle.*",
                )
            }
        }
        verify {
            rule("MVVM and Clean Architecture line coverage") {
                minBound(75)
            }
        }
    }
}
