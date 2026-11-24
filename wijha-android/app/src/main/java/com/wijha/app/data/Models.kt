package com.wijha.app.data

enum class FlightStatus { ONTIME, DELAYED, CANCELLED }

/** رحلة جوية يسجّلها المستخدم. التاريخ yyyy-MM-dd والوقت HH:mm بتوقيت مطار الإقلاع. */
data class Flight(
    val id: String,
    val no: String,
    val date: String,
    val time: String,
    val from: String,
    val to: String,
    val trip: String,
    val status: FlightStatus = FlightStatus.ONTIME,
    val delay: Int = 0,
    val seat: String = "",
    val gate: String = "",
    val term: String = "",
)

data class PhotoItem(val id: String, val caption: String = "")

sealed interface Block {
    data class Heading(val text: String = "") : Block
    data class Paragraph(val text: String = "") : Block
    data class Quote(val text: String = "") : Block
    data class Tip(val text: String = "") : Block
    data class Place(
        val name: String = "",
        val kind: String = "معلم",
        val rating: Int = 0,
        val cost: String = "",
        val note: String = "",
    ) : Block
    data class Photos(val template: String = "duo", val items: List<PhotoItem> = emptyList()) : Block
}

data class Post(
    val id: String,
    val title: String,
    val trip: String,
    val date: String,
    val tags: List<String> = emptyList(),
    val cover: String = "",
    val blocks: List<Block> = emptyList(),
    val sample: Boolean = false,
)

data class AppData(
    val name: String = "",
    val home: String = "RUH",
    val flights: List<Flight> = emptyList(),
    val posts: List<Post> = emptyList(),
    /** نسب الصور (المصوّر والترخيص) حسب معرّف الصورة */
    val credits: Map<String, String> = emptyMap(),
    val sampleFlights: Boolean = false,
    val seeded: Boolean = false,
)

data class Stamp(
    val cc: String,
    val city: String,
    val date: java.time.LocalDate,
    val shape: Int,
    val ink: Int,
    val rot: Float,
)

data class CommonsPhoto(val url: String, val credit: String)
