package com.example.hebrewassistant.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hebrewassistant.llm.LlmProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class LlmSettings(
    val provider: LlmProvider = LlmProvider.GEMINI,
    val apiKey: String = ""
)

class SettingsRepository(context: Context) {
    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val PROVIDER = stringPreferencesKey("llm_provider")
        val API_KEY = stringPreferencesKey("llm_api_key")
    }

    val settingsFlow: Flow<LlmSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val providerName = preferences[PreferencesKeys.PROVIDER] ?: LlmProvider.GEMINI.name
            val apiKey = preferences[PreferencesKeys.API_KEY] ?: ""
            val provider = LlmProvider.values().find { it.name == providerName } ?: LlmProvider.GEMINI
            LlmSettings(provider = provider, apiKey = apiKey)
        }

    suspend fun updateProvider(provider: LlmProvider) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PROVIDER] = provider.name
        }
    }

    suspend fun updateApiKey(apiKey: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.API_KEY] = apiKey.trim()
        }
    }

    suspend fun updateSettings(provider: LlmProvider, apiKey: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PROVIDER] = provider.name
            preferences[PreferencesKeys.API_KEY] = apiKey.trim()
        }
    }
}
