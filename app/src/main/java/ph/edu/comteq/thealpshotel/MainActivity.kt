package ph.edu.comteq.thealpshotel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.gson.Gson
import com.google.gson.JsonObject
import ph.edu.comteq.thealpshotel.ui.theme.TheAlpsHotelTheme
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TheAlpsHotelTheme {
                MainScreen()
            }
        }
    }
}

data class BookingData(
    val hotelId: Int,
    val hotelName: String,
    val room: Room
)

@Composable
fun MainScreen() {
    var showProfile by remember { mutableStateOf(false) }
    var selectedHotelId by remember { mutableStateOf<Int?>(null) }
    var bookingData by remember { mutableStateOf<BookingData?>(null) }

    when {
        showProfile -> ProfileScreen(onBack = { showProfile = false })
        bookingData != null -> BookingConfirmScreen(
            bookingData = bookingData!!,
            onBack = { bookingData = null }
        )
        selectedHotelId != null -> GuestReviewsScreen(
            hotelId = selectedHotelId!!,
            onBack = { selectedHotelId = null },
            onRoomClick = { hotelId, hotelName, room ->
                bookingData = BookingData(hotelId, hotelName, room)
            }
        )
        else -> Homepage(
            onProfileClick = { showProfile = true },
            onHotelClick = { hotelId -> selectedHotelId = hotelId }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Homepage(
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onHotelClick: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    var hotels by remember { mutableStateOf(emptyList<Hotel>()) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredHotels = hotels.filter {
        it.hotel_name.contains(searchQuery, ignoreCase = true)
    }

    LaunchedEffect(Unit) {
        val json = context.assets.open("hotels.json")
            .bufferedReader()
            .use { it.readText() }
        val gson = Gson()
        val hotelsArray = gson.fromJson(json, Array<Hotel>::class.java)
        hotels = hotelsArray.toList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF5F5F5), Color(0xFFEAEAEA))
                )
            )
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "The Alp’s Hotel",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.france_national_flag),
                        contentDescription = "France Flag",
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1E88E5)
            ),
            actions = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "User Icon",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(28.dp)
                        .clickable { onProfileClick() }
                )
            }
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search...") },
            singleLine = true,
            shape = RoundedCornerShape(50.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredHotels) { hotel ->
                HotelCard(hotel = hotel, onClick = { onHotelClick(hotel.hotel_id) })
            }
        }
    }
}

