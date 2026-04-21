package com.example.panchify.modelos

import kotlin.math.abs
import kotlin.math.roundToInt

data class VibeCard(
    val id: String,
    val nombre: String,
    val porcentaje: Int,
    val icono: String,
    val color: String,
    val descripcion: String,
    val ejemploCancion: String,
    val ejemploArtista: String,
    val imagenUrl: String?
)

object VibeCalculator {

    fun calcularVibes(
        tracks: List<Track>,
        features: List<AudioFeatures>,
        etiquetaRango: String
    ): List<VibeCard> {
        val validFeatures = features.filter { it.id != null }
        if (validFeatures.isEmpty()) return emptyList()

        val featuresById = validFeatures.mapNotNull { feature ->
            feature.id?.let { it to feature }
        }.toMap()

        val stats = calcularStatsPonderadas(tracks, validFeatures)
        val vibes = listOf(
            crear("bailables", "Bailables", stats.danceability, "BA", "#E3F2FD", "$etiquetaRango con %d%% de ritmo bailable", tracks, featuresById, { f -> f.danceability.orZero() >= 0.60 }) { f -> f.danceability.orZero() },
            crear("energeticas", "Energeticas", stats.energy, "EN", "#E8F5E9", "$etiquetaRango con %d%% de energia", tracks, featuresById, { f -> f.energy.orZero() >= 0.65 }) { f -> f.energy.orZero() },
            crear("sad", "Sad / melancolicas", stats.sad, "SA", "#EDE7F6", "$etiquetaRango con %d%% de vibra melancolica", tracks, featuresById, { f -> f.valence.orZero() <= 0.40 && f.energy.orZero() <= 0.55 && f.speechiness.orZero() <= 0.35 }) { f -> combine((1.0 - f.valence.orZero()) to 0.80, (1.0 - f.energy.orZero()) to 0.20) },
            crear("positivas", "Positivas", stats.valence, "PO", "#FFF9C4", "$etiquetaRango con %d%% de positividad", tracks, featuresById, { f -> f.valence.orZero() >= 0.55 }) { f -> f.valence.orZero() },
            crear("acusticas", "Acusticas", stats.acousticness, "AC", "#FFF3E0", "$etiquetaRango con %d%% de sonido acustico", tracks, featuresById, { f -> f.acousticness.orZero() >= 0.45 }) { f -> f.acousticness.orZero() },
            crear("instrumentales", "Instrumentales", stats.instrumentalness, "IN", "#F3E5F5", "$etiquetaRango con %d%% de toque instrumental", tracks, featuresById, { f -> f.instrumentalness.orZero() >= 0.35 }) { f -> f.instrumentalness.orZero() },
            crear("en_vivo", "En vivo", stats.liveness, "LV", "#FFEBEE", "$etiquetaRango con %d%% de sensacion en vivo", tracks, featuresById, { f -> f.liveness.orZero() >= 0.45 }) { f -> f.liveness.orZero() },
            crear("habladas", "Habladas / rap", stats.spoken, "SP", "#E0F7FA", "$etiquetaRango con %d%% de voz hablada o rap", tracks, featuresById, { f -> f.speechiness.orZero() >= 0.33 }) { f -> (f.speechiness.orZero() / 0.66).coerceIn(0.0, 1.0) },
            crear("intensas", "Intensas", stats.intense, "IT", "#FFE0B2", "$etiquetaRango con %d%% de intensidad", tracks, featuresById, { f -> f.energy.orZero() >= 0.70 && f.tempo.orZero() >= 105.0 }) { f -> combine(f.energy.orZero() to 0.6, normalizarTempo(f.tempo.orZero()) to 0.4) },
            crear("relajadas", "Relajadas", stats.relaxed, "RE", "#E0F2F1", "$etiquetaRango con %d%% de calma relajada", tracks, featuresById, { f -> f.energy.orZero() <= 0.55 && f.speechiness.orZero() <= 0.35 }) { f -> combine((1.0 - f.energy.orZero()) to 0.5, f.acousticness.orZero() to 0.3, (1.0 - normalizarTempo(f.tempo.orZero())) to 0.2) },
            crear("rapidas", "Rapidas", stats.fast, "RA", "#FBE9E7", "$etiquetaRango con %d%% de velocidad", tracks, featuresById, { f -> f.tempo.orZero() >= 125.0 }) { f -> normalizarTempo(f.tempo.orZero()) },
            crear("lentas", "Lentas", stats.slow, "LE", "#ECEFF1", "$etiquetaRango con %d%% de tempos lentos", tracks, featuresById, { f -> f.tempo.orZero() in 1.0..105.0 && f.energy.orZero() <= 0.65 }) { f -> 1.0 - normalizarTempo(f.tempo.orZero()) },
            crear("electronicas", "Electronicas", stats.electronic, "EL", "#E8EAF6", "$etiquetaRango con %d%% de pulso electronico", tracks, featuresById, { f -> f.acousticness.orZero() <= 0.35 && f.energy.orZero() >= 0.55 }) { f -> combine((1.0 - f.acousticness.orZero()) to 0.4, f.energy.orZero() to 0.35, f.instrumentalness.orZero() to 0.25) },
            crear("organicas", "Organicas", stats.organic, "OR", "#F1F8E9", "$etiquetaRango con %d%% de textura organica", tracks, featuresById, { f -> f.acousticness.orZero() >= 0.35 && f.speechiness.orZero() <= 0.35 }) { f -> combine(f.acousticness.orZero() to 0.6, (1.0 - f.energy.orZero()) to 0.2, (1.0 - f.speechiness.orZero()) to 0.2) },
            crear("oscuras", "Oscuras", stats.dark, "OS", "#D7CCC8", "$etiquetaRango con %d%% de tono oscuro", tracks, featuresById, { f -> f.valence.orZero() <= 0.40 && f.speechiness.orZero() <= 0.45 }) { f -> combine((1.0 - f.valence.orZero()) to 0.65, f.energy.orZero() to 0.35) },
            crear("alegres", "Alegres", stats.happy, "AL", "#FFFDE7", "$etiquetaRango con %d%% de alegria", tracks, featuresById, { f -> f.valence.orZero() >= 0.55 && f.energy.orZero() >= 0.45 }) { f -> combine(f.valence.orZero() to 0.6, f.energy.orZero() to 0.4) },
            crear("calmadas", "Calmadas", stats.calm, "CA", "#E0F2F1", "$etiquetaRango con %d%% de calma", tracks, featuresById, { f -> f.energy.orZero() <= 0.50 && f.speechiness.orZero() <= 0.35 }) { f -> combine((1.0 - f.energy.orZero()) to 0.55, f.acousticness.orZero() to 0.3, (1.0 - normalizarTempo(f.tempo.orZero())) to 0.15) },
            crear("epicas", "Epicas / potentes", stats.epic, "EP", "#FFECB3", "$etiquetaRango con %d%% de potencia", tracks, featuresById, { f -> f.energy.orZero() >= 0.70 && f.tempo.orZero() >= 100.0 }) { f -> combine(f.energy.orZero() to 0.5, normalizarTempo(f.tempo.orZero()) to 0.3, (1.0 - f.acousticness.orZero()) to 0.2) },
            crear("suaves", "Suaves", stats.soft, "SU", "#FCE4EC", "$etiquetaRango con %d%% de suavidad", tracks, featuresById, { f -> f.energy.orZero() <= 0.65 && f.speechiness.orZero() <= 0.35 }) { f -> combine((1.0 - f.energy.orZero()) to 0.45, f.valence.orZero() to 0.25, f.acousticness.orZero() to 0.3) },
            crear("nocturnas", "Vibras nocturnas", stats.night, "NO", "#E1BEE7", "$etiquetaRango con %d%% de vibra nocturna", tracks, featuresById, { f -> f.valence.orZero() in 0.20..0.58 && f.energy.orZero() in 0.30..0.78 }) { f -> combine(cercania(f.valence.orZero(), 0.38, 0.45) to 0.35, cercania(f.energy.orZero(), 0.55, 0.45) to 0.3, cercania(normalizarTempo(f.tempo.orZero()), 0.48, 0.35) to 0.2, (1.0 - f.acousticness.orZero()) to 0.15) }
        ).sortedByDescending { it.porcentaje }

        return listOf(crearResumen(vibes, etiquetaRango)) + vibes
    }

