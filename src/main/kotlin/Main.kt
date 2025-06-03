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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.petshop.database.Database
import java.io.File

val driver = JdbcSqliteDriver("jdbc:sqlite:petshop.db")
val database = Database(driver)
val usersQueries = database.petshopQueries
val animalsQueries = database.petshopQueries
fun databaseExists(path: String): Boolean = File(path).exists()

enum class Screen {
    Landing,
    MainMenu,
    Login,
    Register,
    AdminPanel,
    Home,
    CustomerPanel
}

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

    val defaultWindowState = rememberWindowState(size = DpSize(1366.dp, 768.dp))

    var currentScreen by remember { mutableStateOf(Screen.Landing) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Petshop App",
        state = defaultWindowState
    ) {
        val windowSize = defaultWindowState.size

        val minWidth = 600.dp
        val minHeight = 400.dp

        MaterialTheme {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (windowSize.width < minWidth || windowSize.height < minHeight) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "\uD83D\uDE04",
                            fontSize = 96.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    WavyBackground(modifier = Modifier.fillMaxSize())
                    when (currentScreen) {
                        Screen.Landing -> LandingPage(onContinue = { currentScreen = Screen.MainMenu })
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
}

@Composable
fun MainMenu(onLogin: () -> Unit, onRegister: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .padding(32.dp)
            .width(350.dp)
    ) {
        Text(
            "\uD83D\uDC36 Welcome to Petshop App \uD83D\uDC31",
            style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF00897B),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onLogin,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00897B)),
            modifier = Modifier.width(220.dp).clip(RoundedCornerShape(16.dp))
        ) {
            Text("Login", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRegister,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00897B)),
            modifier = Modifier.width(220.dp).clip(RoundedCornerShape(16.dp))
        ) {
            Text("Register", color = Color.White, fontWeight = FontWeight.Bold)
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
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .padding(32.dp)
            .width(350.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("\uD83D\uDC31 Register", style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold), color = Color(0xFF00897B))
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Register as: ", color = Color(0xFF00695C))
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
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
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
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00897B))
        ) {
            Text("Register", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBack) {
            Text("Back to Menu", color = Color(0xFF00897B))
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
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00897B)),
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
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
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .padding(32.dp)
            .width(350.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("\uD83D\uDC36 Login", style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold), color = Color(0xFF00897B))
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Login as: ", color = Color(0xFF00695C))
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
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
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
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00897B))
        ) {
            Text("Login", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBack) {
            Text("Back to Menu", color = Color(0xFF00897B))
        }

        message?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = if (it.startsWith("Login successful")) Color(0xFF388E3C) else Color.Red)
        }
    }
}

@Composable
fun LandingPage(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.97f))
            .padding(48.dp)
            .width(500.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "\uD83D\uDC36 Petshop \uD83D\uDC31",
            style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF00897B),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Find your new best friend today!",
            style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Medium),
            color = Color(0xFF00695C),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00897B)),
            modifier = Modifier.width(220.dp).clip(RoundedCornerShape(20.dp))
        ) {
            Text("Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}
