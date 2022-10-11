package com.example.googlemapcurrentlocation.network

import com.example.googlemapcurrentlocation.model.DrugStoreList
import com.example.googlemapcurrentlocation.model.ViewPlaceList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface DrugStoreService {
    @GET
    fun getNearByPlace(@Url url: String): Call<DrugStoreList>

    @GET
    fun getDetailPlace(@Url url: String): Call<ViewPlaceList>
}