    fun calcularVibesEstimadas(
        tracks: List<Track>,
        artistGenres: List<String>,
        etiquetaRango: String,
        artistGenresById: Map<String, List<String>> = emptyMap()
    ): List<VibeCard> {
        if (tracks.isEmpty() && artistGenres.isEmpty()) return emptyList()

        val popularity = tracks.map { it.popularity / 100.0 }.averageOrZero()
        val duration = tracks.map { (it.duration_ms / 300_000.0).coerceIn(0.0, 1.0) }.averageOrZero()
        val artistVariety = if (tracks.isEmpty()) {
            0.4
        } else {
            (tracks.flatMap { it.artists }.map { it.name }.toSet().size.toDouble() / tracks.size)
                .coerceIn(0.0, 1.0)
        }

        val vibes = listOf(
            crearEstimado("bailables", "Bailables", combine(scoreGeneros(artistGenres, "dance", "disco", "funk", "reggaeton", "latin", "pop") to 0.70, popularity to 0.30), "BA", "#E3F2FD", "$etiquetaRango con %d%% de ritmo bailable", tracks, artistGenresById, 0, "dance", "disco", "funk", "reggaeton", "latin", "pop"),
            crearEstimado("energeticas", "Energeticas", combine(scoreGeneros(artistGenres, "rock", "metal", "edm", "trap", "punk", "reggaeton", "pop") to 0.65, popularity to 0.35), "EN", "#E8F5E9", "$etiquetaRango con %d%% de energia", tracks, artistGenresById, 1, "rock", "metal", "edm", "trap", "punk", "reggaeton", "pop"),
            crearEstimado("sad", "Sad / melancolicas", combine(scoreGeneros(artistGenres, "sad", "emo", "indie", "alternative", "bedroom", "singer-songwriter") to 0.75, (1.0 - popularity) to 0.25), "SA", "#EDE7F6", "$etiquetaRango con %d%% de vibra melancolica", tracks, artistGenresById, 2, "sad", "emo", "indie", "alternative", "bedroom", "singer-songwriter", "ballad"),
            crearEstimado("positivas", "Positivas", combine(scoreGeneros(artistGenres, "pop", "latin", "funk", "dance", "disco", "happy") to 0.70, popularity to 0.30), "PO", "#FFF9C4", "$etiquetaRango con %d%% de positividad", tracks, artistGenresById, 3, "pop", "latin", "funk", "dance", "disco", "happy"),
            crearEstimado("acusticas", "Acusticas", scoreGeneros(artistGenres, "acoustic", "folk", "singer-songwriter", "country", "cantautor"), "AC", "#FFF3E0", "$etiquetaRango con %d%% de sonido acustico", tracks, artistGenresById, 4, "acoustic", "folk", "singer-songwriter", "country", "cantautor"),
            crearEstimado("habladas", "Habladas / rap", scoreGeneros(artistGenres, "rap", "hip hop", "trap", "urbano", "spoken"), "SP", "#E0F7FA", "$etiquetaRango con %d%% de voz hablada o rap", tracks, artistGenresById, 5, "rap", "hip hop", "trap", "urbano", "spoken"),
            crearEstimado("intensas", "Intensas", combine(scoreGeneros(artistGenres, "rock", "metal", "trap", "punk", "edm", "hardcore") to 0.65, popularity to 0.35), "IT", "#FFE0B2", "$etiquetaRango con %d%% de intensidad", tracks, artistGenresById, 6, "rock", "metal", "trap", "punk", "edm", "hardcore"),
            crearEstimado("relajadas", "Relajadas", scoreGeneros(artistGenres, "chill", "lo-fi", "lofi", "ambient", "acoustic", "bedroom", "soul"), "RE", "#E0F2F1", "$etiquetaRango con %d%% de calma relajada", tracks, artistGenresById, 7, "chill", "lo-fi", "lofi", "ambient", "acoustic", "bedroom", "soul"),
            crearEstimado("rapidas", "Rapidas", scoreGeneros(artistGenres, "edm", "techno", "house", "drum", "reggaeton", "punk", "dance"), "RA", "#FBE9E7", "$etiquetaRango con %d%% de velocidad", tracks, artistGenresById, 8, "edm", "techno", "house", "drum", "reggaeton", "punk", "dance"),
            crearEstimado("lentas", "Lentas", combine(duration to 0.60, scoreGeneros(artistGenres, "ballad", "soul", "ambient", "acoustic", "r&b") to 0.40), "LE", "#ECEFF1", "$etiquetaRango con %d%% de tempos lentos", tracks, artistGenresById, 9, "ballad", "soul", "ambient", "acoustic", "r&b"),
            crearEstimado("electronicas", "Electronicas", scoreGeneros(artistGenres, "electronic", "house", "techno", "edm", "synth", "dance"), "EL", "#E8EAF6", "$etiquetaRango con %d%% de pulso electronico", tracks, artistGenresById, 10, "electronic", "house", "techno", "edm", "synth", "dance"),
            crearEstimado("organicas", "Organicas", combine(scoreGeneros(artistGenres, "acoustic", "folk", "indie", "singer-songwriter", "soul") to 0.75, artistVariety to 0.25), "OR", "#F1F8E9", "$etiquetaRango con %d%% de textura organica", tracks, artistGenresById, 11, "acoustic", "folk", "indie", "singer-songwriter", "soul"),
            crearEstimado("oscuras", "Oscuras", scoreGeneros(artistGenres, "dark", "goth", "metal", "emo", "alternative", "industrial"), "OS", "#D7CCC8", "$etiquetaRango con %d%% de tono oscuro", tracks, artistGenresById, 12, "dark", "goth", "metal", "emo", "alternative", "industrial"),
            crearEstimado("alegres", "Alegres", combine(scoreGeneros(artistGenres, "pop", "latin", "dance", "disco", "funk") to 0.70, popularity to 0.30), "AL", "#FFFDE7", "$etiquetaRango con %d%% de alegria", tracks, artistGenresById, 13, "pop", "latin", "dance", "disco", "funk"),
            crearEstimado("calmadas", "Calmadas", scoreGeneros(artistGenres, "chill", "ambient", "acoustic", "folk", "lo-fi", "lofi"), "CA", "#E0F2F1", "$etiquetaRango con %d%% de calma", tracks, artistGenresById, 14, "chill", "ambient", "acoustic", "folk", "lo-fi", "lofi"),
            crearEstimado("epicas", "Epicas / potentes", combine(scoreGeneros(artistGenres, "soundtrack", "epic", "metal", "rock", "cinematic") to 0.65, popularity to 0.35), "EP", "#FFECB3", "$etiquetaRango con %d%% de potencia", tracks, artistGenresById, 15, "soundtrack", "epic", "metal", "rock", "cinematic"),
            crearEstimado("suaves", "Suaves", scoreGeneros(artistGenres, "r&b", "soul", "acoustic", "chill", "indie", "bedroom"), "SU", "#FCE4EC", "$etiquetaRango con %d%% de suavidad", tracks, artistGenresById, 16, "r&b", "soul", "acoustic", "chill", "indie", "bedroom"),
            crearEstimado("nocturnas", "Vibras nocturnas", scoreGeneros(artistGenres, "night", "synth", "ambient", "trap", "r&b", "urbano", "chill"), "NO", "#E1BEE7", "$etiquetaRango con %d%% de vibra nocturna", tracks, artistGenresById, 17, "night", "synth", "ambient", "trap", "r&b", "urbano", "chill")
        ).sortedByDescending { it.porcentaje }

        return listOf(crearResumen(vibes, etiquetaRango)) + vibes
    }

