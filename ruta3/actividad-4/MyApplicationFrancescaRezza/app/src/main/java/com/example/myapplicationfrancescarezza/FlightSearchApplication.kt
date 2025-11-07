package com.example.myapplicationfrancescarezza

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplicationfrancescarezza.data.local.FlightSearchDatabase
import com.example.myapplicationfrancescarezza.data.preferences.SearchPreferencesRepository
import com.example.myapplicationfrancescarezza.data.repository.FlightSearchRepository

private val Context.flightSearchDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "flight_search_preferences"
)

class FlightSearchApplication : Application() {

    val database: FlightSearchDatabase by lazy {
        FlightSearchDatabase.getInstance(this)
    }

    val repository: FlightSearchRepository by lazy {
        FlightSearchRepository(
            airportDao = database.airportDao(),
            favoriteDao = database.favoriteDao()
        )
    }

    val searchPreferencesRepository: SearchPreferencesRepository by lazy {
        SearchPreferencesRepository(flightSearchDataStore)
    }
}


