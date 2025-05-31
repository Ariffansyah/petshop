import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.petshop.database.Database
import java.io.File

val driver = JdbcSqliteDriver("jdbc:sqlite:petshop.db")
val database = Database(driver)
val usersQueries = database.petshopQueries
val animalsQueries = database.petshopQueries
fun databaseExists(path: String): Boolean = File(path).exists()

enum class Screen {
    MainMenu,
    Login,
    Register,
    AdminPanel,
    Home,
    CustomerPanel
}

data class Animal(
    val id: Long,
    val name: String,
    val species: String,
    val age: Int,
    val price: Double,
    val status: String
)

enum class UserRole(val display: String) {
    Customer("Customer"),
    Admin("Admin")
}

abstract class User(
    open val id: Int? = null,
    open val username: String,
    open val password: String,
    open val role: UserRole
) {
    abstract fun login(): Boolean
    abstract fun register(): Pair<Boolean, String>
}

class Customer(
    override val id: Int? = null,
    override val username: String,
    override val password: String
) : User(id, username, password, UserRole.Customer) {
    override fun login(): Boolean {
        return try {
            val user = usersQueries.validateUser(username, password, role.display).executeAsOneOrNull()
            user != null
        } catch (e: Exception) {
            false
        }
    }

    override fun register(): Pair<Boolean, String> {
        return try {
            val existing = usersQueries.selectUserByUsername(username).executeAsOneOrNull()
            if (existing != null) {
                false to "Username already exists."
            } else {
                usersQueries.insertUser(username, password, role.display)
                true to "Registered successfully!"
            }
        } catch (e: Exception) {
            false to "Registration error: ${e.message}"
        }
    }
}

class Admin(
    override val id: Int? = null,
    override val username: String,
    override val password: String
) : User(id, username, password, UserRole.Admin) {
    override fun login(): Boolean {
        return try {
            val user = usersQueries.validateUser(username, password, role.display).executeAsOneOrNull()
            user != null
        } catch (e: Exception) {
            false
        }
    }

    override fun register(): Pair<Boolean, String> {
        return try {
            val existing = usersQueries.selectUserByUsername(username).executeAsOneOrNull()
            if (existing != null) {
                false to "Username already exists."
            } else {
                usersQueries.insertUser(username, password, role.display)
                true to "Admin registered successfully!"
            }
        } catch (e: Exception) {
            false to "Registration error: ${e.message}"
        }
    }
}

@Composable
fun WavyBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val blackWave = Path().apply {
            moveTo(0f, height * 0.7f)
            cubicTo(width * 0.25f, height * 0.8f, width * 0.75f, height * 0.6f, width, height * 0.7f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = blackWave,
            color = Color.Black,
            style = Fill
        )

        val greyWave = Path().apply {
            moveTo(0f, height * 0.5f)
            cubicTo(width * 0.25f, height * 0.6f, width * 0.75f, height * 0.4f, width, height * 0.5f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = greyWave,
            color = Color.LightGray.copy(alpha = 0.85f),
            style = Fill
        )

        val whiteWave = Path().apply {
            moveTo(0f, height * 0.3f)
            cubicTo(width * 0.25f, height * 0.4f, width * 0.75f, height * 0.2f, width, height * 0.3f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = whiteWave,
            color = Color.White.copy(alpha = 0.8f),
            style = Fill
        )
    }
}

fun main() = application {
    val dbPath = "petshop.db"
    val driver = JdbcSqliteDriver("jdbc:sqlite:$dbPath")
    if (!databaseExists(dbPath)) {
        Database.Schema.create(driver)
    }
    var loggedUsername by mutableStateOf("")
    var loggedRole by mutableStateOf<UserRole?>(null)
    Window(onCloseRequest = ::exitApplication, title = "Petshop App") {
        var currentScreen by remember { mutableStateOf(Screen.MainMenu) }
        MaterialTheme {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                WavyBackground(modifier = Modifier.fillMaxSize())
                when (currentScreen) {
                    Screen.AdminPanel -> AdminPanel(
                        onLogout = {
                            currentScreen = Screen.MainMenu
                            loggedUsername = ""
                            loggedRole = null
                        },
                        username = loggedUsername,
                        animalsQueries = animalsQueries
                    )
                    Screen.CustomerPanel -> CustomerPanel (
                        onLogout = {
                            currentScreen = Screen.MainMenu
                            loggedUsername = ""
                            loggedRole = null
                        },
                        username = loggedUsername,
                        animalsQueries = animalsQueries
                    )
                    Screen.MainMenu -> MainMenu(
                        onLogin = { currentScreen = Screen.Login },
                        onRegister = { currentScreen = Screen.Register }
                    )
                    Screen.Login -> LoginScreen(
                        onBack = { currentScreen = Screen.MainMenu },
                        onAdminLogin = { username ->
                            loggedUsername = username
                            loggedRole = UserRole.Admin
                            currentScreen = Screen.AdminPanel
                        },
                        onCustomerLogin = { username ->
                            loggedUsername = username
                            loggedRole = UserRole.Customer
                            currentScreen = Screen.CustomerPanel
                        }
                    )
                    Screen.Register -> RegisterScreen(
                        onBack = { currentScreen = Screen.MainMenu }
                    )

                    Screen.Home -> TODO()
                }
            }
        }
    }
}

