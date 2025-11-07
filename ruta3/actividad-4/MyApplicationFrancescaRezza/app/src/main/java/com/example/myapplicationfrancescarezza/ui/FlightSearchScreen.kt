package com.example.myapplicationfrancescarezza.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplicationfrancescarezza.FlightSearchApplication
import com.example.myapplicationfrancescarezza.domain.model.Airport
import com.example.myapplicationfrancescarezza.domain.model.FlightRoute

@Composable
fun FlightSearchApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val application = context.applicationContext as FlightSearchApplication
    val factory = remember {
        FlightSearchViewModel.provideFactory(
            repository = application.repository,
            preferencesRepository = application.searchPreferencesRepository
        )
    }
    val viewModel: FlightSearchViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FlightSearchScreen(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::onSearchSubmitted,
        onClearQuery = viewModel::onClearSearch,
        onSuggestionSelected = viewModel::onSuggestionSelected,
        onToggleFavorite = viewModel::onToggleFavorite,
        modifier = modifier
    )
}

private enum class FlightSearchContentState {
    Suggestions,
    Routes,
    Favorites
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchScreen(
    uiState: FlightSearchUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearQuery: () -> Unit,
    onSuggestionSelected: (Airport) -> Unit,
    onToggleFavorite: (FlightRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val contentState = remember(uiState.searchQuery, uiState.selectedAirport, uiState.routes) {
        when {
            uiState.selectedAirport != null -> FlightSearchContentState.Routes
            uiState.searchQuery.isNotBlank() -> FlightSearchContentState.Suggestions
            else -> FlightSearchContentState.Favorites
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Flight Search") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            SearchTextField(
                value = uiState.searchQuery,
                onValueChange = onQueryChange,
                onClear = onClearQuery,
                onSearch = onSearch,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Crossfade(
                targetState = contentState,
                label = "flightSearchContent"
            ) { state ->
                when (state) {
                    FlightSearchContentState.Suggestions -> SuggestionList(
                        suggestions = uiState.suggestions,
                        onSuggestionSelected = onSuggestionSelected
                    )

                    FlightSearchContentState.Routes -> FlightsFromAirport(
                        airport = uiState.selectedAirport,
                        routes = uiState.routes,
                        onToggleFavorite = onToggleFavorite
                    )

                    FlightSearchContentState.Favorites -> FavoriteRoutes(
                        favorites = uiState.favorites,
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(text = "Enter departure airport") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.FlightTakeoff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (value.isNotBlank()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Borrar búsqueda"
                        )
                    }
                }
                IconButton(onClick = onSearch) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Buscar vuelos"
                    )
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun SuggestionList(
    suggestions: List<Airport>,
    onSuggestionSelected: (Airport) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(suggestions) { airport ->
            SuggestionItem(
                airport = airport,
                onClick = { onSuggestionSelected(airport) }
            )
        }
    }
}

@Composable
private fun SuggestionItem(
    airport: Airport,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = airport.iataCode,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(0.3f)
            )
            Text(
                text = airport.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FlightsFromAirport(
    airport: Airport?,
    routes: List<FlightRoute>,
    onToggleFavorite: (FlightRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = if (airport != null) "Flights from ${airport.iataCode}" else "Flights",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        if (routes.isEmpty()) {
            EmptyState(message = "No hay rutas disponibles desde este aeropuerto.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(routes) { route ->
                    FlightRouteCard(
                        route = route,
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteRoutes(
    favorites: List<FlightRoute>,
    onToggleFavorite: (FlightRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "Favorite routes",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        if (favorites.isEmpty()) {
            EmptyState(message = "Aún no se han guardado rutas favoritas.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favorites) { route ->
                    FlightRouteCard(
                        route = route,
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }
        }
    }
}

@Composable
private fun FlightRouteCard(
    route: FlightRoute,
    onToggleFavorite: (FlightRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                RouteInfo(
                    label = "DEPART",
                    code = route.departureCode,
                    name = route.departureName
                )
                Spacer(modifier = Modifier.height(12.dp))
                RouteInfo(
                    label = "ARRIVE",
                    code = route.destinationCode,
                    name = route.destinationName
                )
            }
            IconButton(onClick = { onToggleFavorite(route) }) {
                val icon = if (route.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder
                val tint = if (route.isFavorite) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                Icon(
                    imageVector = icon,
                    contentDescription = if (route.isFavorite) {
                        "Quitar de favoritos"
                    } else {
                        "Guardar como favorito"
                    },
                    tint = tint
                )
            }
        }
    }
}

@Composable
private fun RouteInfo(
    label: String,
    code: String,
    name: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = code,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


