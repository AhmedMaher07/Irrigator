package com.maher.irrigator.model

import com.google.gson.annotations.SerializedName

data class Longitude(
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("entry_id")
    val entryId: Int,
    val field2: String
)