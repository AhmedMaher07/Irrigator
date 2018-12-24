package com.maher.irrigator.model.l

data class LPlant(
    val dev: Int,
    val `init`: Int,
    val late: Int,
    val mid: Int,
    val name: String,
    val count: Int,
    val date: Int,
    val flow: Int
)