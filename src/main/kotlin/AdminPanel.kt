import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.petshop.database.PetshopQueries
import org.jetbrains.skia.FontWeight

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
                    status = it.status
                )
            }
        )
    }
    LaunchedEffect(true) { refreshAnimals() }

    Box(contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text("Admin Panel", style = MaterialTheme.typography.h4 .copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = Color.Black)
            Text("Welcome, $username", style = MaterialTheme.typography.subtitle1 .copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = Color.Black)
            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(800.dp)
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

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
            ) {
                Text("Logout", color = Color.White)
            }
        }

        if (showAddDialog) {
            AnimalDialog(
                title = "Add Animal",
                onConfirm = { name, species, age, price, status ->
                    animalsQueries.insertAnimal(
                        name = name,
                        species = species,
                        age = age.toLong(),
                        price = price,
                        status = status
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
                onConfirm = { name, species, age, price, status ->
                    animalsQueries.updateAnimal(
                        name = name,
                        species = species,
                        age = age.toLong(),
                        price = price,
                        status = status,
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
            .clickable(onClick = onAdd),
        elevation = 8.dp,
        backgroundColor = Color(0xFF808080)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "+",
                color = Color.White,
                style = MaterialTheme.typography.h3
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
            .height(100.dp)
            .padding(horizontal = 8.dp),
        elevation = 8.dp
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text("Name: ${animal.name}", style = MaterialTheme.typography.subtitle1)
                Text("Species: ${animal.species}", style = MaterialTheme.typography.body2)
                Text("Age: ${animal.age}", style = MaterialTheme.typography.body2)
                Text("Price: $${animal.price}", style = MaterialTheme.typography.body2)
                Text("Status: ${animal.status}", style = MaterialTheme.typography.body2)
            }
            Row {
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
                    modifier = Modifier.padding(end = 8.dp)
                ) { Text("Modify", color = Color.White) }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                    modifier = Modifier.padding(end = 8.dp)
                ) { Text("Delete", color = Color.White) }
                if (animal.status == "Available") {
                    Button(
                        onClick = onMarkBought,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF388E3C))
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
    onConfirm: (name: String, species: String, age: Long, price: Double, status: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var species by remember { mutableStateOf(initialSpecies) }
    var age by remember { mutableStateOf(initialAge) }
    var price by remember { mutableStateOf(initialPrice) }
    var status by remember { mutableStateOf(initialStatus) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = species,
                    onValueChange = { species = it },
                    label = { Text("Species") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Status: ")
                    Spacer(Modifier.width(8.dp))
                    DropdownMenuStatusSelector(selectedStatus = status, onStatusSelected = { status = it })
                }
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
                        onConfirm(name, species, ageLong, priceDouble, status)
                        error = null
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Cancel", color = Color.White)
            }
        }
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
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
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