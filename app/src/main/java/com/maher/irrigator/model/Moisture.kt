package com.maher.irrigator.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class Moisture(
    @SerializedName("created_at")
    val createdAt: Date,
    @SerializedName("entry_id")
    val entryId: Int,
    val field3: String
)