    private fun calcularStatsPonderadas(
        tracks: List<Track>,
        features: List<AudioFeatures>
    ): VibeStats {
        val featuresById = features.mapNotNull { feature ->
            feature.id?.let { it to feature }
        }.toMap()

        val ordered = tracks.mapNotNull { featuresById[it.id] }.ifEmpty { features }
        val totalWeight = ordered.indices.sumOf { index -> ordered.size - index }.toDouble()

        fun weighted(selector: (AudioFeatures) -> Double?): Double {
            val weightedSum = ordered.mapIndexedNotNull { index, feature ->
                selector(feature)?.coerceIn(0.0, 1.0)?.let { value ->
                    value * (ordered.size - index)
                }
            }.sum()
            return if (totalWeight == 0.0) 0.0 else weightedSum / totalWeight
        }

        fun weightedTempo(): Double {
            val weightedSum = ordered.mapIndexedNotNull { index, feature ->
                feature.tempo?.let { tempo ->
                    normalizarTempo(tempo) * (ordered.size - index)
                }
            }.sum()
            return if (totalWeight == 0.0) 0.0 else weightedSum / totalWeight
        }

        val danceability = weighted { it.danceability }
        val energy = weighted { it.energy }
        val acousticness = weighted { it.acousticness }
        val instrumentalness = weighted { it.instrumentalness }
        val liveness = weighted { it.liveness }
        val valence = weighted { it.valence }
        val speechiness = (weighted { it.speechiness } / 0.66).coerceIn(0.0, 1.0)
        val tempo = weightedTempo()
        val popularity = tracks.map { it.popularity / 100.0 }.averageOrZero()

        return VibeStats(
            danceability = danceability,
            energy = energy,
            acousticness = acousticness,
            instrumentalness = instrumentalness,
            liveness = liveness,
            valence = valence,
            spoken = speechiness,
            fast = tempo,
            slow = 1.0 - tempo,
            sad = combine((1.0 - valence) to 0.7, (1.0 - energy) to 0.3),
            intense = combine(energy to 0.55, tempo to 0.35, (1.0 - acousticness) to 0.10),
            relaxed = combine((1.0 - energy) to 0.45, (1.0 - tempo) to 0.25, acousticness to 0.30),
            electronic = combine((1.0 - acousticness) to 0.35, energy to 0.25, instrumentalness to 0.20, tempo to 0.10, (1.0 - liveness) to 0.10),
            organic = combine(acousticness to 0.50, (1.0 - energy) to 0.20, (1.0 - speechiness) to 0.15, (1.0 - instrumentalness) to 0.15),
            dark = combine((1.0 - valence) to 0.65, energy to 0.35),
            happy = combine(valence to 0.60, energy to 0.40),
            calm = combine((1.0 - energy) to 0.55, acousticness to 0.30, (1.0 - tempo) to 0.15),
            epic = combine(energy to 0.45, tempo to 0.25, (1.0 - acousticness) to 0.15, popularity to 0.15),
            soft = combine((1.0 - energy) to 0.40, valence to 0.25, acousticness to 0.25, (1.0 - speechiness) to 0.10),
            night = combine(
                cercania(valence, 0.38, 0.45) to 0.35,
                cercania(energy, 0.55, 0.45) to 0.30,
                cercania(tempo, 0.48, 0.35) to 0.20,
                (1.0 - acousticness) to 0.15
            )
        )
    }