@Composable
fun RoomCard(room: Room, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = room.room_type,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Bed: ${room.room_bed_type}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Total number of guests: ${room.room_total_number_of_guests}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = room.room_features.take(3).joinToString(", "),
                    fontSize = 12.sp,
                    color = Color(0xFF757575),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "€${room.room_price_for_one_night}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E88E5),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun HotelCard(hotel: Hotel, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("file:///android_asset/${hotel.hotel_cover_image}")
                    .crossfade(true)
                    .build(),
                contentDescription = hotel.hotel_name,
                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClick() },
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = hotel.hotel_name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onClick() }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = hotel.hotel_rating.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E88E5)
                    )
                    val starCount = kotlin.math.round(hotel.hotel_rating).toInt()
                    repeat(starCount) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = "Star",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFFD700)
                        )
                    }
                }

                Text(
                    text = "${hotel.hotel_to_ski_distance} km to ski",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestReviewsScreen(hotelId: Int, onBack: () -> Unit, onRoomClick: (Int, String, Room) -> Unit = { _, _, _ -> }) {
    val context = LocalContext.current
    var hotelName by remember { mutableStateOf("") }
    var ratings by remember { mutableStateOf(listOf<Pair<String, Double>>()) }
    var reviews by remember { mutableStateOf(listOf<Map<String, String>>()) }
    var rooms by remember { mutableStateOf(emptyList<Room>()) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    LaunchedEffect(hotelId) {
        val fileName = "hotels_details.$hotelId.json"
        val jsonStream = context.assets.open(fileName)
        val json = InputStreamReader(jsonStream).readText()
        val gson = Gson()
        val obj = gson.fromJson(json, JsonObject::class.java)

        hotelName = obj.get("hotel_name").asString

        val ratingsArray = obj.getAsJsonObject("guest_reviews")
            .getAsJsonArray("ratings_categories")

        ratings = ratingsArray.map { item ->
            val ratingObj = item.asJsonObject
            val key = ratingObj.keySet().first()
            key to ratingObj.get(key).asDouble
        }

        val reviewsArray = obj.getAsJsonObject("guest_reviews")
            .getAsJsonArray("reviews_objects")

        reviews = reviewsArray.map { review ->
            val r = review.asJsonObject
            mapOf(
                "username" to r.get("username").asString,
                "country" to r.get("country").asString,
                "review_text" to r.get("review_text").asString
            )
        }

        val roomsArray = obj.getAsJsonArray("rooms")
        rooms = roomsArray.map { room ->
            val r = room.asJsonObject
            Room(
                room_id = r.get("room_id").asInt,
                room_type = r.get("room_type").asString,
                room_bed_type = r.get("room_bed_type").asString,
                room_total_number_of_guests = r.get("room_total_number_of_guests").asInt,
                room_features = r.getAsJsonArray("room_features").map { it.asString },
                room_price_for_one_night = r.get("room_price_for_one_night").asInt
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "The Alps' Hotels",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Image(
                            painter = painterResource(id = R.drawable.france_national_flag),
                            contentDescription = "France Flag",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(28.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E88E5))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            val tabs = listOf("Guest Reviews", "Room Selection")
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF1E88E5)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                title,
                                color = if (selectedTabIndex == index) Color(0xFF1E88E5) else Color.Gray,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedTabIndex == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp)
                ) {
                    item {
                        Text(
                            text = hotelName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )


                        Text("Ratings", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6)),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                ratings.forEach { (title, value) ->
                                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                title,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 16.sp,
                                                color = Color(0xFF212121),
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = String.format("%.1f", value),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color(0xFF1E88E5)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { (value / 10).toFloat() },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(10.dp)
                                                .clip(RoundedCornerShape(50.dp)),
                                            color = Color(0xFF1E88E5),
                                            trackColor = Color(0xFFDDE7F2)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Reviews", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // review design
                    item {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 16.dp)
                        ) {
                            reviews.forEach { r ->
                                Card(
                                    modifier = Modifier
                                        .width(250.dp)
                                        .padding(end = 12.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(3.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFDCE6F1)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = r["username"]?.firstOrNull()?.toString() ?: "",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1E88E5)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = r["username"] ?: "",
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = r["country"] ?: "",
                                                    color = Color.Gray,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = r["review_text"] ?: "",
                                            fontSize = 14.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp)
                ) {
                    item {
                        Text(
                            text = hotelName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(rooms) { room ->
                        RoomCard(
                            room = room,
                            onClick = { onRoomClick(hotelId, hotelName, room) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "The Alp’s Hotel",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Image(
                            painter = painterResource(id = R.drawable.france_national_flag),
                            contentDescription = "France Flag",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "User Icon",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(28.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E88E5)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ronald),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ronald Guiyab",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )

            Text(
                text = "Developer",
                fontSize = 18.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "This app is part of our Android Studio subject in Comteq Computer and Business College, where we learn to build modern and functional mobile applications using Kotlin and Jetpack Compose.",
                fontSize = 16.sp,
                color = Color(0xFF424242),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmScreen(bookingData: BookingData, onBack: () -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var checkInDate by remember { mutableStateOf("Tue, Sep 10, 2024") }
    var checkOutDate by remember { mutableStateOf("Sun, Sep 15, 2024") }
    var adults by remember { mutableStateOf(2) }
    var children by remember { mutableStateOf(0) }
    var isBusinessTravel by remember { mutableStateOf(false) }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Calculate number of rooms based on total guests
    val totalGuests = adults + children
    val rooms = remember(totalGuests, bookingData.room.room_total_number_of_guests) {
        kotlin.math.ceil(totalGuests.toDouble() / bookingData.room.room_total_number_of_guests).toInt()
    }

    // Calculate number of nights
    val nights = remember(checkInDate, checkOutDate) {
        try {
            val dateFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US)
            val checkIn = dateFormat.parse(checkInDate)
            val checkOut = dateFormat.parse(checkOutDate)
            if (checkIn != null && checkOut != null) {
                val diff = checkOut.time - checkIn.time
                (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
            } else {
                5 // default
            }
        } catch (e: Exception) {
            5 // default
        }
    }

    // Calculate total price
    val basePrice = bookingData.room.room_price_for_one_night * rooms * nights
    val businessFee = if (isBusinessTravel) 150 else 0
    val totalPrice = basePrice + businessFee

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Booking Confirm",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E88E5))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You are going to reserve:",
                fontSize = 16.sp,
                color = Color(0xFF424242)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = bookingData.hotelName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Room details card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bookingData.room.room_type,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bed: ${bookingData.room.room_bed_type}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Total number of guests: $totalGuests",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = "€${bookingData.room.room_price_for_one_night}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E88E5)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Form",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Personal details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("First Name") },
                    placeholder = { Text("First Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Last Name") },
                    placeholder = { Text("Last Name") },
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = checkInDate,
                    onValueChange = { 
                        val formatted = formatDateInput(it)
                        if (formatted != null) checkInDate = formatted
                        else checkInDate = it
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Check-in date") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = checkOutDate,
                    onValueChange = { 
                        val formatted = formatDateInput(it)
                        if (formatted != null) checkOutDate = formatted
                        else checkOutDate = it
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Check-out date") },
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Room type
            Text(
                text = bookingData.room.room_type,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Occupancy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = adults.toString(),
                    onValueChange = { 
                        val value = it.toIntOrNull() ?: 0
                        if (value >= 0) adults = value
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Adults") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = children.toString(),
                    onValueChange = { 
                        val value = it.toIntOrNull() ?: 0
                        if (value >= 0) children = value
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Children") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = rooms.toString(),
                    onValueChange = {},
                    modifier = Modifier.weight(1f),
                    label = { Text("Room") },
                    singleLine = true,
                    enabled = false
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Travel purpose
            Text(
                text = "Travel for business?",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = !isBusinessTravel,
                    onClick = { isBusinessTravel = false }
                )
                Text(
                    text = "For sightseeing",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = isBusinessTravel,
                    onClick = { isBusinessTravel = true }
                )
                Text(
                    text = "+ €150 For business with a meeting room",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment method
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Which way to pay?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = paymentMethod == "Cash",
                            onClick = { paymentMethod = "Cash" }
                        )
                        Text(
                            text = "Cash",
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = paymentMethod == "Credit card",
                            onClick = { paymentMethod = "Credit card" }
                        )
                        Text(
                            text = "Credit card",
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = paymentMethod == "E-Pay",
                            onClick = { paymentMethod = "E-Pay" }
                        )
                        Text(
                            text = "E-Pay",
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                Text(
                    text = "€ $totalPrice",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E88E5),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Book now button
            Button(
                onClick = {
                    // Validate form
                    when {
                        firstName.isBlank() -> {
                            errorMessage = "Please enter your first name"
                            showErrorDialog = true
                        }
                        lastName.isBlank() -> {
                            errorMessage = "Please enter your last name"
                            showErrorDialog = true
                        }
                        !isValidDate(checkInDate) -> {
                            errorMessage = "Please enter a valid check-in date"
                            showErrorDialog = true
                        }
                        !isValidDate(checkOutDate) -> {
                            errorMessage = "Please enter a valid check-out date"
                            showErrorDialog = true
                        }
                        adults <= 0 -> {
                            errorMessage = "Please enter at least 1 adult"
                            showErrorDialog = true
                        }
                        else -> {
                            showConfirmDialog = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
            ) {
                Text(
                    text = "Book now",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Confirmation dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Booking") },
            text = { Text("Are you going to book this room?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        // In a real app, save the booking here
                        // For now, just navigate back
                        onBack()
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    // Error dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

fun formatDateInput(input: String): String? {
    val patterns = listOf(
        "MM/dd/yyyy",
        "MM-dd-yyyy",
        "MMM dd yyyy",
        "dd/MM/yyyy",
        "dd-MM-yyyy"
    )
    
    val outputFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US)
    
    for (pattern in patterns) {
        try {
            val inputFormat = SimpleDateFormat(pattern, Locale.US)
            val date = inputFormat.parse(input)
            if (date != null) {
                return outputFormat.format(date)
            }
        } catch (e: Exception) {
            // Try next pattern
        }
    }
    return null
}

fun isValidDate(dateString: String): Boolean {
    return try {
        val format = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US)
        format.parse(dateString)
        true
    } catch (e: Exception) {
        false
    }
}

@Preview(showBackground = true)
@Composable
fun HomepagePreview() {
    TheAlpsHotelTheme {
        Homepage(modifier = Modifier)
    }
}
