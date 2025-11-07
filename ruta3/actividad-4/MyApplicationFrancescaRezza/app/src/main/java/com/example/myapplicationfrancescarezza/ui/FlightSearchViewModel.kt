package com.example.myapplicationfrancescarezza.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplicationfrancescarezza.data.preferences.SearchPreferencesRepository
import com.example.myapplicationfrancescarezza.data.repository.FlightSearchRepository
import com.example.myapplicationfrancescarezza.domain.model.Airport
import com.example.myapplicationfrancescarezza.domain.model.FlightRoute
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class FlightSearchUiState(
    val searchQuery: String = "",
    val selectedAirport: Airport? = null,
    val suggestions: List<Airport> = emptyList(),
    val routes: List<FlightRoute> = emptyList(),
    val favorites: List<FlightRoute> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class FlightSearchViewModel(
    private val repository: FlightSearchRepository,
    private val preferencesRepository: SearchPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlightSearchUiState())
    val uiState: StateFlow<FlightSearchUiState> = _uiState.asStateFlow()

    private var suggestionsJob: Job? = null

    init {
        observeFavorites()
        restoreLastQuery()
    }

    fun onQueryChange(query: String) {
        suggestionsJob?.cancel()

        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            selectedAirport = null,
            routes = emptyList(),
            errorMessage = null,
            isLoading = query.isNotBlank()
        )

        viewModelScope.launch {
            preferencesRepository.saveQuery(query)
        }

        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                suggestions = emptyList(),
                isLoading = false
            )
            return
        }

        suggestionsJob = viewModelScope.launch {
            repository.searchAirports(query).collectLatest { suggestions ->
                _uiState.value = _uiState.value.copy(
                    suggestions = suggestions,
                    isLoading = false
                )
            }
        }
    }

    fun onSuggestionSelected(airport: Airport) {
        suggestionsJob?.cancel()
        _uiState.value = _uiState.value.copy(
            searchQuery = airport.iataCode,
            selectedAirport = airport,
            suggestions = emptyList(),
            routes = emptyList(),
            isLoading = true,
            errorMessage = null
        )
        viewModelScope.launch {
            preferencesRepository.saveQuery(airport.iataCode)
            loadRoutesForAirport(airport.iataCode)
        }
    }

    fun onSearchSubmitted() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isBlank()) return

        viewModelScope.launch {
            val code = query.uppercase()
            val airport = repository.getAirport(code)
            if (airport != null) {
                onSuggestionSelected(airport)
                return@launch
            }

            val suggestions = repository.searchAirports(query).first()
            val firstSuggestion = suggestions.firstOrNull()
            if (firstSuggestion != null) {
                onSuggestionSelected(firstSuggestion)
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No se encontraron aeropuertos que coincidan.",
                    isLoading = false
                )
            }
        }
    }

    fun onToggleFavorite(route: FlightRoute) {
        viewModelScope.launch {
            repository.toggleFavorite(route.departureCode, route.destinationCode)
            val selectedCode = _uiState.value.selectedAirport?.iataCode
            if (selectedCode != null) {
                loadRoutesForAirport(selectedCode)
            }
        }
    }

    fun onClearSearch() {
        suggestionsJob?.cancel()
        _uiState.value = FlightSearchUiState(
            favorites = _uiState.value.favorites
        )
        viewModelScope.launch {
            preferencesRepository.clearQuery()
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.observeFavorites().collectLatest { favorites ->
                _uiState.value = _uiState.value.copy(favorites = favorites)
            }
        }
    }

    private fun restoreLastQuery() {
        viewModelScope.launch {
            val storedQuery = preferencesRepository.lastQuery.first()
            if (storedQuery.isBlank()) {
                _uiState.value = _uiState.value.copy(searchQuery = "")
                return@launch
            }
            _uiState.value = _uiState.value.copy(
                searchQuery = storedQuery,
                isLoading = true
            )

            val airport = repository.getAirport(storedQuery.uppercase())
            if (airport != null) {
                _uiState.value = _uiState.value.copy(selectedAirport = airport)
                loadRoutesForAirport(airport.iataCode)
            } else {
                suggestionsJob?.cancel()
                suggestionsJob = viewModelScope.launch {
                    repository.searchAirports(storedQuery).collectLatest { suggestions ->
                        _uiState.value = _uiState.value.copy(
                            suggestions = suggestions,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadRoutesForAirport(iataCode: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        val routes = repository.getRoutesFromDeparture(iataCode)
        _uiState.value = _uiState.value.copy(
            routes = routes,
            isLoading = false,
            errorMessage = if (routes.isEmpty()) "No hay rutas disponibles." else null
        )
    }

    companion object {
        fun provideFactory(
            repository: FlightSearchRepository,
            preferencesRepository: SearchPreferencesRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(FlightSearchViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return FlightSearchViewModel(repository, preferencesRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}


