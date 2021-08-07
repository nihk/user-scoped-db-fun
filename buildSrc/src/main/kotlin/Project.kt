import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.android.build.gradle.internal.dsl.BuildType
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

fun Project.androidAppConfig(extras: (BaseAppModuleExtension.() -> Unit) = {}) = androidConfig<BaseAppModuleExtension>().run {
    defaultConfig {
        buildToolsVersion(BuildVersion.buildTools)
        multiDexEnabled = true
    }

    buildTypes {
        listOf(getByName(BuildTypes.DebugMinified), getByName(BuildTypes.Release)).forEach { buildType ->
            buildType.isShrinkResources = true
        }
    }

    buildFeatures {
        buildConfig = true
    }

    extras()
}

fun Project.androidLibraryConfig(extras: (LibraryExtension.() -> Unit) = {}) = androidConfig<LibraryExtension>().run {
    buildFeatures {
        buildConfig = false
    }

    extras()
}

private fun <T : BaseExtension> Project.androidConfig() = android<T>().apply {
    compileSdkVersion(BuildVersion.compileSdk)

    defaultConfig {
        minSdkVersion(BuildVersion.minSdk)
        targetSdkVersion(BuildVersion.targetSdk)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName(BuildTypes.Debug) {
            isMinifyEnabled = false
        }
        create(BuildTypes.DebugMinified) {
            signingConfig  = signingConfigs.getByName(BuildTypes.Debug)
            minify(defaultProguardFile())
        }
        getByName(BuildTypes.Release) {
            minify(defaultProguardFile())
        }
    }

    buildFeatures.apply {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            freeCompilerArgs = listOf(
                "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi"
            )
        }
    }

    sourceSets {
        getByName("main").java.srcDir("src/main/kotlin")
        getByName("test").java.srcDir("src/test/kotlin")
        getByName("androidTest").java.srcDir("src/androidTest/kotlin")
    }

    testOptions {
        animationsDisabled = true
    }

    packagingOptions {
        setExcludes(
            setOf(
                "LICENSE.txt",
                "NOTICE.txt",
                "META-INF/**",
            )
        )
    }
}.also {
    defaultDependencies()
}

private fun <T : BaseExtension> Project.android(): T {
    @Suppress("UNCHECKED_CAST")
    return extensions.findByName("android") as T
}

fun Project.jvmConfig() {
    val sourceSets = extensions.getByName("sourceSets") as SourceSetContainer
    sourceSets["main"].java.srcDir("src/main/kotlin")
    sourceSets["test"].java.srcDir("src/test/kotlin")

    defaultDependencies()
}

private fun Project.defaultDependencies() {
    dependencies {
        "implementation"(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
        "implementation"(Dependencies.Kotlin.stdlib)
    }
}

private fun BaseExtension.defaultProguardFile(): File {
    return getDefaultProguardFile("proguard-android.txt")
}

private fun BuildType.minify(defaultProguardFile: File) {
    isMinifyEnabled = true
    proguardFiles(defaultProguardFile, "proguard-rules.pro")
    consumerProguardFiles("consumer-rules.pro")
}
