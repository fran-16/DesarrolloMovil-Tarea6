package com.example.myapplicationfrancescarezza.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.myapplicationfrancescarezza.data.local.entity.AirportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AirportDao {

    @Query(
        """
            SELECT *
            FROM airport
            WHERE name LIKE '%' || :query || '%'
               OR iata_code LIKE '%' || :query || '%'
            ORDER BY passengers DESC
            LIMIT 10
        """
    )
    fun searchAirports(query: String): Flow<List<AirportEntity>>

    @Query("SELECT * FROM airport WHERE iata_code = :iataCode LIMIT 1")
    suspend fun getAirportByCode(iataCode: String): AirportEntity?

    @Query(
        """
            SELECT *
            FROM airport
            WHERE iata_code != :departureCode
            ORDER BY passengers DESC
        """
    )
    suspend fun getDestinationAirports(departureCode: String): List<AirportEntity>
}


