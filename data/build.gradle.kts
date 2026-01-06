import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.sql.delight)
}

// Load local.properties
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

android {
    namespace = "com.ptit.data"

    val MIN_SDK: String by project
    val COMPILE_SDK: String by project
    compileSdk = COMPILE_SDK.toInt()

    defaultConfig {
        minSdk = MIN_SDK.toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // Add Gemini API key from local.properties
        buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\"")
    }

    buildTypes {
        debug {
//            buildConfigField("String", "BASE_URL", "\"https://shoppie-api-61607f7565c2.herokuapp.com\"")
            buildConfigField("String", "BASE_URL", "\"https://76ef72e475eb.ngrok-free.app\"")
        }

        release {
            buildConfigField("String", "BASE_URL", "\"https://shoppie-api-61607f7565c2.herokuapp.com\"")

            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    sqldelight {
        databases {
            create("PtitEcomDatabase") {
                packageName.set("com.ptit.database")
            }
        }
    }

    androidComponents {
        onVariants(selector().all()) { variant ->
            afterEvaluate {
                val capName = variant.name.capitalize()
                tasks.getByName<KotlinCompile>("ksp${capName}Kotlin") {
                    setSource(tasks.getByName("generate${capName}PtitEcomDatabaseInterface").outputs)
                }
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.hilt)
    implementation(libs.suggestions)
    ksp(libs.hilt.compiler)

    implementation(libs.mmkv)

    implementation(libs.recurly)
    
    implementation(libs.gemini.ai)

    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.logging.interceptor)

    implementation(libs.sql.delight)
    implementation(libs.sql.delight.coroutines)

    implementation(libs.paging)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(project(":common"))
    implementation(project(":domain"))
}

tasks.withType<KotlinCompile>().configureEach {
    if (project.findProperty("enableReport") == "true") {
        compilerOptions.freeCompilerArgs.addAll(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${layout.buildDirectory.asFile.get().absolutePath}/compose_compiler",
        )
    }
}