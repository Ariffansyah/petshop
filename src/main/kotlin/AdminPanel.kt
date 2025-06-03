import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import app.petshop.database.PetshopQueries

@Composable
fun AdminPanel(
    onLogout: () -> Unit,
    username: String,
    animalsQueries: PetshopQueries
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var animalToEdit by remember { mutableStateOf<Animal?>(null) }

    val animalsState = remember { mutableStateListOf<Animal>() }
    fun refreshAnimals() {
        animalsState.clear()
        animalsState.addAll(
            animalsQueries.selectAllAnimals().executeAsList().map {
                Animal(
                    id = it.id.toLong(),
                    name = it.name,
                    species = it.species,
                    age = it.age.toInt(),
                    price = it.price,
                    status = it.status,
                    owner = it.owner
                )
            }
        )
    }
    LaunchedEffect(true) { refreshAnimals() }

    val totalAnimals = animalsState.size
    val availableAnimals = animalsState.count { it.status == "Available" }
    val boughtAnimals = animalsState.count { it.status == "Bought" }
    var menuExpanded by remember { mutableStateOf(false) }

    Box(contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F8FC))
                .padding(24.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                        menuExpanded = false
                        onLogout()
                    }) {
                        Text("Logout")
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Text(
                        "\uD83D\uDC36 Admin Panel",
                        style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF00897B)
                    )
                    Text(
                        "Welcome, $username",
                        style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF00695C)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                backgroundColor = Color(0xFFB2DFDB),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total", color = Color(0xFF00695C), fontWeight = FontWeight.Bold)
                        Text("$totalAnimals", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Available", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold)
                        Text("$availableAnimals", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Bought", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                        Text("$boughtAnimals", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(800.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE0F2F1))
            ) {
                val state = rememberLazyListState()
                LazyColumn(
                    state = state,
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        AddAnimalCard(onAdd = { showAddDialog = true })
                        Spacer(Modifier.height(12.dp))
                    }
                    items(animalsState) { animal ->
                        AnimalCard(
                            animal = animal,
                            onEdit = { animalToEdit = animal },
                            onDelete = {
                                animalsQueries.deleteAnimal(animal.id)
                                refreshAnimals()
                            },
                            onMarkBought = {
                                animalsQueries.updateAnimalStatus("Bought", animal.id)
                                refreshAnimals()
                            }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(state),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                )
            }
        }

        if (showAddDialog) {
            AnimalDialog(
                title = "Add Animal",
                onConfirm = { name, species, age, price, status, owner ->
                    animalsQueries.insertAnimal(
                        name = name,
                        species = species,
                        age = age.toLong(),
                        price = price,
                        status = status,
                        owner = owner
                    )
                    refreshAnimals()
                    showAddDialog = false
                },
                onDismiss = { showAddDialog = false }
            )
        }

        animalToEdit?.let { animal ->
            AnimalDialog(
                title = "Edit Animal",
                initialName = animal.name,
                initialSpecies = animal.species,
                initialAge = animal.age.toString(),
                initialPrice = animal.price.toString(),
                initialStatus = animal.status,
                initialOwner = animal.owner,
                onConfirm = { name, species, age, price, status, owner ->
                    animalsQueries.updateAnimal(
                        name = name,
                        species = species,
                        age = age.toLong(),
                        price = price,
                        status = status,
                        owner = owner,
                        id = animal.id
                    )
                    refreshAnimals()
                    animalToEdit = null
                },
                onDismiss = { animalToEdit = null }
            )
        }
    }
}

@Composable
fun AddAnimalCard(
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onAdd),
        elevation = 8.dp,
        backgroundColor = Color(0xFFB2DFDB)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "+ Add Animal",
                color = Color(0xFF00695C),
                style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun AnimalCard(
    animal: Animal,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkBought: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(20.dp)),
        elevation = 8.dp,
        backgroundColor = Color.White
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = when (animal.species.lowercase()) {
                    "dog" -> "\uD83D\uDC36"
                    "cat" -> "\uD83D\uDC31"
                    "rabbit" -> "\uD83D\uDC30"
                    "bird" -> "\uD83D\uDC26"
                    else -> "\uD83D\uDC3E"
                },
                fontSize = androidx.compose.ui.unit.TextUnit.Unspecified,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text("Name: ${animal.name}", style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = Color(0xFF00897B)))
                Text("Species: ${animal.species}", style = MaterialTheme.typography.body2)
                Text("Age: ${animal.age}", style = MaterialTheme.typography.body2)
                Text("Price: $${animal.price}", style = MaterialTheme.typography.body2.copy(color = Color(0xFF388E3C), fontWeight = FontWeight.Bold))
                Text("Status: ${animal.status}", style = MaterialTheme.typography.body2)
                animal.owner?.let {
                    Text("Owner: $it", style = MaterialTheme.typography.body2)
                }
            }
            Row {
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF80CBC4)),
                    modifier = Modifier.padding(end = 8.dp).clip(RoundedCornerShape(12.dp))
                ) { Text("Modify", color = Color.White) }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFD32F2F)),
                    modifier = Modifier.padding(end = 8.dp).clip(RoundedCornerShape(12.dp))
                ) { Text("Delete", color = Color.White) }
                if (animal.status == "Available") {
                    Button(
                        onClick = onMarkBought,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF388E3C)),
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) { Text("Mark as Bought", color = Color.White) }
                }
            }
        }
    }
}

@Composable
fun AnimalDialog(
    title: String,
    initialName: String = "",
    initialSpecies: String = "",
    initialAge: String = "",
    initialPrice: String = "",
    initialStatus: String = "Available",
    initialOwner: String? = null,
    onConfirm: (name: String, species: String, age: Long, price: Double, status: String, owner: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var species by remember { mutableStateOf(initialSpecies) }
    var age by remember { mutableStateOf(initialAge) }
    var price by remember { mutableStateOf(initialPrice) }
    var status by remember { mutableStateOf(initialStatus) }
    var owner by remember { mutableStateOf(initialOwner ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = Color(0xFF00897B), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                )
                OutlinedTextField(
                    value = species,
                    onValueChange = { species = it },
                    label = { Text("Species") },
                    singleLine = true,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                )
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    singleLine = true,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    singleLine = true,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Status: ")
                    Spacer(Modifier.width(8.dp))
                    DropdownMenuStatusSelector(selectedStatus = status, onStatusSelected = { status = it })
                }
                OutlinedTextField(
                    value = owner,
                    onValueChange = { owner = it },
                    label = { Text("Owner (username, optional)") },
                    singleLine = true,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                )
                error?.let { Text(it, color = Color.Red) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ageLong = age.toLongOrNull()
                    val priceDouble = price.toDoubleOrNull()
                    if (name.isBlank() || species.isBlank() || ageLong == null || priceDouble == null || status.isBlank()) {
                        error = "Please fill all fields correctly."
                    } else {
                        onConfirm(name, species, ageLong, priceDouble, status, owner.ifBlank { null })
                        error = null
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00897B)),
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Text("Cancel", color = Color.White)
            }
        },
        shape = RoundedCornerShape(24.dp),
        backgroundColor = Color(0xFFE0F2F1)
    )
}

@Composable
fun DropdownMenuStatusSelector(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf("Available", "Bought")
    Box {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00897B)),
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            Text(selectedStatus, color = Color.White)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            statuses.forEach { s ->
                DropdownMenuItem(onClick = {
                    onStatusSelected(s)
                    expanded = false
                }) {
                    Text(s)
                }
            }
        }
    }
}
