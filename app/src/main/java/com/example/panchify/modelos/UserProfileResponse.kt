package com.example.panchify.modelos

data class UserProfileResponse(
    val id: String,
    val display_name: String?,
    val images: List<Image>?
)
