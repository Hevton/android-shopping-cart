package com.shopping.domain

data class Product(
    val id: Int,
    val imageUrl: String,
    val name: String,
    val price: Price,
)
