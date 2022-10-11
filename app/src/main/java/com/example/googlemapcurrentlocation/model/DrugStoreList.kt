package com.example.googlemapcurrentlocation.model

import com.google.gson.annotations.SerializedName

data class DrugStoreList(
    val html_attributions: List<Any>,
    val next_page_token: String,
    @SerializedName("results")
    var drugStores: List<DrugStore>,
    val status: String
)