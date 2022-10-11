package com.example.googlemapcurrentlocation.model

import com.google.gson.annotations.SerializedName

data class Photo(
    val height: Int,
    val html_attributions: List<String>,
    @SerializedName("photo_reference")
    val photo_reference: String,
    val width: Int
)