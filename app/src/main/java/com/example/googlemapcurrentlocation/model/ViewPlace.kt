package com.example.googlemapcurrentlocation.model

data class ViewPlace(
    val address_components: List<AddressComponent>,
    val adr_address: String,
    val business_status: String,
    val formatted_address: String,
    val formatted_phone_number: String,
    val geometry: GeometryX,
    val icon: String,
    val icon_background_color: String,
    val icon_mask_base_uri: String,
    val international_phone_number: String,
    val name: String,
    val photos: List<PhotoX>,
    val place_id: String,
    val plus_code: PlusCodeX,
    val price_level: Int,
    val rating: Double,
    val reference: String,
    val reviews: List<Review>,
    val types: List<String>,
    val url: String,
    val user_ratings_total: Int,
    val utc_offset: Int,
    val vicinity: String,
    val website: String
)