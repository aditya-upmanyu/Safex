// data/appsentry/FakeAppRegistryDataSource.kt
package com.safex.app.data.appsentry

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.safex.app.domain.appsentry.repository.AppRegistryProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

data class RegistryModel(
    val legitimate_packages: Map<String, String> = emptyMap(),
    val known_rat_signatures: List<String> = emptyList(),
    val suspicious_package_patterns: List<String> = emptyList()
)

@Singleton
class FakeAppRegistryDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : AppRegistryProvider {

    private var cachedRegistry: RegistryModel? = null
    private var compiledPatterns: List<Pattern>? = null

    @Synchronized
    fun getRegistry(): RegistryModel {
        cachedRegistry?.let { return it }
        val loaded = try {
            context.assets.open("appsentry/fake_app_registry.json").use { stream ->
                InputStreamReader(stream).use { reader ->
                    val type = object : TypeToken<RegistryModel>() {}.type
                    gson.fromJson<RegistryModel>(reader, type) ?: RegistryModel()
                }
            }
        } catch (e: Exception) {
            RegistryModel()
        }
        cachedRegistry = loaded
        compiledPatterns = loaded.suspicious_package_patterns.mapNotNull {
            try { Pattern.compile(it, Pattern.CASE_INSENSITIVE) } catch (_: Exception) { null }
        }
        return loaded
    }

    override fun isKnownRat(packageName: String): Boolean {
        return getRegistry().known_rat_signatures.any { it.equals(packageName, ignoreCase = true) }
    }

    override fun matchesSuspiciousPattern(packageName: String): Boolean {
        getRegistry()
        return compiledPatterns?.any { it.matcher(packageName).matches() } ?: false
    }

    override fun checkMasquerading(packageName: String, appLabel: String): AppRegistryProvider.MasqueradeType {
        val registry = getRegistry()
        val normalizedLabel = appLabel.lowercase().replace(" ", "")

        for ((legitPkg, legitName) in registry.legitimate_packages) {
            val normalizedLegit = legitName.lowercase().replace(" ", "")
            if (packageName.equals(legitPkg, ignoreCase = true)) {
                return AppRegistryProvider.MasqueradeType.NONE // Genuine package
            }

            if (normalizedLabel.contains(normalizedLegit) || normalizedLegit.contains(normalizedLabel)) {
                if (legitName.contains("Aadhaar", ignoreCase = true) ||
                    legitName.contains("Parivahan", ignoreCase = true) ||
                    legitName.contains("DigiLocker", ignoreCase = true)) {
                    return AppRegistryProvider.MasqueradeType.GOVT
                }
                return AppRegistryProvider.MasqueradeType.BANKING
            }
        }
        return AppRegistryProvider.MasqueradeType.NONE
    }
}
