import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AdminPanel(onLogout: () -> Unit, username: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color.White, Color.LightGray, Color.DarkGray, Color.Black)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            elevation = 12.dp,
            modifier = Modifier
                .width(350.dp)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text("Admin Panel", style = MaterialTheme.typography.h4, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Welcome, $username!", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)) {
                    Text("Logout", color = Color.White)
                }
            }
        }
    }
}