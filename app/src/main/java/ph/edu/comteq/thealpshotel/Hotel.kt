package ph.edu.comteq.thealpshotel

data class Hotel(
    val hotel_id: Int,
    val hotel_name: String,
    val hotel_rating: Double,
    val hotel_to_ski_distance: Double,
    val hotel_cover_image: String,
)

data class Room(
    val room_id: Int,
    val room_type: String,
    val room_bed_type: String,
    val room_total_number_of_guests: Int,
    val room_features: List<String>,
    val room_price_for_one_night: Int
)