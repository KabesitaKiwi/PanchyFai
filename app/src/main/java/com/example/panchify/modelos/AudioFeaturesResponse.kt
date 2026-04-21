package com.example.panchify.modelos

data class AudioFeaturesResponse(
    val audio_features: List<AudioFeatures?>
)

data class AudioFeatures(
    val id: String? = null,
    val danceability: Double? = null,
    val energy: Double? = null,
    val acousticness: Double? = null,
    val instrumentalness: Double? = null,
    val liveness: Double? = null,
    val valence: Double? = null,
    val tempo: Double? = null,
    val speechiness: Double? = null
)