    private fun crear(
        id: String,
        nombre: String,
        score: Double,
        icono: String,
        color: String,
        descripcion: String,
        tracks: List<Track>,
        featuresById: Map<String, AudioFeatures>,
        validator: ((AudioFeatures) -> Boolean)? = null,
        scorer: ((AudioFeatures) -> Double)? = null
    ): VibeCard {
        val percentage = score.toPercentage()
        val example = elegirEjemplo(tracks, featuresById, validator, scorer)
        return VibeCard(
            id = id,
            nombre = nombre,
            porcentaje = percentage,
            icono = icono,
            color = color,
            descripcion = descripcion.format(percentage),
            ejemploCancion = example?.name ?: "Sin ejemplo",
            ejemploArtista = example?.artists?.joinToString(", ") { it.name } ?: "Spotify",
            imagenUrl = example?.album?.images?.firstOrNull()?.url
        )
    }

    private fun crearResumen(vibes: List<VibeCard>, etiquetaRango: String): VibeCard {
        val top = vibes.first()
        val topNames = vibes.take(3).joinToString(", ") { it.nombre.lowercase() }
        return VibeCard(
            id = "resumen",
            nombre = "Tu vibra dominante",
            porcentaje = top.porcentaje,
            icono = "TOP",
            color = top.color,
            descripcion = "$etiquetaRango destaca por $topNames",
            ejemploCancion = top.ejemploCancion,
            ejemploArtista = top.ejemploArtista,
            imagenUrl = top.imagenUrl
        )
    }

