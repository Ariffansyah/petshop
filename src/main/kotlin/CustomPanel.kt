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
import androidx.compose.ui.unit.dp
import app.petshop.database.PetshopQueries

enum class CustomerPage {
    MAIN,
    BROWSE,
    SEARCH
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
                CustomerPage.MAIN -> MainPage()
                CustomerPage.BROWSE -> BrowsePage(animalsQueries)
                CustomerPage.SEARCH -> SearchPage(animalsQueries)
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
                text = "Welcome, $username",
                style = MaterialTheme.typography.h6,
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
            title = "Browse Animals",
            isSelected = currentPage == CustomerPage.BROWSE,
            onClick = { onPageSelected(CustomerPage.BROWSE) }
        )

        SidebarMenuItem(
            title = "Search",
            isSelected = currentPage == CustomerPage.SEARCH,
            onClick = { onPageSelected(CustomerPage.SEARCH) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Logout Button
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
    val textColor = Color.Black

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
fun MainPage() {
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

    }
}

@Composable
fun BrowsePage(animalsQueries: PetshopQueries) {
    val allAnimals = remember { mutableStateListOf<Animal>() }
    val filteredAnimals = remember { mutableStateListOf<Animal>() }
    var selectedSpecies by remember { mutableStateOf("All") }

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

    LaunchedEffect(selectedSpecies, allAnimals.size) {
        filteredAnimals.clear()
        filteredAnimals.addAll(
            if (selectedSpecies == "All") allAnimals
            else allAnimals.filter { it.species.equals(selectedSpecies, ignoreCase = true) }
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

        Text("Filter by Category", style = MaterialTheme.typography.subtitle1)

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
                            .padding(vertical = 8.dp, horizontal = 12.dp),
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
}

@Composable
fun SearchPage(animalsQueries: PetshopQueries) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Search Animals",
            style = MaterialTheme.typography.h5,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

    }
}