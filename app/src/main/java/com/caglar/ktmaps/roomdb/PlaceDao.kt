package com.caglar.ktmaps.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.caglar.ktmaps.model.Place
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface PlaceDao {

    @Query("SELECT * FROM Place")
    fun getAll() : Flowable<List<Place>>

    @Insert
    fun insert(place: Place) : Completable

    @Insert
    fun delete(place: Place) : Completable
}