    private fun crearEstimado(
        id: String,
        nombre: String,
        score: Double,
        icono: String,
        color: String,
        descripcion: String,
        tracks: List<Track>,
        artistGenresById: Map<String, List<String>>,
        fallbackIndex: Int,
        vararg keywords: String
    ): VibeCard {
        val percentage = score.toPercentage()
        val example = elegirEjemploEstimado(tracks, artistGenresById, fallbackIndex, keywords.toList())
        return VibeCard(
            id = id,
            nombre = nombre,
            porcentaje = percentage,
            icono = icono,
            color = color,
            descripcion = descripcion.format(percentage),
            ejemploCancion = example?.name ?: "Sin ejemplo",
            ejemploArtista = example?.artists?.joinToString(", ") { it.name } ?: "Spotify",
            imagenUrl = example?.album?.images?.firstOrNull()?.url
        )
    }

    private fun elegirEjemploEstimado(
        tracks: List<Track>,
        artistGenresById: Map<String, List<String>>,
        fallbackIndex: Int,
        keywords: List<String>
    ): Track? {
        if (tracks.isEmpty()) return null
        val scoredTracks = tracks.map { track ->
            val genres = track.artists.flatMap { artist -> artistGenresById[artist.id].orEmpty() }
            val score = genres.sumOf { genre ->
                keywords.count { keyword -> genre.lowercase().contains(keyword) }
            }
            track to score
        }
        val best = scoredTracks.maxByOrNull { it.second }
        return if (best != null && best.second > 0) {
            best.first
        } else if (artistGenresById.isNotEmpty()) {
            null
        } else {
            tracks[fallbackIndex % tracks.size]
        }
    }

