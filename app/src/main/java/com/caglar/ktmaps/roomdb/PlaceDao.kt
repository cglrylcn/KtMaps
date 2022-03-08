package com.caglar.ktmaps.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.caglar.ktmaps.model.Place

@Dao
interface PlaceDao {

    @Query("SELECT * FROM Place")
    fun getAll() : List<Place>

    @Insert
    fun insert(place: Place)

    @Insert
    fun delete(place: Place)
}