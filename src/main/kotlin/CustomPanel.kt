import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import animalsQueries
import app.petshop.database.PetshopQueries
import java.time.LocalDateTime
import kotlin.collections.addAll
import kotlin.text.clear
import kotlin.text.toInt
import kotlin.text.toLong

enum class CustomerPage {
    MAIN,
    BROWSE
}

data class Transaction(
    val customer: Customer,
    val animal: Animal,
    val date: LocalDateTime
) {
    fun getTotal(): Double = animal.price
}

@Composable
fun CustomerPanel(
    username: String,
    animalsQueries: PetshopQueries,
    onLogout: () -> Unit
) {
    var currentPage by remember { mutableStateOf(CustomerPage.MAIN) }

    Row(modifier = Modifier.fillMaxSize()) {
        CustomerSidebar(
            username = username,
            currentPage = currentPage,
            onPageSelected = { page -> currentPage = page },
            onLogout = onLogout
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(Color.White)
        ) {
            when (currentPage) {
                CustomerPage.MAIN -> MainPage(
                    onPageSelected = { currentPage = it }
                )
                CustomerPage.BROWSE -> BrowseSearchPage(
                    animalsQueries = animalsQueries,
                    customer = Customer(username = username, password = "")
                )
            }
        }
    }
}

@Composable
fun CustomerSidebar(
    username: String,
    currentPage: CustomerPage,
    onPageSelected: (CustomerPage) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(250.dp)
            .fillMaxHeight()
            .background(Color.LightGray)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Pet Shop",
                style = MaterialTheme.typography.h6 .copy(fontWeight = FontWeight.Bold),
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = "Navigation",
            style = MaterialTheme.typography.subtitle2,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SidebarMenuItem(
            title = "Home",
            isSelected = currentPage == CustomerPage.MAIN,
            onClick = { onPageSelected(CustomerPage.MAIN) }
        )

        SidebarMenuItem(
            title = "Browse",
            isSelected = currentPage == CustomerPage.BROWSE,
            onClick = { onPageSelected(CustomerPage.BROWSE) }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
        ) {
            Text("Logout", color = Color.White)
        }
    }
}

@Composable
fun SidebarMenuItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color.DarkGray else Color.Transparent
    val textColor = if (isSelected) Color.White else Color.Black

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = textColor,
            style = MaterialTheme.typography.body2
        )
    }

    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun MainPage(onPageSelected: (CustomerPage) -> Unit,) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Pet Shop",
            style = MaterialTheme.typography.h4,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = { onPageSelected(CustomerPage.BROWSE) },
            modifier = Modifier.padding(top = 24.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
        ) {
            Text("Start Browsing", color = Color.White)
        }
    }
}

@Composable
fun BrowseSearchPage(
    animalsQueries: PetshopQueries,
    customer: Customer
) {
    var query by remember { mutableStateOf("") }
    val allAnimals = remember { mutableStateListOf<Animal>() }
    val filteredAnimals = remember { mutableStateListOf<Animal>() }
    var selectedSpecies by remember { mutableStateOf("All") }
    var selectedAnimal by remember { mutableStateOf<Animal?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showTotal by remember { mutableStateOf(false) }
    var transaction by remember { mutableStateOf<Transaction?>(null) }

    fun refreshAnimals() {
        allAnimals.clear()
        allAnimals.addAll(
            animalsQueries.selectAllAnimals().executeAsList()
                .filter { it.status == "Available" }
                .map {
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

    LaunchedEffect(true) {
        refreshAnimals()
        filteredAnimals.clear()
        filteredAnimals.addAll(allAnimals)
    }

    LaunchedEffect(query, selectedSpecies, allAnimals.size) {
        filteredAnimals.clear()
        filteredAnimals.addAll(
            allAnimals.filter {
                (selectedSpecies == "All" || it.species.equals(selectedSpecies, ignoreCase = true)) &&
                (query.isBlank() ||
                    it.name.contains(query, ignoreCase = true) ||
                    it.species.contains(query, ignoreCase = true))
            }
        )
    }

    val listState = rememberLazyListState()
    val speciesList = listOf("All") + allAnimals.map { it.species }.distinct()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Browse Animals",
            style = MaterialTheme.typography.h5,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search by name or species") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Category: ", color = Color.Black)
            Spacer(Modifier.width(8.dp))
            var expanded by remember { mutableStateOf(false) }

            Box {
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
                ) {
                    Text(selectedSpecies, color = Color.White)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    speciesList.forEach { species ->
                        DropdownMenuItem(onClick = {
                            selectedSpecies = species
                            expanded = false
                        }) {
                            Text(species)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            LazyColumn(state = listState) {
                items(filteredAnimals) { animal ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                            .clickable {
                                selectedAnimal = animal
                                showDialog = true
                            },
                        elevation = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Name: ${animal.name}", style = MaterialTheme.typography.subtitle1)
                            Text("Species: ${animal.species}", style = MaterialTheme.typography.body2)
                            Text("Age: ${animal.age}", style = MaterialTheme.typography.body2)
                            Text("Price: $${animal.price}", style = MaterialTheme.typography.body2)
                            Text("Status: ${animal.status}", style = MaterialTheme.typography.body2)
                        }
                    }
                }
            }

            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
            )
        }
    }

    if (showDialog && selectedAnimal != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Animal Details") },
            text = {
                Column {
                    Text("Name: ${selectedAnimal!!.name}")
                    Text("Species: ${selectedAnimal!!.species}")
                    Text("Age: ${selectedAnimal!!.age}")
                    Text("Price: $${selectedAnimal!!.price}")
                    Text("Status: ${selectedAnimal!!.status}")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    animalsQueries.updateAnimalStatus("Bought", selectedAnimal!!.id)
                    transaction = Transaction(
                        customer = customer,
                        animal = selectedAnimal!!,
                        date = LocalDateTime.now()
                    )
                    showDialog = false
                    showTotal = true
                    refreshAnimals()
                }) {
                    Text("Buy")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showTotal && transaction != null) {
        AlertDialog(
            onDismissRequest = { showTotal = false },
            title = { Text("Purchase Successful") },
            text = {
                Column {
                    Text("Name: ${transaction!!.animal.name}")
                    Text("Species: ${transaction!!.animal.species}")
                    Text("Age: ${transaction!!.animal.age}")
                    Text("Price: $${transaction!!.animal.price}")
                    Text("Status: ${transaction!!.animal.status}")
                    Text("Date: ${transaction!!.date}")
                    Text("Total: $${transaction!!.getTotal()}")
                }
            },
            confirmButton = {
                TextButton(onClick = { showTotal = false }) {
                    Text("OK")
                }
            }
        )
    }
}
