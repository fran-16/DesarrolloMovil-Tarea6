package com.example.myapplicationfrancescarezza.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {

    val lastQuery: Flow<String> =
        dataStore.data.map { preferences -> preferences[KEY_SEARCH_QUERY] ?: "" }

    suspend fun saveQuery(query: String) {
        dataStore.edit { preferences ->
            if (query.isBlank()) {
                preferences.remove(KEY_SEARCH_QUERY)
            } else {
                preferences[KEY_SEARCH_QUERY] = query
            }
        }
    }

    suspend fun clearQuery() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_SEARCH_QUERY)
        }
    }

    private companion object {
        val KEY_SEARCH_QUERY = stringPreferencesKey("search_query")
    }
}


