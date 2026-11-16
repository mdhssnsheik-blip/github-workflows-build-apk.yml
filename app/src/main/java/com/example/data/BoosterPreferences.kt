package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.boosterDataStore by preferencesDataStore(name = "ff_turbo_prefs")

enum class BoostProfile(
    val title: String,
    val subtitle: String,
    val targetNote: String
) {
    BALANCED(
        title = "Balanced",
        subtitle = "Safe RAM trim + thermal guard",
        targetNote = "Smooth 60 FPS stability"
    ),
    AGGRESSIVE_RAM(
        title = "RAM Sweep",
        subtitle = "Deep cache flush + GC pass",
        targetNote = "Frees runtime heap headroom"
    ),
    ULTRA_FPS(
        title = "Ultra Focus",
        subtitle = "Full 5s matrix + network probe",
        targetNote = "Priority launch pipeline"
    )
}

enum class FreeFireVariant(val packageName: String?, val label: String) {
    AUTO(null, "Auto Detect"),
    FREE_FIRE("com.dts.freefireth", "Free Fire"),
    FREE_FIRE_MAX("com.dts.freefiremax", "Free Fire MAX")
}

data class BoosterSettings(
    val profile: BoostProfile = BoostProfile.ULTRA_FPS,
    val preferredVariant: FreeFireVariant = FreeFireVariant.AUTO,
    val autoLaunchAfterBoost: Boolean = true,
    val trimCacheOnBoost: Boolean = true,
    val totalBoostCount: Int = 0,
    val lastBoostTimestampMs: Long = 0L
)

class BoosterPreferences(private val context: Context) {

    private object Keys {
        val PROFILE = stringPreferencesKey("boost_profile")
        val VARIANT = stringPreferencesKey("ff_variant")
        val AUTO_LAUNCH = booleanPreferencesKey("auto_launch")
        val TRIM_CACHE = booleanPreferencesKey("trim_cache")
        val BOOST_COUNT = intPreferencesKey("boost_count")
        val LAST_BOOST = longPreferencesKey("last_boost_ts")
    }

    val settingsFlow: Flow<BoosterSettings> = context.boosterDataStore.data.map { prefs ->
        val profile = runCatching {
            BoostProfile.valueOf(prefs[Keys.PROFILE] ?: BoostProfile.ULTRA_FPS.name)
        }.getOrDefault(BoostProfile.ULTRA_FPS)

        val variant = runCatching {
            FreeFireVariant.valueOf(prefs[Keys.VARIANT] ?: FreeFireVariant.AUTO.name)
        }.getOrDefault(FreeFireVariant.AUTO)

        BoosterSettings(
            profile = profile,
            preferredVariant = variant,
            autoLaunchAfterBoost = prefs[Keys.AUTO_LAUNCH] ?: true,
            trimCacheOnBoost = prefs[Keys.TRIM_CACHE] ?: true,
            totalBoostCount = prefs[Keys.BOOST_COUNT] ?: 0,
            lastBoostTimestampMs = prefs[Keys.LAST_BOOST] ?: 0L
        )
    }

    suspend fun setProfile(profile: BoostProfile) {
        context.boosterDataStore.edit { it[Keys.PROFILE] = profile.name }
    }

    suspend fun setPreferredVariant(variant: FreeFireVariant) {
        context.boosterDataStore.edit { it[Keys.VARIANT] = variant.name }
    }

    suspend fun setAutoLaunch(enabled: Boolean) {
        context.boosterDataStore.edit { it[Keys.AUTO_LAUNCH] = enabled }
    }

    suspend fun setTrimCache(enabled: Boolean) {
        context.boosterDataStore.edit { it[Keys.TRIM_CACHE] = enabled }
    }

    suspend fun recordBoostRun() {
        context.boosterDataStore.edit { prefs ->
            prefs[Keys.BOOST_COUNT] = (prefs[Keys.BOOST_COUNT] ?: 0) + 1
            prefs[Keys.LAST_BOOST] = System.currentTimeMillis()
        }
    }
}
