package com.example.myapplicationfrancescarezza.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplicationfrancescarezza.data.local.entity.FavoriteRouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query(
        """
            SELECT 
                f.id AS id,
                f.departure_code AS departureCode,
                departure.name AS departureName,
                f.destination_code AS destinationCode,
                arrival.name AS destinationName,
                arrival.passengers AS destinationPassengers
            FROM favorite f
            INNER JOIN airport departure ON departure.iata_code = f.departure_code
            INNER JOIN airport arrival ON arrival.iata_code = f.destination_code
            ORDER BY departure.passengers DESC, arrival.passengers DESC
        """
    )
    fun observeFavoriteRoutes(): Flow<List<FavoriteRouteTuple>>

    @Query("SELECT destination_code FROM favorite WHERE departure_code = :departureCode")
    suspend fun getFavoriteDestinations(departureCode: String): List<String>

    @Query(
        """
            SELECT EXISTS(
                SELECT 1 
                FROM favorite 
                WHERE departure_code = :departureCode 
                  AND destination_code = :destinationCode
            )
        """
    )
    suspend fun isFavorite(departureCode: String, destinationCode: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(route: FavoriteRouteEntity): Long

    @Query(
        "DELETE FROM favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode"
    )
    suspend fun deleteFavorite(departureCode: String, destinationCode: String)
}

data class FavoriteRouteTuple(
    val id: Int,
    val departureCode: String,
    val departureName: String,
    val destinationCode: String,
    val destinationName: String,
    val destinationPassengers: Int
)


