package com.example.rumahmakancakri

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// Data Models
data class MenuItem(
    val id: Int,
    val name: String,
    val price: String,
    val description: String,
    val imageRes: Int = android.R.drawable.ic_menu_gallery
)

data class RestaurantProfile(
    val name: String,
    val address: String,
    val description: String,
    val hours: String
)

// Preference Manager
class PreferenceManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("RestoPrefs", Context.MODE_PRIVATE)

    fun saveProfile(profile: RestaurantProfile) {
        sharedPreferences.edit().apply {
            putString("name", profile.name)
            putString("address", profile.address)
            putString("description", profile.description)
            putString("hours", profile.hours)
            apply()
        }
    }

    fun getProfile(): RestaurantProfile {
        return RestaurantProfile(
            name = sharedPreferences.getString("name", "Rumah Makan Cak Ri") ?: "Rumah Makan Cak Ri",
            address = sharedPreferences.getString("address", "Jl. Mawar No. 123, Malang") ?: "Jl. Mawar No. 123, Malang",
            description = sharedPreferences.getString("description", "Penyetan legendaris di Malang.") ?: "Penyetan legendaris di Malang.",
            hours = sharedPreferences.getString("hours", "10:00 - 22:00") ?: "10:00 - 22:00"
        )
    }
}

// Hardcoded Menu Data
val menuList = listOf(
    MenuItem(1, "Ayam Goreng Penyet", "Rp 20.000", "Ayam goreng empuk dengan sambal penyet khas Cak Ri."),
    MenuItem(2, "Bebek Goreng", "Rp 25.000", "Bebet goreng garing diluar lembut didalam."),
    MenuItem(3, "Lele Terbang", "Rp 15.000", "Lele goreng krispi yang digoreng lebar."),
    MenuItem(4, "Es Teh Manis", "Rp 5.000", "Minuman segar pelepas dahaga."),
    MenuItem(5, "Es Jeruk Peras", "Rp 7.000", "Jeruk asli diperas segar.")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                preferenceManager = preferenceManager,
                onNavigateToMenu = { navController.navigate("menu") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }
        composable("menu") {
            MenuScreen(
                onNavigateToDetail = { menuId -> navController.navigate("menu_detail/$menuId") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "menu_detail/{menuId}",
            arguments = listOf(navArgument("menuId") { type = NavType.IntType })
        ) { backStackEntry ->
            val menuId = backStackEntry.arguments?.getInt("menuId") ?: 0
            DetailMenuScreen(
                menuId = menuId,
                onBack = { navController.popBackStack() }
            )
        }
        composable("profile") {
            ProfileScreen(
                preferenceManager = preferenceManager,
                onNavigateToEdit = { navController.navigate("edit_profile") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("edit_profile") {
            EditProfileScreen(
                preferenceManager = preferenceManager,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    preferenceManager: PreferenceManager,
    onNavigateToMenu: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val profile = remember { mutableStateOf(preferenceManager.getProfile()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Selamat Datang") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = profile.value.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Rasakan kenikmatan penyetan asli Jawa Timur")
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onNavigateToMenu, modifier = Modifier.fillMaxWidth()) {
                Text("Lihat Menu")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onNavigateToProfile, modifier = Modifier.fillMaxWidth()) {
                Text("Profil Restoran")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(onNavigateToDetail: (Int) -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Menu") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(menuList) { item ->
                ListItem(
                    modifier = Modifier.clickable { onNavigateToDetail(item.id) },
                    headlineContent = { Text(item.name) },
                    supportingContent = { Text(item.price) },
                    leadingContent = {
                        Icon(Icons.Default.Restaurant, contentDescription = null)
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailMenuScreen(menuId: Int, onBack: () -> Unit) {
    val menuItem = menuList.find { it.id == menuId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Menu") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (menuItem != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(150.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = menuItem.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = menuItem.price, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = menuItem.description)
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Kembali ke Menu")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    preferenceManager: PreferenceManager,
    onNavigateToEdit: () -> Unit,
    onBack: () -> Unit
) {
    // Force re-reading profile when this screen is shown
    val profile = preferenceManager.getProfile()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Restoran") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(text = "Nama Restoran:", fontWeight = FontWeight.Bold)
            Text(text = profile.name)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Alamat:", fontWeight = FontWeight.Bold)
            Text(text = profile.address)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Deskripsi Singkat:", fontWeight = FontWeight.Bold)
            Text(text = profile.description)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Jam Buka:", fontWeight = FontWeight.Bold)
            Text(text = profile.hours)
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onNavigateToEdit, modifier = Modifier.fillMaxWidth()) {
                Text("Edit Profil")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    preferenceManager: PreferenceManager,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val currentProfile = remember { preferenceManager.getProfile() }
    var name by remember { mutableStateOf(currentProfile.name) }
    var address by remember { mutableStateOf(currentProfile.address) }
    var description by remember { mutableStateOf(currentProfile.description) }
    var hours by remember { mutableStateOf(currentProfile.hours) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Edit Profil") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Restoran") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Alamat") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = hours, onValueChange = { hours = it }, label = { Text("Jam Buka") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(32.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        preferenceManager.saveProfile(RestaurantProfile(name, address, description, hours))
                        onSave()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Simpan")
                }
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Batal")
                }
            }
        }
    }
}
