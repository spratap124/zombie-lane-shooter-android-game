import com.android.build.gradle.AppExtension
import java.io.File
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.reader()?.use { load(it) }
}

// Debug / dev: read from local.properties (admob.*.id.test); optional fallbacks match Google's samples.
val admobTestApplicationId =
    localProperties.getProperty("admob.application.id.test")
        ?: "ca-app-pub-3940256099942544~3347511713"
val admobTestBannerId =
    localProperties.getProperty("admob.banner.id.test")
        ?: "ca-app-pub-3940256099942544/6300978111"
val admobTestInterstitialId =
    localProperties.getProperty("admob.interstitial.id.test")
        ?: "ca-app-pub-3940256099942544/1033173712"
val admobTestRewardedId =
    localProperties.getProperty("admob.rewarded.id.test")
        ?: "ca-app-pub-3940256099942544/5224354917"

/** True when this invocation is building/linting a release variant (not debug). */
val releaseWorkRequested: Boolean =
    gradle.startParameter.taskNames.any { name ->
        name.contains("release", ignoreCase = true) && !name.contains("debug", ignoreCase = true)
    }

android {
    namespace = "com.zombielane.shooter"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    sourceSets.getByName("main").assets.srcDir(rootProject.layout.projectDirectory.dir("assets"))

    defaultConfig {
        applicationId = "com.zombielane.shooter"
        minSdk = 24
        targetSdk = 35
        versionCode = 8
        versionName = "1.0.7"

        // Debug / local: admob.*.id.test in local.properties (see release for production).
        manifestPlaceholders["admobApplicationId"] = admobTestApplicationId
        buildConfigField("String", "ADMOB_BANNER_ID", "\"$admobTestBannerId\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$admobTestInterstitialId\"")
        buildConfigField("String", "ADMOB_REWARDED_ID", "\"$admobTestRewardedId\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            fun prodAdMobId(
                humanName: String,
                envVar: String,
                localProp: String,
                gradleProp: String,
                testDevFallback: String,
            ): String {
                val fromEnv = System.getenv(envVar)
                val fromLocal = localProperties.getProperty(localProp)
                val fromGradle = project.findProperty(gradleProp) as String?
                val prod = fromEnv ?: fromLocal ?: fromGradle
                return if (releaseWorkRequested) {
                    require(!prod.isNullOrBlank()) {
                        "Release build requires production AdMob $humanName. " +
                            "Set $envVar, add $localProp to local.properties, or pass -P$gradleProp=. " +
                            "Debug builds use admob.*.id.test from local.properties."
                    }
                    prod
                } else {
                    prod ?: testDevFallback
                }
            }

            val admobAppId = prodAdMobId(
                "application ID",
                "ADMOB_APPLICATION_ID",
                "admob.application.id",
                "admobApplicationId",
                admobTestApplicationId,
            )
            manifestPlaceholders["admobApplicationId"] = admobAppId

            val bannerId = prodAdMobId(
                "banner unit ID",
                "ADMOB_BANNER_ID",
                "admob.banner.id",
                "adMobBannerId",
                admobTestBannerId,
            )
            val interstitialId = prodAdMobId(
                "interstitial unit ID",
                "ADMOB_INTERSTITIAL_ID",
                "admob.interstitial.id",
                "adMobInterstitialId",
                admobTestInterstitialId,
            )
            val rewardedId = prodAdMobId(
                "rewarded unit ID",
                "ADMOB_REWARDED_ID",
                "admob.rewarded.id",
                "adMobRewardedId",
                admobTestRewardedId,
            )

            buildConfigField("String", "ADMOB_BANNER_ID", "\"$bannerId\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$interstitialId\"")
            buildConfigField("String", "ADMOB_REWARDED_ID", "\"$rewardedId\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.gms:play-services-ads:23.6.0")
}

afterEvaluate {
    val versionName =
        extensions.getByType<AppExtension>().defaultConfig.versionName
            ?: error("defaultConfig.versionName is required for bundle file naming")
    val safeVersion = versionName.replace(Regex("[^A-Za-z0-9._-]"), "_")

    val bundleRelease = tasks.findByName("bundleRelease") ?: return@afterEvaluate
    bundleRelease.doLast {
        val dir = layout.buildDirectory.dir("outputs/bundle/release").get().asFile
        val aabs = dir.listFiles { _, name -> name.endsWith(".aab") }?.toList() ?: emptyList()
        check(aabs.isNotEmpty()) {
            "no .aab found in $dir"
        }
        // AGP emits app-release.aab; we may also keep lane-shooter-*.aab from a prior run—pick the fresh artifact.
        val built = aabs.firstOrNull { it.name == "app-release.aab" }
            ?: aabs.filter { it.name.endsWith("-release.aab") }.maxByOrNull { it.lastModified() }
            ?: aabs.maxBy { it.lastModified() }
        val final = File(dir, "lane-shooter-$safeVersion.aab")
        if (built.canonicalFile == final.canonicalFile) return@doLast
        final.delete()
        built.copyTo(final, overwrite = true)
        built.delete()
    }
}
