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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import animalsQueries
import app.petshop.database.PetshopQueries
import java.time.LocalDateTime
import kotlin.collections.addAll
import kotlin.text.clear
import kotlin.text.toInt
import kotlin.text.toLong

enum class CustomerPage {
    MAIN,
    BROWSE,
    PURCHASES // Add new page for purchases
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
    val purchases = remember { mutableStateListOf<Transaction>() } // Track purchases in session

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
                .background(Color(0xFFF6F8FC)) // Soft background
        ) {
            when (currentPage) {
                CustomerPage.MAIN -> MainPage(
                    onPageSelected = { currentPage = it }
                )
                CustomerPage.BROWSE -> BrowseSearchPage(
                    animalsQueries = animalsQueries,
                    customer = Customer(username = username, password = ""),
                    onPurchase = { transaction -> purchases.add(transaction) }
                )
                CustomerPage.PURCHASES -> PurchasesPage(purchases)
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
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFFB2DFDB), Color(0xFFE0F7FA))
                )
            )
            .padding(16.dp)
            .clip(RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "\uD83D\uDC36 Pet Shop",
                    style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp),
                    color = Color(0xFF00695C),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Hello, $username!",
                    style = MaterialTheme.typography.subtitle2,
                    color = Color(0xFF00897B)
                )
            }
        }

        Text(
            text = "Navigation",
            style = MaterialTheme.typography.subtitle2,
            color = Color(0xFF00695C),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SidebarMenuItem(
            title = "\uD83C\uDFE0 Home",
            isSelected = currentPage == CustomerPage.MAIN,
            onClick = { onPageSelected(CustomerPage.MAIN) }
        )

        SidebarMenuItem(
            title = "\uD83D\uDC31 Browse",
            isSelected = currentPage == CustomerPage.BROWSE,
            onClick = { onPageSelected(CustomerPage.BROWSE) }
        )

        SidebarMenuItem(
            title = "\uD83D\uDCB0 My Purchases",
            isSelected = currentPage == CustomerPage.PURCHASES,
            onClick = { onPageSelected(CustomerPage.PURCHASES) }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00897B)),
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
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
    val backgroundColor = if (isSelected) Color(0xFF80CBC4) else Color.Transparent
    val textColor = if (isSelected) Color(0xFF004D40) else Color(0xFF00695C)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = textColor,
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.SemiBold)
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
            text = "\uD83D\uDC36 Welcome to Pet Shop \uD83D\uDC31",
            style = MaterialTheme.typography.h3.copy(fontWeight = FontWeight.Bold, fontSize = 36.sp),
            color = Color(0xFF00897B),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Find your new best friend today!",
            style = MaterialTheme.typography.subtitle1,
            color = Color(0xFF00695C)
        )
        Button(
            onClick = { onPageSelected(CustomerPage.BROWSE) },
            modifier = Modifier.padding(top = 32.dp).clip(RoundedCornerShape(24.dp)),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00897B))
        ) {
            Text("Start Browsing", color = Color.White, fontSize = 18.sp)
        }
    }
}

@Composable
fun BrowseSearchPage(
    animalsQueries: PetshopQueries,
    customer: Customer,
    onPurchase: (Transaction) -> Unit = {}
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
            text = "\uD83D\uDC31 Browse Animals",
            style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF00897B),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search by name or species") },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            leadingIcon = { Text("\uD83D\uDD0D") }
        )

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Category: ", color = Color(0xFF00695C))
            Spacer(Modifier.width(8.dp))
            var expanded by remember { mutableStateOf(false) }

            Box {
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00897B)),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE0F2F1))
                .padding(8.dp)
        ) {
            LazyColumn(state = listState) {
                items(filteredAnimals) { animal ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .clickable {
                                selectedAnimal = animal
                                showDialog = true
                            },
                        elevation = 10.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = when (animal.species.lowercase()) {
                                    "dog" -> "\uD83D\uDC36"
                                    "cat" -> "\uD83D\uDC31"
                                    "rabbit" -> "\uD83D\uDC30"
                                    "bird" -> "\uD83D\uDC26"
                                    else -> "\uD83D\uDC3E"
                                },
                                fontSize = 36.sp,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            Column {
                                Text("Name: ${animal.name}", style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = Color(0xFF00897B)))
                                Text("Species: ${animal.species}", style = MaterialTheme.typography.body2)
                                Text("Age: ${animal.age}", style = MaterialTheme.typography.body2)
                                Text("Price: $${animal.price}", style = MaterialTheme.typography.body2.copy(color = Color(0xFF388E3C), fontWeight = FontWeight.Bold))
                                Text("Status: ${animal.status}", style = MaterialTheme.typography.body2)
                            }
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
            title = { Text("Animal Details", color = Color(0xFF00897B), fontWeight = FontWeight.Bold) },
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
                Button(
                    onClick = {
                        animalsQueries.updateAnimalStatus("Bought", selectedAnimal!!.id)
                        transaction = Transaction(
                            customer = customer,
                            animal = selectedAnimal!!,
                            date = LocalDateTime.now()
                        )
                        showDialog = false
                        showTotal = true
                        refreshAnimals()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF388E3C))
                ) {
                    Text("Buy", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = Color(0xFF00897B))
                }
            },
            shape = RoundedCornerShape(24.dp),
            backgroundColor = Color(0xFFE0F2F1)
        )
    }

    if (showTotal && transaction != null) {
        // Add to purchases when confirmed
        LaunchedEffect(transaction) {
            onPurchase(transaction!!)
        }
        AlertDialog(
            onDismissRequest = { showTotal = false },
            title = { Text("Purchase Successful", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Name: ${transaction!!.animal.name}")
                    Text("Species: ${transaction!!.animal.species}")
                    Text("Age: ${transaction!!.animal.age}")
                    Text("Price: $${transaction!!.animal.price}")
                    Text("Status: ${transaction!!.animal.status}")
                    Text("Date: ${transaction!!.date}")
                    Text("Total: $${transaction!!.getTotal()}", fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTotal = false },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00897B))
                ) {
                    Text("OK", color = Color.White)
                }
            },
            shape = RoundedCornerShape(24.dp),
            backgroundColor = Color(0xFFE0F2F1)
        )
    }
}

@Composable
fun PurchasesPage(purchases: List<Transaction>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            text = "\uD83D\uDCB0 My Purchases",
            style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF00897B),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (purchases.isEmpty()) {
            Text("You haven't bought any animals yet.", color = Color.Gray)
        } else {
            LazyColumn {
                items(purchases) { transaction ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        elevation = 6.dp,
                        backgroundColor = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (transaction.animal.species.lowercase()) {
                                    "dog" -> "\uD83D\uDC36"
                                    "cat" -> "\uD83D\uDC31"
                                    "rabbit" -> "\uD83D\uDC30"
                                    "bird" -> "\uD83D\uDC26"
                                    else -> "\uD83D\uDC3E"
                                },
                                fontSize = 32.sp,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            Column {
                                Text("Name: ${transaction.animal.name}", fontWeight = FontWeight.Bold)
                                Text("Species: ${transaction.animal.species}")
                                Text("Age: ${transaction.animal.age}")
                                Text("Price: $${transaction.animal.price}")
                                Text("Date: ${transaction.date}")
                            }
                        }
                    }
                }
            }
        }
    }
}