@Composable
fun MainMenu(onLogin: () -> Unit, onRegister: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Welcome to Petshop App", style = MaterialTheme.typography.h5, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onLogin, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black),
            modifier = Modifier.width(200.dp)) {
            Text("Login", color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRegister, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black),
            modifier = Modifier.width(200.dp)) {
            Text("Register", color = Color.White)
        }
    }
}

@Composable
fun RegisterScreen(onBack: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var selectedRole by remember { mutableStateOf(UserRole.Customer) }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .padding(24.dp)
            .width(300.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Register", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Register as: ")
            Spacer(modifier = Modifier.width(8.dp))
            DropdownMenuRoleSelector(
                selectedRole = selectedRole,
                onRoleSelected = { selectedRole = it }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    message = "All fields are required"
                } else if (password != confirmPassword) {
                    message = "Passwords do not match"
                } else {
                    val user: User = when (selectedRole) {
                        UserRole.Customer -> Customer(username = username, password = password)
                        UserRole.Admin -> Admin(username = username, password = password)
                    }
                    val (success, msg) = user.register()
                    message = msg
                    if (success) {
                        username = ""
                        password = ""
                        confirmPassword = ""
                        onBack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
        ) {
            Text("Register", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBack) {
            Text("Back to Menu", color = Color.Black)
        }

        message?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = if (it.startsWith("Registered")) Color(0xFF388E3C) else Color.Red)
        }
    }
}

@Composable
fun DropdownMenuRoleSelector(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
        ) {
            Text(selectedRole.display, color = Color.White)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            UserRole.values().forEach { role ->
                DropdownMenuItem(onClick = {
                    onRoleSelected(role)
                    expanded = false
                }) {
                    Text(role.display)
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onBack: () -> Unit, onAdminLogin: (String) -> Unit, onCustomerLogin: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var selectedRole by remember { mutableStateOf(UserRole.Customer) }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .padding(24.dp)
            .width(300.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Login as: ")
            Spacer(modifier = Modifier.width(8.dp))
            DropdownMenuRoleSelector(
                selectedRole = selectedRole,
                onRoleSelected = { selectedRole = it }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    message = "Please enter username and password"
                } else {
                    val user: User = when (selectedRole) {
                        UserRole.Customer -> Customer(username = username, password = password)
                        UserRole.Admin -> Admin(username = username, password = password)
                    }
                    if (user.login()) {
                        if (selectedRole == UserRole.Admin) {
                            message = "Login successful! Welcome Admin."
                            onAdminLogin(username)
                        } else {
                            message = "Login successful! Welcome Customer."
                            onCustomerLogin(username)
                        }
                    } else {
                        message = "Invalid username or password"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
        ) {
            Text("Login", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBack) {
            Text("Back to Menu", color = Color.Black)
        }

        message?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = if (it.startsWith("Login successful")) Color(0xFF388E3C) else Color.Red)
        }
    }
}