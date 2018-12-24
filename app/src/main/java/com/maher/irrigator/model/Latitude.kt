package com.maher.irrigator.model

import com.google.gson.annotations.SerializedName

data class Latitude(
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("entry_id")
    val entryId: Int,
    val field1: String
)