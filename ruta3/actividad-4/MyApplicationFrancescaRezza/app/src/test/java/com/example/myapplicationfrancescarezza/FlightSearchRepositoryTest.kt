package com.example.myapplicationfrancescarezza

import com.example.myapplicationfrancescarezza.data.local.dao.AirportDao
import com.example.myapplicationfrancescarezza.data.local.dao.FavoriteDao
import com.example.myapplicationfrancescarezza.data.local.dao.FavoriteRouteTuple
import com.example.myapplicationfrancescarezza.data.local.entity.AirportEntity
import com.example.myapplicationfrancescarezza.data.local.entity.FavoriteRouteEntity
import com.example.myapplicationfrancescarezza.data.repository.FlightSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FlightSearchRepositoryTest {

    private val sampleAirports = listOf(
        AirportEntity(iataCode = "AAA", name = "Alpha Airport", passengers = 500),
        AirportEntity(iataCode = "BBB", name = "Bravo International", passengers = 1000),
        AirportEntity(iataCode = "CCC", name = "Charlie Hub", passengers = 200)
    )

    @Test
    fun `toggleFavorite updates routes and favorites`() = runTest {
        val airportDao = FakeAirportDao(sampleAirports)
        val favoriteDao = FakeFavoriteDao { sampleAirports }
        val repository = FlightSearchRepository(airportDao, favoriteDao)

        val initialRoutes = repository.getRoutesFromDeparture("AAA")
        val bravoRoute = initialRoutes.first { it.destinationCode == "BBB" }
        assertFalse(bravoRoute.isFavorite)

        repository.toggleFavorite("AAA", "BBB")

        val updatedRoutes = repository.getRoutesFromDeparture("AAA")
        val updatedBravoRoute = updatedRoutes.first { it.destinationCode == "BBB" }
        assertTrue(updatedBravoRoute.isFavorite)

        val favorites = repository.observeFavorites().first()
        assertEquals(1, favorites.size)
        assertEquals("AAA", favorites.first().departureCode)
        assertEquals("BBB", favorites.first().destinationCode)
    }
}

private class FakeAirportDao(
    private val airports: List<AirportEntity>
) : AirportDao {

    override fun searchAirports(query: String): Flow<List<AirportEntity>> {
        val filtered = airports.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.iataCode.contains(query, ignoreCase = true)
        }.sortedByDescending { it.passengers }
        return MutableStateFlow(filtered)
    }

    override suspend fun getAirportByCode(iataCode: String): AirportEntity? =
        airports.find { it.iataCode == iataCode }

    override suspend fun getDestinationAirports(departureCode: String): List<AirportEntity> =
        airports.filterNot { it.iataCode == departureCode }
            .sortedByDescending { it.passengers }
}

private class FakeFavoriteDao(
    private val airportsProvider: () -> List<AirportEntity>
) : FavoriteDao {

    private val favorites = mutableListOf<FavoriteRouteEntity>()
    private val favoritesFlow = MutableStateFlow<List<FavoriteRouteTuple>>(emptyList())
    private var nextId = 1

    override fun observeFavoriteRoutes(): Flow<List<FavoriteRouteTuple>> = favoritesFlow

    override suspend fun getFavoriteDestinations(departureCode: String): List<String> =
        favorites.filter { it.departureCode == departureCode }
            .map { it.destinationCode }

    override suspend fun isFavorite(departureCode: String, destinationCode: String): Boolean =
        favorites.any {
            it.departureCode == departureCode && it.destinationCode == destinationCode
        }

    override suspend fun insertFavorite(route: FavoriteRouteEntity): Long {
        if (isFavorite(route.departureCode, route.destinationCode)) {
            return -1
        }
        val entity = route.copy(id = nextId++)
        favorites.add(entity)
        emitFavorites()
        return entity.id.toLong()
    }

    override suspend fun deleteFavorite(departureCode: String, destinationCode: String) {
        val removed = favorites.removeIf {
            it.departureCode == departureCode && it.destinationCode == destinationCode
        }
        if (removed) {
            emitFavorites()
        }
    }

    private fun emitFavorites() {
        val airportMap = airportsProvider().associateBy { it.iataCode }
        favoritesFlow.value = favorites.map { favorite ->
            val departure = airportMap[favorite.departureCode]
            val destination = airportMap[favorite.destinationCode]
            FavoriteRouteTuple(
                id = favorite.id,
                departureCode = favorite.departureCode,
                departureName = departure?.name.orEmpty(),
                destinationCode = favorite.destinationCode,
                destinationName = destination?.name.orEmpty(),
                destinationPassengers = destination?.passengers ?: 0
            )
        }
    }
}


