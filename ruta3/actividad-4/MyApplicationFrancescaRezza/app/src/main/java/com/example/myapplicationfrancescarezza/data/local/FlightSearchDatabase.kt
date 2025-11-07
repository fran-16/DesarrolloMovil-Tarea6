package com.example.myapplicationfrancescarezza.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplicationfrancescarezza.data.local.dao.AirportDao
import com.example.myapplicationfrancescarezza.data.local.dao.FavoriteDao
import com.example.myapplicationfrancescarezza.data.local.entity.AirportEntity
import com.example.myapplicationfrancescarezza.data.local.entity.FavoriteRouteEntity

@Database(
    entities = [AirportEntity::class, FavoriteRouteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FlightSearchDatabase : RoomDatabase() {

    abstract fun airportDao(): AirportDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        private const val DATABASE_NAME = "flight_search.db"

        @Volatile
        private var instance: FlightSearchDatabase? = null

        fun getInstance(context: Context): FlightSearchDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FlightSearchDatabase::class.java,
                    DATABASE_NAME
                )
                    .createFromAsset("database/$DATABASE_NAME")
                    .build()
                    .also { instance = it }
            }
    }
}


