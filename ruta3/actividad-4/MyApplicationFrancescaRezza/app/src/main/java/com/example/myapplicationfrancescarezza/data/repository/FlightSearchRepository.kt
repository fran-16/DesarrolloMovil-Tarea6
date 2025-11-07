package com.example.myapplicationfrancescarezza.data.repository

import com.example.myapplicationfrancescarezza.data.local.dao.AirportDao
import com.example.myapplicationfrancescarezza.data.local.dao.FavoriteDao
import com.example.myapplicationfrancescarezza.data.local.entity.FavoriteRouteEntity
import com.example.myapplicationfrancescarezza.domain.model.Airport
import com.example.myapplicationfrancescarezza.domain.model.FlightRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FlightSearchRepository(
    private val airportDao: AirportDao,
    private val favoriteDao: FavoriteDao
) {

    fun searchAirports(query: String): Flow<List<Airport>> {
        val sanitizedQuery = query.trim()
        if (sanitizedQuery.isEmpty()) {
            return flowOf(emptyList())
        }
        return airportDao.searchAirports(sanitizedQuery).map { airports ->
            airports.map { entity ->
                Airport(
                    iataCode = entity.iataCode,
                    name = entity.name,
                    passengers = entity.passengers
                )
            }
        }
    }

    suspend fun getAirport(code: String): Airport? {
        val entity = airportDao.getAirportByCode(code) ?: return null
        return Airport(
            iataCode = entity.iataCode,
            name = entity.name,
            passengers = entity.passengers
        )
    }

    suspend fun getRoutesFromDeparture(departureCode: String): List<FlightRoute> {
        val departure = airportDao.getAirportByCode(departureCode) ?: return emptyList()
        val favoriteDestinations = favoriteDao.getFavoriteDestinations(departureCode).toSet()
        return airportDao.getDestinationAirports(departureCode).map { destination ->
            FlightRoute(
                departureCode = departure.iataCode,
                departureName = departure.name,
                destinationCode = destination.iataCode,
                destinationName = destination.name,
                destinationPassengers = destination.passengers,
                isFavorite = favoriteDestinations.contains(destination.iataCode)
            )
        }
    }

    fun observeFavorites(): Flow<List<FlightRoute>> =
        favoriteDao.observeFavoriteRoutes().map { favorites ->
            favorites.map { tuple ->
                FlightRoute(
                    departureCode = tuple.departureCode,
                    departureName = tuple.departureName,
                    destinationCode = tuple.destinationCode,
                    destinationName = tuple.destinationName,
                    destinationPassengers = tuple.destinationPassengers,
                    isFavorite = true
                )
            }
        }

    suspend fun toggleFavorite(departureCode: String, destinationCode: String) {
        val isFavorite = favoriteDao.isFavorite(departureCode, destinationCode)
        if (isFavorite) {
            favoriteDao.deleteFavorite(departureCode, destinationCode)
        } else {
            favoriteDao.insertFavorite(
                FavoriteRouteEntity(
                    departureCode = departureCode,
                    destinationCode = destinationCode
                )
            )
        }
    }
}


