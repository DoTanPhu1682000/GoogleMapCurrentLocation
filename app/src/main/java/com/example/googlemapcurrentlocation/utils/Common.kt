package com.example.googlemapcurrentlocation.utils

import com.example.googlemapcurrentlocation.model.DrugStore
import com.example.googlemapcurrentlocation.network.DrugStoreService
import com.example.googlemapcurrentlocation.network.RetrofitClient


object Common {
    private val GOOGLE_API_URL = "https://maps.googleapis.com/"

    var currentResult: DrugStore? = null

    val googleApiService: DrugStoreService
        get() = RetrofitClient.getClient(GOOGLE_API_URL).create(DrugStoreService::class.java)
}