package com.maher.irrigator.model.l

data class LPlant(
    val dev: Double,
    val `init`: Double,
    val late: Double,
    val mid: Double,
    val name: String,
    val count: Int,
    val date: Int,
    val flow: Double
)