    private fun elegirEjemplo(
        tracks: List<Track>,
        featuresById: Map<String, AudioFeatures>,
        validator: ((AudioFeatures) -> Boolean)?,
        scorer: ((AudioFeatures) -> Double)?
    ): Track? {
        if (tracks.isEmpty()) return null
        if (scorer == null || featuresById.isEmpty()) return tracks.firstOrNull()
        val candidates = tracks.filter { track ->
            val feature = featuresById[track.id]
            feature != null && (validator?.invoke(feature) ?: true)
        }

        if (candidates.isEmpty() && validator != null) return null
        val safeCandidates = candidates.ifEmpty {
            tracks.filter { track -> featuresById[track.id] != null }
        }

        return safeCandidates.maxByOrNull { track ->
            featuresById[track.id]?.let(scorer) ?: -1.0
        } ?: tracks.firstOrNull()
    }

    private fun normalizarTempo(tempo: Double): Double {
        return ((tempo - 70.0) / 100.0).coerceIn(0.0, 1.0)
    }

    private fun cercania(value: Double, target: Double, spread: Double): Double {
        return (1.0 - (abs(value - target) / spread)).coerceIn(0.0, 1.0)
    }

    private fun combine(vararg values: Pair<Double, Double>): Double {
        val totalWeight = values.sumOf { it.second }
        if (totalWeight == 0.0) return 0.0
        return (values.sumOf { it.first.coerceIn(0.0, 1.0) * it.second } / totalWeight)
            .coerceIn(0.0, 1.0)
    }

    private fun scoreGeneros(genres: List<String>, vararg keywords: String): Double {
        if (genres.isEmpty()) return 0.35
        val normalizedGenres = genres.map { it.lowercase() }
        val hits = normalizedGenres.count { genre ->
            keywords.any { keyword -> genre.contains(keyword) }
        }
        return ((hits.toDouble() / normalizedGenres.size) * 2.4).coerceIn(0.12, 1.0)
    }

    private fun Double?.orZero(): Double = this ?: 0.0

    private fun Double.toPercentage(): Int = (coerceIn(0.0, 1.0) * 100).roundToInt()

    private fun List<Double>.averageOrZero(): Double {
        return if (isEmpty()) 0.0 else average().coerceIn(0.0, 1.0)
    }

    private data class VibeStats(
        val danceability: Double,
        val energy: Double,
        val acousticness: Double,
        val instrumentalness: Double,
        val liveness: Double,
        val valence: Double,
        val spoken: Double,
        val fast: Double,
        val slow: Double,
        val sad: Double,
        val intense: Double,
        val relaxed: Double,
        val electronic: Double,
        val organic: Double,
        val dark: Double,
        val happy: Double,
        val calm: Double,
        val epic: Double,
        val soft: Double,
        val night: Double
    )
}
