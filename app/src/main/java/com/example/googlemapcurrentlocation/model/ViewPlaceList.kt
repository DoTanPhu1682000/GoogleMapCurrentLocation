package com.example.googlemapcurrentlocation.model

import com.google.gson.annotations.SerializedName

data class ViewPlaceList(
    val html_attributions: List<Any>,
    @SerializedName("result")
    val viewPlace: ViewPlace,
    val status: String
)